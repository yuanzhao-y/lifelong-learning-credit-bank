import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "../..");
const evidenceDir = path.join(root, "test-evidence", "2026-06-25");
fs.mkdirSync(evidenceDir, { recursive: true });

const env = Object.fromEntries(
  fs.readFileSync(path.join(root, ".env"), "utf8")
    .split(/\r?\n/)
    .filter((line) => line && !line.startsWith("#") && line.includes("="))
    .map((line) => {
      const index = line.indexOf("=");
      return [line.slice(0, index), line.slice(index + 1)];
    }),
);

const baseUrl = process.env.QA_BASE_URL || "http://127.0.0.1/api";
const directUrls = (process.env.QA_BACKEND_URLS || "http://127.0.0.1:8080/api")
  .split(",")
  .map((url) => url.trim())
  .filter(Boolean);
const results = [];
let nodeIndex = 0;

function add(id, requirement, description, expected, actual, passed, details = "") {
  results.push({
    id,
    requirement,
    description,
    expected,
    actual,
    status: passed ? "PASS" : "FAIL",
    details,
    executedAt: new Date().toISOString(),
  });
}

function safeJson(text) {
  try {
    return JSON.parse(text.replace(/(:\s*)(\d{16,})(?=\s*[,}\]])/g, '$1"$2"'));
  } catch {
    return null;
  }
}

async function http(pathName, options = {}) {
  const headers = { ...(options.headers || {}) };
  if (options.token) headers.Authorization = `Bearer ${options.token}`;
  if (options.body !== undefined) headers["Content-Type"] = "application/json";
  const url = options.direct
    ? `${directUrls[(nodeIndex += 1) % directUrls.length]}${pathName}`
    : `${baseUrl}${pathName}`;
  const response = await fetch(url, {
    method: options.method || "GET",
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });
  const text = await response.text();
  return {
    url,
    status: response.status,
    headers: Object.fromEntries(response.headers.entries()),
    text,
    json: safeJson(text),
  };
}

async function login(username, password, expectedRole, id) {
  const response = await http("/auth/login/password", {
    method: "POST",
    body: { username, password },
  });
  const roles = response.json?.data?.roles || [];
  const passed = response.status === 200 && roles.includes(expectedRole) && response.json?.data?.token;
  add(id, "FR-01", `${expectedRole} login for multinode audit`, "HTTP 200 and token",
    `HTTP ${response.status}; roles=${roles.join(",")}`, Boolean(passed));
  if (!passed) throw new Error(`Unable to login ${username}`);
  return response.json.data.token;
}

const password = env.ADMIN_PASSWORD;
const learner = await login("demo_learner_20260622", password, "learner", "MN-AUTH-L");
const auditor = await login("demo_auditor_20260622", password, "auditor", "MN-AUTH-A");

const healthChecks = await Promise.all(directUrls.map(async (url) => {
  try {
    const response = await fetch(`${url}/actuator/health`);
    const json = await response.json();
    return { url, status: response.status, health: json.status };
  } catch (error) {
    return { url, error: error.message };
  }
}));
add("MN-ENV-01", "ENV", "All configured backend nodes are reachable",
  "every node returns 200/UP", JSON.stringify(healthChecks),
  directUrls.length >= 2 && healthChecks.every((item) => item.status === 200 && item.health === "UP"));

const publicOutcomes = await http("/public/outcomes?page=1&size=1");
const catalogId = publicOutcomes.json?.data?.records?.[0]?.id;
const certSubmit = await http("/certifications", {
  method: "POST",
  token: learner,
  body: {
    catalogId,
    certifyType: "course_cert",
    outcomeName: `QA multinode certification ${Date.now()}`,
    certificateNo: `QA-MN-CERT-${Date.now()}`,
    issuingAuthority: "QA Institute",
    requestedCredit: 1,
    materialFileIds: [],
  },
});
const certId = certSubmit.json?.data?.id;
if (certSubmit.status === 200 && certId) {
  const approvals = await Promise.all(Array.from({ length: 12 }, () =>
    http(`/certifications/audit/${certId}/approve`, {
      method: "POST",
      token: auditor,
      direct: true,
      body: { recognizedCredit: 1 },
    })));
  const successCount = approvals.filter((response) => response.status === 200).length;
  const rejectedCount = approvals.filter((response) => response.status === 400).length;
  const nodeHits = [...new Set(approvals.map((response) => response.url.split("/api")[0]))];
  add("MN-CONC-01", "FR-06", "Certification approval race across configured nodes",
    "one success, remaining business rejected, at least two target nodes used",
    `success=${successCount}; rejected=${rejectedCount}; nodes=${nodeHits.join(",")}`,
    successCount === 1 && rejectedCount === 11 && nodeHits.length >= 2);
} else {
  add("MN-CONC-01", "FR-06", "Certification approval race across configured nodes",
    "certification fixture submission succeeds", `HTTP ${certSubmit.status}`, false,
    certSubmit.json?.message || "");
}

const learnerOutcomes = await http("/learner-outcomes/mine?page=1&size=100", { token: learner });
let conversionFixture = null;
for (const outcome of learnerOutcomes.json?.data?.records || []) {
  if (Number(outcome.availableCredit) < 1) continue;
  const matches = await http(`/conversions/match-rules?sourceOutcomeId=${outcome.id}`, { token: learner });
  if (matches.status === 200 && matches.json?.data?.length) {
    conversionFixture = { outcome, rule: matches.json.data[0] };
    break;
  }
}
if (conversionFixture) {
  const conversionSubmit = await http("/conversions", {
    method: "POST",
    token: learner,
    body: {
      ruleId: conversionFixture.rule.id,
      sourceOutcomeId: conversionFixture.outcome.id,
      sourceCredit: 1,
    },
  });
  const conversionId = conversionSubmit.json?.data?.id;
  if (conversionSubmit.status === 200 && conversionId) {
    const approvals = await Promise.all(Array.from({ length: 12 }, () =>
      http(`/conversions/audit/${conversionId}/approve`, {
        method: "POST",
        token: auditor,
        direct: true,
      })));
    const successCount = approvals.filter((response) => response.status === 200).length;
    const rejectedCount = approvals.filter((response) => response.status === 400).length;
    const nodeHits = [...new Set(approvals.map((response) => response.url.split("/api")[0]))];
    add("MN-CONC-02", "FR-10", "Conversion approval race across configured nodes",
      "one success, remaining business rejected, at least two target nodes used",
      `success=${successCount}; rejected=${rejectedCount}; nodes=${nodeHits.join(",")}`,
      successCount === 1 && rejectedCount === 11 && nodeHits.length >= 2);
  } else {
    add("MN-CONC-02", "FR-10", "Conversion approval race across configured nodes",
      "conversion fixture submission succeeds", `HTTP ${conversionSubmit.status}`, false,
      conversionSubmit.json?.message || "");
  }
} else {
  add("MN-CONC-02", "FR-10", "Conversion approval race across configured nodes",
    "matching conversion fixture exists", "fixture unavailable", false);
}

const passed = results.filter((result) => result.status === "PASS").length;
const output = {
  metadata: {
    suite: "LLCB multi-node concurrency audit",
    executedAt: new Date().toISOString(),
    baseUrl,
    backendUrls: directUrls,
  },
  summary: { total: results.length, passed, failed: results.length - passed },
  results,
};

fs.writeFileSync(path.join(evidenceDir, "multinode-concurrency.json"), JSON.stringify(output, null, 2));
fs.writeFileSync(path.join(evidenceDir, "multinode-concurrency.md"), [
  "# Multi-node Concurrency Audit",
  "",
  `- Executed: ${output.metadata.executedAt}`,
  `- Gateway URL: ${baseUrl}`,
  `- Backend node URLs: ${directUrls.join(", ")}`,
  `- Total: ${output.summary.total}`,
  `- Passed: ${output.summary.passed}`,
  `- Failed: ${output.summary.failed}`,
  "",
  "| ID | Requirement | Test | Expected | Actual | Result |",
  "|---|---|---|---|---|---|",
  ...results.map((result) =>
    `| ${result.id} | ${result.requirement} | ${result.description} | ${result.expected} | ${String(result.actual).replace(/\|/g, "\\|")} | ${result.status} |`),
  "",
  "This audit proves only the configured local node set. It is not a production cluster capacity or failover certification.",
  "",
].join("\n"));

console.log(JSON.stringify(output.summary));
if (output.summary.failed > 0) process.exitCode = 1;
