import { spawn, execFileSync } from "node:child_process";
import fs from "node:fs";
import path from "node:path";
import readline from "node:readline";

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
  throw new Error("Unable to obtain learner token for performance test");
}
const learnerToken = loginJson.data.token;

const jmeter = "D:/application/apache-jmeter-5.6.3/bin/jmeter.bat";
const jmx = path.join(root, "scripts", "qa", "performance-test.jmx");
const scenarios = [
  {
    id: "ST-PERF-03",
    name: "public-outcome-query",
    label: "public-outcome-query",
    method: "GET",
    path: "/api/public/outcomes?page=1&size=10",
    authHeader: "",
    requestBody: "",
    threshold: 500,
  },
  {
    id: "ST-PERF-04",
    name: "authenticated-credit-account",
    label: "authenticated-credit-account",
    method: "GET",
    path: "/api/credits/account",
    authHeader: `Bearer ${learnerToken}`,
    requestBody: "",
    threshold: 500,
  },
  {
    id: "ST-PERF-05",
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

const resourceStream = fs.createWriteStream(path.join(evidenceDir, "performance-docker-stats.csv"));
resourceStream.write("timestamp,scenario,container,cpu_percent,memory_usage\n");

function propertiesText(scenario) {
  const escape = (value) => String(value)
    .replaceAll("\\", "\\\\")
    .replaceAll("\r", "\\r")
    .replaceAll("\n", "\\n");
  return [
    "threads=30",
    "ramp=30",
    "duration=300",
    `label=${escape(scenario.label)}`,
    `method=${escape(scenario.method)}`,
    `path=${escape(scenario.path)}`,
    `authHeader=${escape(scenario.authHeader)}`,
    `requestBody=${escape(scenario.requestBody)}`,
    "",
  ].join("\n");
}

function sampleResources(scenario) {
  try {
    const output = execFileSync("docker", [
      "stats", "--no-stream", "--format",
      "{{.Name}}|{{.CPUPerc}}|{{.MemUsage}}",
      "llcb-backend", "llcb-mysql", "llcb-redis", "llcb-nginx",
    ], { encoding: "utf8" });
    for (const line of output.trim().split(/\r?\n/)) {
      const [container, cpu, memory] = line.split("|");
      resourceStream.write(`${new Date().toISOString()},${scenario.name},${container},${cpu},${memory}\n`);
    }
  } catch {
    resourceStream.write(`${new Date().toISOString()},${scenario.name},stats-unavailable,,\n`);
  }
}

async function runScenario(scenario) {
  const properties = path.join(targetDir, `${scenario.name}.properties`);
  const jtl = path.join(evidenceDir, `performance-${scenario.name}.jtl`);
  const log = path.join(evidenceDir, `performance-${scenario.name}.log`);
  fs.writeFileSync(properties, propertiesText(scenario));
  if (fs.existsSync(jtl)) fs.rmSync(jtl);

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
  sampleResources(scenario);
  const timer = setInterval(() => sampleResources(scenario), 5000);
  const exitCode = await new Promise((resolve) => child.on("close", resolve));
  clearInterval(timer);
  sampleResources(scenario);
  fs.rmSync(properties, { force: true });
  fs.writeFileSync(path.join(evidenceDir, `performance-${scenario.name}-console.txt`), output);
  if (exitCode !== 0) {
    throw new Error(`JMeter scenario ${scenario.name} failed with exit code ${exitCode}`);
  }
  return summarizeJtl(jtl, scenario);
}

function percentile(sorted, percentileValue) {
  if (!sorted.length) return 0;
  const index = Math.ceil((percentileValue / 100) * sorted.length) - 1;
  return sorted[Math.max(0, Math.min(index, sorted.length - 1))];
}

function parseCsvLine(line) {
  const values = [];
  let value = "";
  let quoted = false;
  for (let index = 0; index < line.length; index += 1) {
    const char = line[index];
    if (char === '"') {
      if (quoted && line[index + 1] === '"') {
        value += '"';
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

function summarizeJtl(jtl, scenario) {
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
  const p95 = percentile(elapsed, 95);
  const errorRate = rows.length ? failures.length / rows.length : 1;
  return {
    id: scenario.id,
    scenario: scenario.name,
    threads: 30,
    rampSeconds: 30,
    durationSeconds: 300,
    pacingMilliseconds: 1000,
    requests: rows.length,
    failures: failures.length,
    errorRatePercent: Number((errorRate * 100).toFixed(3)),
    averageMilliseconds: Number((elapsed.reduce((sum, value) => sum + value, 0) / elapsed.length).toFixed(2)),
    p90Milliseconds: percentile(elapsed, 90),
    p95Milliseconds: p95,
    p99Milliseconds: percentile(elapsed, 99),
    maxMilliseconds: elapsed.at(-1),
    throughputPerSecond: Number((rows.length / observedSeconds).toFixed(2)),
    responseCodes: [...new Set(rows.map((row) => row[responseCodeIndex]))],
    thresholdMilliseconds: scenario.threshold,
    passed: failures.length === 0 && errorRate <= 0.01 && p95 <= scenario.threshold,
  };
}

const summaries = [];
try {
  for (const scenario of scenarios) {
    console.log(`Starting ${scenario.name}`);
    summaries.push(await runScenario(scenario));
    console.log(JSON.stringify(summaries.at(-1)));
  }
} finally {
  resourceStream.end();
}

const output = {
  metadata: {
    tool: "Apache JMeter 5.6.3",
    executedAt: new Date().toISOString(),
    gateway: "http://127.0.0.1",
  },
  summaries,
};
fs.writeFileSync(path.join(evidenceDir, "performance-summary.json"), JSON.stringify(output, null, 2));
fs.writeFileSync(path.join(evidenceDir, "performance-summary.md"), [
  "# Business Performance Summary",
  "",
  `- Tool: ${output.metadata.tool}`,
  `- Executed: ${output.metadata.executedAt}`,
  "- Load: 30 users, 30-second ramp-up, 300-second duration, one-second pacing",
  "- Gateway: Nginx on 127.0.0.1:80",
  "",
  "| ID | Scenario | Requests | Failures | Error rate | Avg ms | P90 | P95 | P99 | Max | Throughput/s | Result |",
  "|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|",
  ...summaries.map((summary) =>
    `| ${summary.id} | ${summary.scenario} | ${summary.requests} | ${summary.failures} | ${summary.errorRatePercent}% | ${summary.averageMilliseconds} | ${summary.p90Milliseconds} | ${summary.p95Milliseconds} | ${summary.p99Milliseconds} | ${summary.maxMilliseconds} | ${summary.throughputPerSecond} | ${summary.passed ? "PASS" : "FAIL"} |`),
  "",
].join("\n"));

if (summaries.some((summary) => !summary.passed)) process.exitCode = 1;
