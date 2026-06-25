import { spawn, execFileSync } from "node:child_process";
import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "../..");
const evidenceDir = path.join(root, "test-evidence", "2026-06-25");
const targetDir = path.join(root, "target", "qa-performance");
fs.mkdirSync(evidenceDir, { recursive: true });
fs.mkdirSync(targetDir, { recursive: true });

const env = Object.fromEntries(
  fs.readFileSync(path.join(root, ".env"), "utf8")
    .split(/\r?\n/)
    .filter((line) => line && !line.startsWith("#") && line.includes("="))
    .map((line) => {
      const index = line.indexOf("=");
      return [line.slice(0, index), line.slice(index + 1)];
    }),
);

const jmeter = process.env.JMETER_BIN || "D:/application/apache-jmeter-5.6.3/bin/jmeter.bat";
const jmx = path.join(root, "scripts", "qa", "performance-test.jmx");
const levels = (process.env.QA_PERF_LEVELS || "30,60,100")
  .split(",")
  .map((value) => Number(value.trim()))
  .filter(Boolean);
const durationSeconds = Number(process.env.QA_PERF_DURATION_SECONDS || 120);
const rampSeconds = Number(process.env.QA_PERF_RAMP_SECONDS || 30);

const loginResponse = await fetch("http://127.0.0.1/api/auth/login/password", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    username: "demo_learner_20260622",
    password: env.ADMIN_PASSWORD,
  }),
});
const loginJson = await loginResponse.json();
if (!loginResponse.ok || !loginJson.data?.token) {
  throw new Error("Unable to obtain learner token for stepped performance test");
}
const learnerToken = loginJson.data.token;

const scenarios = [
  {
    id: "ST-PERF-S1",
    name: "public-outcome-query",
    label: "public-outcome-query",
    method: "GET",
    path: "/api/public/outcomes?page=1&size=10",
    authHeader: "",
    requestBody: "",
    threshold: 500,
  },
  {
    id: "ST-PERF-S2",
    name: "authenticated-credit-account",
    label: "authenticated-credit-account",
    method: "GET",
    path: "/api/credits/account",
    authHeader: `Bearer ${learnerToken}`,
    requestBody: "",
    threshold: 500,
  },
  {
    id: "ST-PERF-S3",
    name: "password-login",
    label: "password-login",
    method: "POST",
    path: "/api/auth/login/password",
    authHeader: "",
    requestBody: JSON.stringify({
      username: "demo_learner_20260622",
      password: env.ADMIN_PASSWORD,
    }),
    threshold: 1000,
  },
];

const resourceStream = fs.createWriteStream(path.join(evidenceDir, "performance-stepped-docker-stats.csv"));
resourceStream.write("timestamp,scenario,threads,container,cpu_percent,memory_usage\n");

function propertiesText(scenario, threads) {
  const escape = (value) => String(value)
    .replaceAll("\\", "\\\\")
    .replaceAll("\r", "\\r")
    .replaceAll("\n", "\\n");
  return [
    `threads=${threads}`,
    `ramp=${rampSeconds}`,
    `duration=${durationSeconds}`,
    `label=${escape(scenario.label)}`,
    `method=${escape(scenario.method)}`,
    `path=${escape(scenario.path)}`,
    `authHeader=${escape(scenario.authHeader)}`,
    `requestBody=${escape(scenario.requestBody)}`,
    "",
  ].join("\n");
}

function sampleResources(scenario, threads) {
  try {
    const output = execFileSync("docker", [
      "stats", "--no-stream", "--format",
      "{{.Name}}|{{.CPUPerc}}|{{.MemUsage}}",
      "llcb-backend", "llcb-mysql", "llcb-redis", "llcb-nginx",
    ], { encoding: "utf8" });
    for (const line of output.trim().split(/\r?\n/).filter(Boolean)) {
      const [container, cpu, memory] = line.split("|");
      resourceStream.write(`${new Date().toISOString()},${scenario.name},${threads},${container},${cpu},${memory}\n`);
    }
  } catch {
    resourceStream.write(`${new Date().toISOString()},${scenario.name},${threads},stats-unavailable,,\n`);
  }
}

function parseCsvLine(line) {
  const values = [];
  let value = "";
  let quoted = false;
  for (let index = 0; index < line.length; index += 1) {
    const char = line[index];
    if (char === "\"") {
      if (quoted && line[index + 1] === "\"") {
        value += "\"";
        index += 1;
      } else {
        quoted = !quoted;
      }
    } else if (char === "," && !quoted) {
      values.push(value);
      value = "";
    } else {
      value += char;
    }
  }
  values.push(value);
  return values;
}

function percentile(sorted, percentileValue) {
  if (!sorted.length) return 0;
  const index = Math.ceil((percentileValue / 100) * sorted.length) - 1;
  return sorted[Math.max(0, Math.min(index, sorted.length - 1))];
}

function summarizeJtl(jtl, scenario, threads) {
  const lines = fs.readFileSync(jtl, "utf8").trim().split(/\r?\n/);
  const headers = parseCsvLine(lines.shift());
  const elapsedIndex = headers.indexOf("elapsed");
  const successIndex = headers.indexOf("success");
  const timestampIndex = headers.indexOf("timeStamp");
  const responseCodeIndex = headers.indexOf("responseCode");
  const rows = lines.map(parseCsvLine);
  const elapsed = rows.map((row) => Number(row[elapsedIndex])).sort((a, b) => a - b);
  const failures = rows.filter((row) => row[successIndex] !== "true");
  const firstTimestamp = Math.min(...rows.map((row) => Number(row[timestampIndex])));
  const lastTimestamp = Math.max(...rows.map((row) => Number(row[timestampIndex])));
  const observedSeconds = Math.max(1, (lastTimestamp - firstTimestamp) / 1000);
  const errorRate = rows.length ? failures.length / rows.length : 1;
  return {
    id: scenario.id,
    scenario: scenario.name,
    threads,
    rampSeconds,
    durationSeconds,
    pacingMilliseconds: 1000,
    requests: rows.length,
    failures: failures.length,
    errorRatePercent: Number((errorRate * 100).toFixed(3)),
    averageMilliseconds: Number((elapsed.reduce((sum, value) => sum + value, 0) / elapsed.length).toFixed(2)),
    p90Milliseconds: percentile(elapsed, 90),
    p95Milliseconds: percentile(elapsed, 95),
    p99Milliseconds: percentile(elapsed, 99),
    maxMilliseconds: elapsed.at(-1),
    throughputPerSecond: Number((rows.length / observedSeconds).toFixed(2)),
    responseCodes: [...new Set(rows.map((row) => row[responseCodeIndex]))],
    thresholdMilliseconds: scenario.threshold,
    passed: failures.length === 0 && errorRate <= 0.01 && percentile(elapsed, 95) <= scenario.threshold,
  };
}

async function runScenario(scenario, threads) {
  const name = `${scenario.name}-${threads}u`;
  const properties = path.join(targetDir, `${name}.properties`);
  const jtl = path.join(evidenceDir, `performance-stepped-${name}.jtl`);
  const log = path.join(evidenceDir, `performance-stepped-${name}.log`);
  fs.writeFileSync(properties, propertiesText(scenario, threads));
  fs.rmSync(jtl, { force: true });
  const args = [
    "-n", "-t", jmx, "-q", properties, "-l", jtl, "-j", log,
    "-Jjmeter.save.saveservice.output_format=csv",
    "-Jjmeter.save.saveservice.print_field_names=true",
    "-Jjmeter.save.saveservice.response_data=false",
    "-Jjmeter.save.saveservice.samplerData=false",
    "-Jjmeter.save.saveservice.requestHeaders=false",
    "-Jjmeter.save.saveservice.responseHeaders=false",
  ];
  const child = spawn("cmd.exe", ["/d", "/s", "/c", jmeter, ...args], {
    cwd: root,
    stdio: ["ignore", "pipe", "pipe"],
  });
  let output = "";
  child.stdout.on("data", (data) => { output += data.toString(); });
  child.stderr.on("data", (data) => { output += data.toString(); });
  sampleResources(scenario, threads);
  const timer = setInterval(() => sampleResources(scenario, threads), 5000);
  const exitCode = await new Promise((resolve) => child.on("close", resolve));
  clearInterval(timer);
  sampleResources(scenario, threads);
  fs.rmSync(properties, { force: true });
  fs.writeFileSync(path.join(evidenceDir, `performance-stepped-${name}-console.txt`), output);
  if (exitCode !== 0) {
    return {
      id: scenario.id,
      scenario: scenario.name,
      threads,
      passed: false,
      error: `JMeter exit code ${exitCode}`,
    };
  }
  return summarizeJtl(jtl, scenario, threads);
}

const summaries = [];
try {
  for (const threads of levels) {
    for (const scenario of scenarios) {
      console.log(`Starting ${scenario.name} ${threads} users`);
      summaries.push(await runScenario(scenario, threads));
      console.log(JSON.stringify(summaries.at(-1)));
    }
  }
} finally {
  resourceStream.end();
}

const output = {
  metadata: {
    tool: "Apache JMeter 5.6.3",
    executedAt: new Date().toISOString(),
    gateway: "http://127.0.0.1",
    levels,
    durationSeconds,
    rampSeconds,
  },
  summaries,
};

fs.writeFileSync(path.join(evidenceDir, "performance-stepped-summary.json"), JSON.stringify(output, null, 2));
fs.writeFileSync(path.join(evidenceDir, "performance-stepped-summary.md"), [
  "# Stepped Business Performance Summary",
  "",
  `- Tool: ${output.metadata.tool}`,
  `- Executed: ${output.metadata.executedAt}`,
  `- Load levels: ${levels.join(", ")} users`,
  `- Duration per scenario: ${durationSeconds}s, ramp-up: ${rampSeconds}s`,
  "- Gateway: Nginx on 127.0.0.1:80",
  "",
  "| ID | Scenario | Users | Requests | Failures | HTTP/JMeter error rate | Avg ms | P90 | P95 | P99 | Max | Throughput/s | Result |",
  "|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|",
  ...summaries.map((summary) =>
    `| ${summary.id} | ${summary.scenario} | ${summary.threads} | ${summary.requests ?? 0} | ${summary.failures ?? "n/a"} | ${summary.errorRatePercent ?? "n/a"}% | ${summary.averageMilliseconds ?? "n/a"} | ${summary.p90Milliseconds ?? "n/a"} | ${summary.p95Milliseconds ?? "n/a"} | ${summary.p99Milliseconds ?? "n/a"} | ${summary.maxMilliseconds ?? "n/a"} | ${summary.throughputPerSecond ?? "n/a"} | ${summary.passed ? "PASS" : "FAIL"} |`),
  "",
  "This is a local capacity baseline only. It does not extrapolate production capacity because host resources, data scale, network topology and JVM/container sizing differ from production.",
  "",
].join("\n"));

if (summaries.some((summary) => !summary.passed)) process.exitCode = 1;
