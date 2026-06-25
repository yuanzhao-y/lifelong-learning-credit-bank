import { spawnSync } from "node:child_process";
import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "../..");
const evidenceDir = path.join(root, "test-evidence", "2026-06-25");
fs.mkdirSync(evidenceDir, { recursive: true });

const target = process.env.ZAP_TARGET || "http://host.docker.internal/api";
const sourceOpenApiUrl = process.env.ZAP_SOURCE_OPENAPI_URL || "http://127.0.0.1/api/v3/api-docs";
const timeoutSeconds = Number(process.env.ZAP_TIMEOUT_SECONDS || 900);

const sourceOpenApi = await fetch(sourceOpenApiUrl).then((response) => {
  if (!response.ok) throw new Error(`Unable to fetch OpenAPI document: HTTP ${response.status}`);
  return response.json();
});
sourceOpenApi.servers = [{ url: target }];
const openApiFile = path.join(evidenceDir, "zap-final-openapi.json");
fs.writeFileSync(openApiFile, JSON.stringify(sourceOpenApi, null, 2));

function runZap(args, logName) {
  const startedAt = new Date().toISOString();
  const result = spawnSync("docker", args, {
    cwd: root,
    encoding: "utf8",
    timeout: (timeoutSeconds + 120) * 1000,
  });
  const log = [
    `startedAt=${startedAt}`,
    `command=docker ${args.join(" ")}`,
    `exitCode=${result.status}`,
    `timedOut=${Boolean(result.error && result.error.code === "ETIMEDOUT")}`,
    "",
    "STDOUT",
    result.stdout || "",
    "",
    "STDERR",
    result.stderr || "",
  ].join("\n");
  fs.writeFileSync(path.join(evidenceDir, logName), log);
  return {
    startedAt,
    exitCode: result.status,
    timedOut: Boolean(result.error && result.error.code === "ETIMEDOUT"),
    error: result.error?.message || null,
  };
}

const zapArgs = [
  "run",
  "--rm",
  "--add-host=host.docker.internal:host-gateway",
  "-v", `${evidenceDir.replaceAll("\\", "/")}:/zap/wrk`,
  "ghcr.io/zaproxy/zaproxy:stable",
  "zap-api-scan.py",
  "-t", "/zap/wrk/zap-final-openapi.json",
  "-f", "openapi",
  "-r", "zap-final-report.html",
  "-J", "zap-final-report.json",
  "-w", "zap-final-report.md",
  "-z", `-config api.disablekey=true -config scanner.threadPerHost=4 -config spider.maxDuration=${timeoutSeconds}`,
];

const activeScan = runZap(zapArgs, "zap-final-console.log");
const reportFiles = ["zap-final-report.html", "zap-final-report.json", "zap-final-report.md"]
  .filter((file) => fs.existsSync(path.join(evidenceDir, file)));

const status = activeScan.exitCode === 0 && reportFiles.length >= 2
  ? "PASS"
  : reportFiles.length > 0
    ? "LIMITED"
    : "LIMITED";

const output = {
  metadata: {
    executedAt: new Date().toISOString(),
    target,
    sourceOpenApiUrl,
    openApiFile: "zap-final-openapi.json",
    timeoutSeconds,
  },
  activeScan,
  reportFiles,
  status,
};
fs.writeFileSync(path.join(evidenceDir, "zap-final-summary.json"), JSON.stringify(output, null, 2));
fs.writeFileSync(path.join(evidenceDir, "zap-final-summary.md"), [
  "# ZAP Final DAST Summary",
  "",
  `- Executed: ${output.metadata.executedAt}`,
  `- Target: ${target}`,
  `- Source OpenAPI: ${sourceOpenApiUrl}`,
  `- Rewritten OpenAPI file: ${output.metadata.openApiFile}`,
  `- Timeout seconds: ${timeoutSeconds}`,
  `- Result: ${status}`,
  "",
  "| Evidence | Value |",
  "|---|---|",
  `| Exit code | ${activeScan.exitCode ?? "null"} |`,
  `| Timed out | ${activeScan.timedOut} |`,
  `| Report files | ${reportFiles.length ? reportFiles.join(", ") : "none"} |`,
  `| Console log | zap-final-console.log |`,
  "",
  "PASS requires the ZAP API scan to complete and emit machine-readable output. LIMITED keeps the run auditable but does not support a full DAST pass conclusion.",
  "",
].join("\n"));

console.log(JSON.stringify({ status, reportFiles }));
if (status === "FAIL") process.exitCode = 1;
