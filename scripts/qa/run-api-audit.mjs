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

const baseUrl = "http://127.0.0.1/api";
const directUrl = "http://127.0.0.1:8080/api";
const results = [];
const stamp = new Date().toISOString().replace(/\D/g, "").slice(0, 14);

function safeJson(text) {
  if (!text) return null;
  try {
    return JSON.parse(text.replace(/(:\s*)(\d{16,})(?=\s*[,}\]])/g, '$1"$2"'));
  } catch {
    return null;
  }
}

async function http(url, options = {}) {
  const headers = { ...(options.headers || {}) };
  if (options.token) headers.Authorization = `Bearer ${options.token}`;
  if (options.body !== undefined && !(options.body instanceof FormData)) {
    headers["Content-Type"] = "application/json";
  }
  const response = await fetch(url, {
    method: options.method || "GET",
    headers,
    body: options.body instanceof FormData
      ? options.body
      : options.body === undefined
        ? undefined
        : JSON.stringify(options.body),
  });
  const bytes = new Uint8Array(await response.arrayBuffer());
  const text = new TextDecoder().decode(bytes);
  return {
    status: response.status,
    headers: Object.fromEntries(response.headers.entries()),
    bytes,
    text,
    json: safeJson(text),
  };
}

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

async function check(id, requirement, description, pathName, options, expectedStatuses = [200]) {
  try {
    const response = await http(`${baseUrl}${pathName}`, options);
    const passed = expectedStatuses.includes(response.status);
    add(id, requirement, description, expectedStatuses.join("/"), response.status, passed,
      response.json?.message || "");
    return response;
  } catch (error) {
    add(id, requirement, description, expectedStatuses.join("/"), "network error", false, error.message);
    return { status: 0, headers: {}, bytes: new Uint8Array(), text: "", json: null };
  }
}

async function login(username, password, expectedRole, id) {
  const response = await http(`${baseUrl}/auth/login/password`, {
    method: "POST",
    body: { username, password },
  });
  const roles = response.json?.data?.roles || [];
  const passed = response.status === 200 && roles.includes(expectedRole) && response.json?.data?.token;
  add(id, "FR-01", `${expectedRole} password login`, "HTTP 200 and expected role",
    `HTTP ${response.status}; roles=${roles.join(",")}`, Boolean(passed));
  if (!passed) throw new Error(`Unable to login ${username}`);
  return response.json.data.token;
}

const directHealth = await http(`${directUrl}/actuator/health`);
add("IT-ENV-02", "ENV", "Direct backend health", "200/UP",
  `${directHealth.status}/${directHealth.json?.status}`, directHealth.status === 200 && directHealth.json?.status === "UP");
const proxyHealth = await http(`${baseUrl}/actuator/health`);
add("IT-ENV-03", "ENV", "Nginx health proxy", "200/UP",
  `${proxyHealth.status}/${proxyHealth.json?.status}`, proxyHealth.status === 200 && proxyHealth.json?.status === "UP");
await check("IT-ENV-04", "ENV", "OpenAPI document", "/v3/api-docs", {}, [200]);

const password = env.ADMIN_PASSWORD;
const admin = await login(env.ADMIN_USERNAME || "admin", password, "admin", "BB-01-01A");
const learner = await login("demo_learner_20260622", password, "learner", "BB-01-01L");
const auditor = await login("demo_auditor_20260622", password, "auditor", "BB-01-01U");
const expert = await login("demo_expert_20260622", password, "expert", "BB-01-01E");

await check("IT-AUTH-04", "FR-01", "Protected endpoint without token", "/users/me", {}, [401]);
await check("IT-RBAC-02", "FR-03", "Learner calls administrator API", "/admin/roles", { token: learner }, [403]);

const evilCors = await http(`${directUrl}/public/outcomes?page=1&size=1`, {
  headers: { Origin: "https://evil.example" },
});
add("IT-SEC-04A", "SEC", "Reject untrusted CORS origin", "403 and no ACAO",
  `${evilCors.status}; ACAO=${evilCors.headers["access-control-allow-origin"] || "none"}`,
  evilCors.status === 403 && !evilCors.headers["access-control-allow-origin"]);
add("IT-SEC-04B", "SEC", "Security response headers", "nosniff/DENY/no-referrer",
  `${proxyHealth.headers["x-content-type-options"]}/${proxyHealth.headers["x-frame-options"]}/${proxyHealth.headers["referrer-policy"]}`,
  proxyHealth.headers["x-content-type-options"]?.includes("nosniff")
    && proxyHealth.headers["x-frame-options"]?.includes("DENY")
    && proxyHealth.headers["referrer-policy"]?.includes("no-referrer"));

const routeChecks = [
  ["BB-02-01", "FR-02", "Learner profile", "/users/me", learner],
  ["BB-03-01", "FR-03", "Administrator role list", "/admin/roles", admin],
  ["BB-04-01", "FR-04", "Outcome catalog management list", "/outcomes?page=1&size=5", admin],
  ["BB-05-05", "FR-05", "Certification application history", "/certifications/mine?page=1&size=5", learner],
  ["BB-06-01", "FR-06", "Certification audit queue", "/certifications/audit/pending?page=1&size=5", auditor],
  ["BB-07-01", "FR-07", "Credit account", "/credits/account", learner],
  ["BB-08-01", "FR-08", "Conversion rule management", "/conversion-rules?page=1&size=5", admin],
  ["BB-09-05", "FR-09", "Conversion application history", "/conversions/mine?page=1&size=5", learner],
  ["BB-10-01", "FR-10", "Conversion audit queue", "/conversions/audit/pending?page=1&size=5", auditor],
  ["BB-11-03", "FR-11", "Expert review queue", "/experts/reviews?page=1&size=5", expert],
  ["BB-12-01", "FR-12", "Statistics overview", "/admin/statistics/overview", admin],
  ["BB-13-01", "FR-13", "Learner messages", "/messages?page=1&size=5", learner],
  ["BB-14-01", "FR-14", "Operation logs", "/admin/operation-logs?page=1&size=5", admin],
  ["BB-15-01", "FR-15", "Dictionary management", "/admin/dicts", admin],
  ["BB-16-01", "FR-16", "Public outcome browse", "/public/outcomes?page=1&size=5", null],
  ["BB-17-01", "FR-17", "Public conversion rules", "/public/conversion-rules?page=1&size=5", null],
  ["BB-18-01", "FR-18", "Learner education archive", "/profile/educations", learner],
  ["BB-19-01", "FR-19", "Public announcements", "/public/announcements?page=1&size=5", null],
  ["BB-20-01", "FR-20", "Learner feedback history", "/feedback/mine?page=1&size=5", learner],
];
for (const [id, requirement, description, route, token] of routeChecks) {
  await check(id, requirement, description, route, token ? { token } : {}, [200]);
}

const injection = await check("SEC-INJECT-01", "SEC", "SQL injection-shaped search input",
  `/public/outcomes?page=1&size=5&keyword=${encodeURIComponent("' OR 1=1 --")}`, {}, [200]);
add("SEC-INJECT-01B", "SEC", "Search result remains paginated", "records <= 5",
  injection.json?.data?.records?.length ?? "unknown",
  (injection.json?.data?.records?.length ?? 99) <= 5);

const weakUser = `qa_weak_${stamp}`;
await check("UT-AUTH-04-API", "FR-01", "Weak password registration", "/auth/register", {
  method: "POST",
  body: { username: weakUser, password: "password", phone: `130${stamp.slice(-8)}` },
}, [400]);

const rateUser = `qa_missing_${stamp}`;
const rateStatuses = [];
for (let index = 0; index < 6; index += 1) {
  rateStatuses.push((await http(`${baseUrl}/auth/login/password`, {
    method: "POST",
    body: { username: rateUser, password: "Wrong@123" },
  })).status);
}
add("IT-SEC-LOGIN-RATE", "SEC", "Login brute-force throttling", "five 400 then 429",
  rateStatuses.join(","), rateStatuses.slice(0, 5).every((status) => status === 400) && rateStatuses[5] === 429);

const smsPhone = `131${stamp.slice(-8)}`;
const firstSms = await http(`${baseUrl}/auth/sms-code`, {
  method: "POST",
  body: { phone: smsPhone, scene: "login" },
});
const secondSms = await http(`${baseUrl}/auth/sms-code`, {
  method: "POST",
  body: { phone: smsPhone, scene: "login" },
});
add("IT-SEC-SMS-RATE", "SEC", "SMS code request throttling", "200 then 429",
  `${firstSms.status},${secondSms.status}`, firstSms.status === 200 && secondSms.status === 429);

const qaUsername = `qa_security_${stamp}`;
const qaOldPassword = "QaUser@123456";
const qaNewPassword = "QaUser@654321";
const qaPhone = `132${stamp.slice(-8)}`;
const registration = await check("IT-AUTH-01", "FR-01", "Create isolated QA learner", "/auth/register", {
  method: "POST",
  body: { username: qaUsername, password: qaOldPassword, realName: "QA Security", phone: qaPhone },
}, [200]);
let qaToken = null;
if (registration.status === 200) {
  qaToken = await login(qaUsername, qaOldPassword, "learner", "IT-AUTH-02");
  const changed = await check("BB-02-03", "FR-02", "Change password", "/users/me/password", {
    method: "PUT",
    token: qaToken,
    body: { oldPassword: qaOldPassword, newPassword: qaNewPassword },
  }, [200]);
  if (changed.status === 200) {
    await check("IT-SEC-03", "SEC", "Old JWT after password change", "/users/me", { token: qaToken }, [401]);
    qaToken = await login(qaUsername, qaNewPassword, "learner", "IT-AUTH-02B");
  }
}

const education = await check("BB-18-02", "FR-18", "Create education experience", "/profile/educations", {
  method: "POST",
  token: learner,
  body: { schoolName: `QA University ${stamp}`, major: "Software Engineering" },
}, [200]);
if (education.json?.data?.id && qaToken) {
  await check("IT-SEC-02P", "SEC", "Horizontal update of education experience",
    `/profile/educations/${education.json.data.id}`, {
      method: "PUT",
      token: qaToken,
      body: { schoolName: "Unauthorized update" },
    }, [404]);
}

const expertMessages = await http(`${baseUrl}/messages?page=1&size=5`, { token: expert });
const expertMessageId = expertMessages.json?.data?.records?.[0]?.messageId;
if (expertMessageId) {
  await check("IT-SEC-02M", "SEC", "Horizontal read of another user's message",
    `/messages/${expertMessageId}`, { token: learner }, [404]);
} else {
  add("IT-SEC-02M", "SEC", "Horizontal read of another user's message",
    "expert message fixture exists", "no expert message fixture", false);
}

const xssAnnouncement = await check("IT-XSS-01", "FR-19", "Create announcement containing unsafe HTML",
  "/admin/announcements", {
    method: "POST",
    token: admin,
    body: {
      title: `QA XSS ${stamp}`,
      content: '<p onclick="steal()">safe</p><script>alert(1)</script>',
      status: "enabled",
    },
  }, [200]);
if (xssAnnouncement.json?.data?.id) {
  const publicAnnouncement = await http(
    `${baseUrl}/public/announcements/${xssAnnouncement.json.data.id}`,
  );
  const content = publicAnnouncement.json?.data?.content || "";
  add("IT-XSS-02", "SEC", "Public announcement HTML is sanitized",
    "safe paragraph without script/event handler", content,
    publicAnnouncement.status === 200 && content.includes("<p>safe</p>")
      && !content.includes("script") && !content.includes("onclick"));
  await http(`${baseUrl}/admin/announcements/${xssAnnouncement.json.data.id}`, {
    method: "DELETE",
    token: admin,
  });
}

const csvCode = `QA-AUDIT-${stamp}`;
const csvContent = `\uFEFFoutcome_code,outcome_name,outcome_type,base_credit,status\r\n`
  + `${csvCode},"QA, quoted\nname",course_cert,2.50,enabled\r\n`
  + `=FORMULA,Unsafe,course_cert,1.00,enabled\r\n`;
async function importCsv(content, filename) {
  const form = new FormData();
  form.append("file", new Blob([content], { type: "text/csv" }), filename);
  return http(`${baseUrl}/outcomes/import`, { method: "POST", token: admin, body: form });
}
const firstImport = await importCsv(csvContent, "qa-import.csv");
const firstImportData = firstImport.json?.data;
add("IT-CSV-01A", "FR-04", "CSV BOM/quoted newline/invalid formula row",
  "created=1 skipped=1", `HTTP ${firstImport.status}; created=${firstImportData?.createdCount}; skipped=${firstImportData?.skippedCount}`,
  firstImport.status === 200 && firstImportData?.createdCount === 1 && firstImportData?.skippedCount === 1);
const secondImport = await importCsv(csvContent, "qa-import-repeat.csv");
add("IT-CSV-01B", "FR-04", "Repeated outcome code updates existing row",
  "updated=1 skipped=1",
  `HTTP ${secondImport.status}; updated=${secondImport.json?.data?.updatedCount}; skipped=${secondImport.json?.data?.skippedCount}`,
  secondImport.status === 200 && secondImport.json?.data?.updatedCount === 1
    && secondImport.json?.data?.skippedCount === 1);

let oversizedCsv = "outcome_code,outcome_name,outcome_type,base_credit,status\n";
for (let index = 0; index < 10001; index += 1) {
  oversizedCsv += `QA-LIMIT-${stamp}-${index},Limit ${index},course_cert,1.00,enabled\n`;
}
const oversizedImport = await importCsv(oversizedCsv, "qa-too-many-rows.csv");
add("IT-CSV-02", "FR-04", "CSV row limit", "HTTP 400", oversizedImport.status,
  oversizedImport.status === 400);

const outcomeExport = await http(`${baseUrl}/outcomes/export`, { token: admin });
add("BB-04-05", "FR-04", "Outcome catalog export", "CSV attachment with data",
  `HTTP ${outcomeExport.status}; bytes=${outcomeExport.bytes.length}`,
  outcomeExport.status === 200 && outcomeExport.bytes.length > 100
    && (outcomeExport.headers["content-disposition"] || "").includes("outcome_catalog.csv"));
const logExport = await http(`${baseUrl}/admin/operation-logs/export?operationType=importCsv`, { token: admin });
add("IT-CSV-03", "FR-14", "Filtered operation log export", "UTF-8 BOM CSV attachment",
  `HTTP ${logExport.status}; bytes=${logExport.bytes.length}`,
  logExport.status === 200 && logExport.bytes[0] === 0xef
    && logExport.bytes[1] === 0xbb && logExport.bytes[2] === 0xbf);

const invalidFile = new FormData();
invalidFile.append("file", new Blob(["payload"], { type: "text/html" }), "../payload.exe");
invalidFile.append("bizType", "qa_security");
const invalidUpload = await http(`${baseUrl}/files/upload`, {
  method: "POST",
  token: learner,
  body: invalidFile,
});
add("IT-FILE-01", "FR-05", "Reject forged upload type and extension", "HTTP 400",
  invalidUpload.status, invalidUpload.status === 400);

const validFile = new FormData();
validFile.append("file", new Blob([`qa evidence ${stamp}`], { type: "text/plain" }), "qa-evidence.txt");
validFile.append("bizType", "qa_evidence");
const validUpload = await http(`${baseUrl}/files/upload`, {
  method: "POST",
  token: learner,
  body: validFile,
});
let objectReachable = false;
if (validUpload.status === 200 && validUpload.json?.data?.fileUrl) {
  const objectResponse = await fetch(validUpload.json.data.fileUrl, { method: "GET" });
  objectReachable = objectResponse.status === 200;
}
add("IT-TOS-01", "FR-05", "Valid TOS upload and object read",
  "upload 200 and object 200", `upload=${validUpload.status}; objectReachable=${objectReachable}`,
  validUpload.status === 200 && objectReachable);

const publicOutcomes = await http(`${baseUrl}/public/outcomes?page=1&size=1`);
const catalogId = publicOutcomes.json?.data?.records?.[0]?.id;
const certSubmit = await http(`${baseUrl}/certifications`, {
  method: "POST",
  token: learner,
  body: {
    catalogId,
    certifyType: "course_cert",
    outcomeName: `QA concurrent certification ${stamp}`,
    certificateNo: `QA-CERT-${stamp}`,
    issuingAuthority: "QA Institute",
    requestedCredit: 1,
    materialFileIds: [],
  },
});
const certId = certSubmit.json?.data?.id;
if (certSubmit.status === 200 && certId) {
  if (qaToken) {
    await check("IT-SEC-02C", "SEC", "Horizontal read of certification audit trail",
      `/certifications/${certId}/audit-records`, { token: qaToken }, [404]);
  }
  const approvals = await Promise.all(Array.from({ length: 10 }, () =>
    http(`${baseUrl}/certifications/audit/${certId}/approve`, {
      method: "POST",
      token: auditor,
      body: { recognizedCredit: 1 },
    })));
  const successCount = approvals.filter((response) => response.status === 200).length;
  const conflictCount = approvals.filter((response) => response.status === 400).length;
  add("IT-CONC-01", "FR-06", "Ten concurrent certification approvals",
    "one success and nine rejected", `success=${successCount}; rejected=${conflictCount}`,
    successCount === 1 && conflictCount === 9);
  const duplicateApproval = await http(`${baseUrl}/certifications/audit/${certId}/approve`, {
    method: "POST",
    token: auditor,
    body: { recognizedCredit: 1 },
  });
  add("IT-IDEMP-01", "FR-06", "Repeated certification approval", "HTTP 400",
    duplicateApproval.status, duplicateApproval.status === 400);
}

const raceCert = await http(`${baseUrl}/certifications`, {
  method: "POST",
  token: learner,
  body: {
    catalogId,
    certifyType: "course_cert",
    outcomeName: `QA withdraw race ${stamp}`,
    certificateNo: `QA-RACE-${stamp}`,
    issuingAuthority: "QA Institute",
    requestedCredit: 1,
    materialFileIds: [],
  },
});
if (raceCert.status === 200 && raceCert.json?.data?.id) {
  const raceId = raceCert.json.data.id;
  const [withdraw, approve] = await Promise.all([
    http(`${baseUrl}/certifications/${raceId}/withdraw`, { method: "POST", token: learner }),
    http(`${baseUrl}/certifications/audit/${raceId}/approve`, {
      method: "POST", token: auditor, body: { recognizedCredit: 1 },
    }),
  ]);
  const successes = [withdraw.status, approve.status].filter((status) => status === 200).length;
  add("IT-CONC-03", "FR-06", "Withdraw versus approval race", "exactly one succeeds",
    `${withdraw.status},${approve.status}`, successes === 1);
}

const learnerOutcomes = await http(`${baseUrl}/learner-outcomes/mine?page=1&size=100`, { token: learner });
let conversionFixture = null;
for (const outcome of learnerOutcomes.json?.data?.records || []) {
  if (Number(outcome.availableCredit) < 1) continue;
  const matches = await http(
    `${baseUrl}/conversions/match-rules?sourceOutcomeId=${outcome.id}`,
    { token: learner },
  );
  if (matches.status === 200 && matches.json?.data?.length) {
    conversionFixture = { outcome, rule: matches.json.data[0] };
    break;
  }
}
if (conversionFixture) {
  const conversionSubmit = await http(`${baseUrl}/conversions`, {
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
    const approvals = await Promise.all(Array.from({ length: 10 }, () =>
      http(`${baseUrl}/conversions/audit/${conversionId}/approve`, {
        method: "POST",
        token: auditor,
      })));
    const successCount = approvals.filter((response) => response.status === 200).length;
    const conflictCount = approvals.filter((response) => response.status === 400).length;
    add("IT-CONC-02", "FR-10", "Ten concurrent conversion approvals",
      "one success and nine rejected", `success=${successCount}; rejected=${conflictCount}`,
      successCount === 1 && conflictCount === 9);
    const duplicateApproval = await http(`${baseUrl}/conversions/audit/${conversionId}/approve`, {
      method: "POST",
      token: auditor,
    });
    add("IT-IDEMP-02", "FR-10", "Repeated conversion approval", "HTTP 400",
      duplicateApproval.status, duplicateApproval.status === 400);
  } else {
    add("IT-CONC-02", "FR-10", "Ten concurrent conversion approvals",
      "conversion fixture submission succeeds", `HTTP ${conversionSubmit.status}`, false,
      conversionSubmit.json?.message || "");
  }
} else {
  add("IT-CONC-02", "FR-10", "Ten concurrent conversion approvals",
    "matching rule and source outcome exist", "fixture unavailable", false);
}

const passed = results.filter((result) => result.status === "PASS").length;
const failed = results.length - passed;
const output = {
  metadata: {
    suite: "LLCB API integration, security and concurrency audit",
    executedAt: new Date().toISOString(),
    baseUrl,
    testDataPrefix: "qa_",
  },
  summary: { total: results.length, passed, failed },
  results,
};
fs.writeFileSync(path.join(evidenceDir, "api-audit.json"), JSON.stringify(output, null, 2));

const markdown = [
  "# API Audit Summary",
  "",
  `- Executed: ${output.metadata.executedAt}`,
  `- Total: ${results.length}`,
  `- Passed: ${passed}`,
  `- Failed: ${failed}`,
  "",
  "| ID | Requirement | Test | Expected | Actual | Result |",
  "|---|---|---|---|---|---|",
  ...results.map((result) =>
    `| ${result.id} | ${result.requirement} | ${result.description} | ${result.expected} | ${String(result.actual).replace(/\|/g, "\\|")} | ${result.status} |`),
  "",
].join("\n");
fs.writeFileSync(path.join(evidenceDir, "api-audit-summary.md"), markdown);

console.log(JSON.stringify(output.summary));
if (failed > 0) process.exitCode = 1;
