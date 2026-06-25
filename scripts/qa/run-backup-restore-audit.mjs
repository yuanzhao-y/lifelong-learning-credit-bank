import { execFileSync } from "node:child_process";
import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "../..");
const evidenceDir = path.join(root, "test-evidence", "2026-06-25");
const backupDir = path.join(root, "target", "qa-restore");
fs.mkdirSync(evidenceDir, { recursive: true });
fs.mkdirSync(backupDir, { recursive: true });

const env = Object.fromEntries(
  fs.readFileSync(path.join(root, ".env"), "utf8")
    .split(/\r?\n/)
    .filter((line) => line && !line.startsWith("#") && line.includes("="))
    .map((line) => {
      const index = line.indexOf("=");
      return [line.slice(0, index), line.slice(index + 1)];
    }),
);

function docker(args, options = {}) {
  return execFileSync("docker", args, { encoding: options.encoding || "utf8", stdio: options.stdio || "pipe" }).trim();
}

function counts(container = "llcb-mysql", database = "llcb") {
  const query = `
SELECT 'users', COUNT(*) FROM sys_user WHERE deleted=0;
SELECT 'credit_flows', COUNT(*) FROM credit_flow WHERE deleted=0;
SELECT 'conversion_transactions', COUNT(*) FROM conversion_transaction WHERE deleted=0;
SELECT 'operation_logs', COUNT(*) FROM sys_operation_log WHERE deleted=0;
`;
  const output = docker([
    "exec", container, "mysql", "-ullcb", `-p${env.MYSQL_PASSWORD}`,
    "--batch", "--raw", "--skip-column-names", database, "-e", query,
  ]);
  return Object.fromEntries(output.split(/\r?\n/).map((line) => line.split("\t")));
}

function redisInfo(container = "llcb-redis") {
  const keys = docker([
    "exec", container, "redis-cli", "-a", env.REDIS_PASSWORD, "--no-auth-warning",
    "dbsize",
  ]);
  const ping = docker([
    "exec", container, "redis-cli", "-a", env.REDIS_PASSWORD, "--no-auth-warning",
    "ping",
  ]);
  return { ping, keys: Number(keys) };
}

const started = Date.now();
const stamp = new Date().toISOString().replace(/\D/g, "").slice(0, 14);
const mysqlDump = path.join(backupDir, `llcb-${stamp}.sql`);
const redisBackup = path.join(backupDir, `redis-${stamp}.rdb`);
const jenkinsListing = path.join(backupDir, `jenkins-home-${stamp}.txt`);

const before = {
  database: counts(),
  redis: redisInfo(),
};

execFileSync("docker", [
  "exec", "llcb-mysql", "mysqldump", "-ullcb", `-p${env.MYSQL_PASSWORD}`,
  "--single-transaction", "--routines", "--triggers", "llcb",
], { encoding: "utf8", stdio: ["ignore", fs.openSync(mysqlDump, "w"), "pipe"] });
docker([
  "exec", "llcb-redis", "redis-cli", "-a", env.REDIS_PASSWORD, "--no-auth-warning",
  "save",
]);
execFileSync("docker", ["cp", "llcb-redis:/data/dump.rdb", redisBackup]);
const jenkinsState = docker([
  "exec", "llcb-jenkins", "sh", "-c",
  "find /var/jenkins_home/jobs -maxdepth 3 -type d -name builds -o -type f -name nextBuildNumber 2>/dev/null | sort",
]);
fs.writeFileSync(jenkinsListing, jenkinsState + "\n");

const restoreContainer = `llcb-restore-mysql-${stamp}`;
docker([
  "run", "-d", "--name", restoreContainer,
  "-e", "MYSQL_DATABASE=llcb_restore",
  "-e", "MYSQL_USER=llcb",
  "-e", `MYSQL_PASSWORD=${env.MYSQL_PASSWORD}`,
  "-e", `MYSQL_ROOT_PASSWORD=${env.MYSQL_ROOT_PASSWORD}`,
  "mysql:8.0.33",
  "--character-set-server=utf8mb4",
  "--collation-server=utf8mb4_unicode_ci",
]);

let mysqlRestoreReady = false;
try {
  for (let index = 0; index < 60; index += 1) {
    try {
      docker(["exec", restoreContainer, "mysqladmin", "ping", "-h", "127.0.0.1", "-ullcb", `-p${env.MYSQL_PASSWORD}`, "--silent"]);
      mysqlRestoreReady = true;
      break;
    } catch {
      await new Promise((resolve) => setTimeout(resolve, 2000));
    }
  }
  if (!mysqlRestoreReady) throw new Error("restore MySQL container did not become ready");
  execFileSync("docker", ["cp", mysqlDump, `${restoreContainer}:/tmp/restore.sql`]);
  docker([
    "exec", restoreContainer, "sh", "-c",
    `mysql -ullcb -p'${env.MYSQL_PASSWORD}' llcb_restore < /tmp/restore.sql`,
  ]);
  const afterRestore = {
    database: counts(restoreContainer, "llcb_restore"),
  };

  const finished = Date.now();
  const checks = {
    mysqlDumpCreated: fs.statSync(mysqlDump).size > 0,
    redisBackupCreated: fs.statSync(redisBackup).size > 0,
    jenkinsListingCreated: fs.statSync(jenkinsListing).size > 0,
    mysqlRestoreReady,
    databaseCountsMatch: JSON.stringify(before.database) === JSON.stringify(afterRestore.database),
    redisBackupObservable: before.redis.ping === "PONG",
  };
  const output = {
    metadata: {
      executedAt: new Date().toISOString(),
      mysqlDump: path.relative(root, mysqlDump).replaceAll("\\", "/"),
      redisBackup: path.relative(root, redisBackup).replaceAll("\\", "/"),
      jenkinsListing: path.relative(root, jenkinsListing).replaceAll("\\", "/"),
      rtoSeconds: Number(((finished - started) / 1000).toFixed(2)),
      rpo: "point-in-time manual dump/snapshot during audit",
    },
    before,
    afterRestore,
    checks,
    passed: Object.values(checks).every(Boolean),
  };
  fs.writeFileSync(path.join(evidenceDir, "backup-restore-audit.json"), JSON.stringify(output, null, 2));
  fs.writeFileSync(path.join(evidenceDir, "backup-restore-audit.md"), [
    "# Backup Restore Audit",
    "",
    `- Executed: ${output.metadata.executedAt}`,
    `- MySQL dump: ${output.metadata.mysqlDump}`,
    `- Redis backup: ${output.metadata.redisBackup}`,
    `- Jenkins listing: ${output.metadata.jenkinsListing}`,
    `- Measured RTO for this manual restore exercise: ${output.metadata.rtoSeconds}s`,
    `- RPO model: ${output.metadata.rpo}`,
    "",
    "| Check | Result |",
    "|---|---|",
    ...Object.entries(checks).map(([name, value]) => `| ${name} | ${value ? "PASS" : "FAIL"} |`),
    "",
    `Final result: **${output.passed ? "PASS" : "FAIL"}**`,
    "",
    "This validates a basic local backup/restore path. It does not cover host loss, cross-region recovery, automated failover, TOS object recovery or full disaster-recovery certification.",
    "",
  ].join("\n"));
  console.log(JSON.stringify({ checks, passed: output.passed }));
  if (!output.passed) process.exitCode = 1;
} finally {
  try {
    docker(["rm", "-f", restoreContainer]);
  } catch {
    // Best effort cleanup of the temporary restore container.
  }
}
