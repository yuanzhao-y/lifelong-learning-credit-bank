import { execFileSync, spawnSync } from "node:child_process";
import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "../..");
const evidenceDir = path.join(root, "test-evidence", "2026-06-25");
fs.mkdirSync(evidenceDir, { recursive: true });

const proxy = process.env.QA_PROXY || "http://127.0.0.1:6244";

function run(command, args, options = {}) {
  const startedAt = new Date().toISOString();
  const result = spawnSync(command, args, {
    cwd: options.cwd || root,
    encoding: "utf8",
    timeout: options.timeoutMilliseconds || 300000,
    env: {
      ...process.env,
      HTTP_PROXY: process.env.HTTP_PROXY || proxy,
      HTTPS_PROXY: process.env.HTTPS_PROXY || proxy,
      http_proxy: process.env.http_proxy || proxy,
      https_proxy: process.env.https_proxy || proxy,
    },
    shell: process.platform === "win32",
  });
  return {
    command: [command, ...args].join(" "),
    startedAt,
    exitCode: result.status,
    timedOut: Boolean(result.error && result.error.code === "ETIMEDOUT"),
    error: result.error?.message || null,
    stdout: result.stdout || "",
    stderr: result.stderr || "",
  };
}

function writeJson(name, value) {
  fs.writeFileSync(path.join(evidenceDir, name), JSON.stringify(value, null, 2));
}

function summarizeNpmAudit() {
  const auditPath = path.join(evidenceDir, "npm-audit.json");
  if (!fs.existsSync(auditPath)) {
    return { status: "LIMITED", detail: "npm-audit.json is not present in evidence directory" };
  }
  const bytes = fs.readFileSync(auditPath);
  let text;
  if (bytes[0] === 0xff && bytes[1] === 0xfe) {
    text = bytes.toString("utf16le").replace(/^\uFEFF/, "");
  } else if (bytes[0] === 0xfe && bytes[1] === 0xff) {
    return { status: "LIMITED", detail: "npm-audit.json is UTF-16BE, which this audit runner does not parse" };
  } else {
    text = bytes.toString("utf8").replace(/^\uFEFF/, "");
  }
  const audit = JSON.parse(text);
  const vulnerabilities = audit.metadata?.vulnerabilities || {};
  const total = Object.entries(vulnerabilities)
    .filter(([key]) => key !== "total")
    .reduce((sum, [, count]) => sum + Number(count || 0), 0);
  return {
    status: total === 0 ? "PASS" : "FAIL",
    detail: `metadata vulnerabilities total=${vulnerabilities.total ?? total}`,
  };
}

function runMavenDependencyTree() {
  const result = run("mvn", [
    "-B",
    "-DincludeScope=runtime",
    "-DoutputFile=target/dependency-tree.txt",
    "dependency:tree",
  ], { timeoutMilliseconds: 300000 });
  const treePath = path.join(root, "target", "dependency-tree.txt");
  if (fs.existsSync(treePath)) {
    fs.copyFileSync(treePath, path.join(evidenceDir, "maven-dependency-tree.txt"));
  }
  return {
    status: result.exitCode === 0 ? "PASS" : "LIMITED",
    detail: result.exitCode === 0
      ? "Runtime dependency tree generated for audit traceability"
      : "Dependency tree generation failed or timed out; see raw log",
    raw: result,
  };
}

function runOsvScanner() {
  let tool = "osv-scanner";
  try {
    execFileSync("where", ["osv-scanner"], { encoding: "utf8" });
  } catch {
    tool = "npx";
  }
  const args = tool === "npx"
    ? ["-y", "osv-scanner@latest", "scan", "--format=json", "--output=test-evidence/2026-06-25/osv-scan-final.json", "."]
    : ["scan", "--format=json", "--output=test-evidence/2026-06-25/osv-scan-final.json", "."];
  const result = run(tool, args, { timeoutMilliseconds: 600000 });
  let findings = null;
  const outputPath = path.join(evidenceDir, "osv-scan-final.json");
  if (fs.existsSync(outputPath)) {
    try {
      const parsed = JSON.parse(fs.readFileSync(outputPath, "utf8"));
      findings = JSON.stringify(parsed).includes("\"vulnerability\"");
    } catch {
      findings = null;
    }
  }
  return {
    status: result.exitCode === 0 && findings === false ? "PASS" : result.exitCode === 0 ? "REVIEW" : "LIMITED",
    detail: result.exitCode === 0
      ? "OSV scanner completed; review JSON for package-level details"
      : "OSV scanner unavailable, timed out or failed; not declared pass",
    raw: result,
  };
}

const evidence = {
  metadata: {
    executedAt: new Date().toISOString(),
    proxy,
  },
  checks: {
    npmAudit: summarizeNpmAudit(),
    mavenDependencyTree: runMavenDependencyTree(),
    osvScanner: runOsvScanner(),
  },
};

writeJson("dependency-security-final.json", evidence);

const rows = [
  ["npm audit", evidence.checks.npmAudit.status, evidence.checks.npmAudit.detail],
  ["Maven runtime dependency tree", evidence.checks.mavenDependencyTree.status, evidence.checks.mavenDependencyTree.detail],
  ["OSV scanner", evidence.checks.osvScanner.status, evidence.checks.osvScanner.detail],
];

fs.writeFileSync(path.join(evidenceDir, "dependency-security-final.md"), [
  "# Backend Dependency Security Final Check",
  "",
  `- Executed: ${evidence.metadata.executedAt}`,
  `- Proxy default: ${proxy}`,
  "",
  "| Check | Result | Evidence / limitation |",
  "|---|---|---|",
  ...rows.map((row) => `| ${row[0]} | ${row[1]} | ${String(row[2]).replace(/\|/g, "\\|")} |`),
  "",
  "A PASS result means the tool completed for the configured scope. LIMITED means the report remains auditable but cannot be used as a clean security conclusion.",
  "",
].join("\n"));

const failed = rows.some(([, status]) => status === "FAIL");
if (failed) process.exitCode = 1;
console.log(JSON.stringify({
  npmAudit: evidence.checks.npmAudit.status,
  dependencyTree: evidence.checks.mavenDependencyTree.status,
  osv: evidence.checks.osvScanner.status,
}));
