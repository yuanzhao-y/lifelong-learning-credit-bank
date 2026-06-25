import { execFileSync } from "node:child_process";
import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "../..");
const evidenceDir = path.join(root, "test-evidence", "2026-06-25");
const env = Object.fromEntries(
  fs.readFileSync(path.join(root, ".env"), "utf8")
    .split(/\r?\n/)
    .filter((line) => line && !line.startsWith("#") && line.includes("="))
    .map((line) => {
      const index = line.indexOf("=");
      return [line.slice(0, index), line.slice(index + 1)];
    }),
);

function docker(args) {
  return execFileSync("docker", args, { encoding: "utf8" }).trim();
}

function compose(args) {
  return execFileSync("docker", ["compose", ...args], { cwd: root, encoding: "utf8" }).trim();
}

function databaseCounts() {
  const query = `
SELECT 'users', COUNT(*) FROM sys_user WHERE deleted=0;
SELECT 'credit_flows', COUNT(*) FROM credit_flow WHERE deleted=0;
SELECT 'conversion_transactions', COUNT(*) FROM conversion_transaction WHERE deleted=0;
SELECT 'operation_logs', COUNT(*) FROM sys_operation_log WHERE deleted=0;
`;
  const output = docker([
    "exec", "llcb-mysql", "mysql", "-ullcb", `-p${env.MYSQL_PASSWORD}`,
    "--batch", "--raw", "--skip-column-names", "llcb", "-e", query,
  ]);
  return Object.fromEntries(output.split(/\r?\n/).map((line) => line.split("\t")));
}

function jenkinsState() {
  const output = docker([
    "exec", "llcb-jenkins", "sh", "-c",
    "find /var/jenkins_home/jobs -maxdepth 3 -type d -name builds -o -type f -name nextBuildNumber 2>/dev/null | sort",
  ]);
  return output.split(/\r?\n/).filter(Boolean);
}

async function waitForHealth(url, timeoutMilliseconds) {
  const deadline = Date.now() + timeoutMilliseconds;
  while (Date.now() < deadline) {
    try {
      const response = await fetch(url);
      if (response.status === 200) return true;
    } catch {
      // Keep waiting while containers reconnect.
    }
    await new Promise((resolve) => setTimeout(resolve, 2000));
  }
  return false;
}

const before = {
  database: databaseCounts(),
  jenkins: jenkinsState(),
  volumes: docker(["volume", "ls", "--format", "{{.Name}}"])
    .split(/\r?\n/)
    .filter((name) => name.includes("lifelong-learning-credit-bank")),
};

compose(["restart", "mysql", "redis"]);
const dependenciesHealthy = await waitForHealth("http://127.0.0.1:8080/api/actuator/health", 120000);
compose(["restart", "backend", "nginx", "jenkins"]);
const directHealthy = await waitForHealth("http://127.0.0.1:8080/api/actuator/health", 120000);
const proxyHealthy = await waitForHealth("http://127.0.0.1/api/actuator/health", 120000);

const after = {
  database: databaseCounts(),
  jenkins: jenkinsState(),
  redisPing: docker([
    "exec", "llcb-redis", "redis-cli", "-a", env.REDIS_PASSWORD, "--no-auth-warning", "ping",
  ]),
};

const checks = {
  dependenciesRecovered: dependenciesHealthy,
  directHealthRecovered: directHealthy,
  proxyHealthRecovered: proxyHealthy,
  databaseCountsPreserved: JSON.stringify(before.database) === JSON.stringify(after.database),
  jenkinsStatePreserved: JSON.stringify(before.jenkins) === JSON.stringify(after.jenkins),
  redisRecovered: after.redisPing === "PONG",
  persistentVolumesPresent: before.volumes.length >= 3,
};
const passed = Object.values(checks).every(Boolean);
const output = {
  metadata: { executedAt: new Date().toISOString(), destructiveVolumeReset: false },
  before,
  after,
  checks,
  passed,
};
fs.writeFileSync(path.join(evidenceDir, "recovery-audit.json"), JSON.stringify(output, null, 2));
fs.writeFileSync(path.join(evidenceDir, "recovery-audit.md"), [
  "# Recovery And Persistence Audit",
  "",
  `- Executed: ${output.metadata.executedAt}`,
  "- Operation: restart MySQL, Redis, backend, Nginx and Jenkins without deleting volumes",
  "",
  "| Check | Result |",
  "|---|---|",
  ...Object.entries(checks).map(([name, value]) => `| ${name} | ${value ? "PASS" : "FAIL"} |`),
  "",
  `Final result: **${passed ? "PASS" : "FAIL"}**`,
  "",
].join("\n"));
console.log(JSON.stringify({ checks, passed }));
if (!passed) process.exitCode = 1;
