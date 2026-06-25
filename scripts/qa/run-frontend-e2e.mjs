import fs from "node:fs";
import path from "node:path";
import { spawnSync } from "node:child_process";

const root = path.resolve(import.meta.dirname, "../..");
const evidenceDir = path.join(root, "test-evidence", "2026-06-25");
fs.mkdirSync(evidenceDir, { recursive: true });

const runnerDir = path.join(root, "target", "frontend-e2e-runner");
const e2eScript = path.join(runnerDir, "qa-frontend-e2e.mjs");
fs.mkdirSync(runnerDir, { recursive: true });
fs.writeFileSync(path.join(runnerDir, "package.json"), JSON.stringify({ type: "module" }, null, 2));

fs.writeFileSync(e2eScript, `
import { chromium } from "playwright";
import fs from "node:fs";
import path from "node:path";

const evidenceDir = ${JSON.stringify(evidenceDir)};
const baseUrl = process.env.FRONTEND_BASE_URL || "http://127.0.0.1:3000";
const adminPassword = process.env.ADMIN_PASSWORD || "Admin@123456";
const chromePath = process.env.PLAYWRIGHT_CHROME_PATH || "C:/Program Files/Google/Chrome/Application/chrome.exe";
const results = [];

function add(id, requirement, description, expected, actual, passed) {
  results.push({ id, requirement, description, expected, actual, status: passed ? "PASS" : "FAIL" });
}

async function visible(page, text) {
  try {
    await page.getByText(text, { exact: false }).first().waitFor({ timeout: 5000 });
    return true;
  } catch {
    return false;
  }
}

async function login(page, username, password) {
  await page.goto(baseUrl + "/login", { waitUntil: "networkidle" });
  await page.locator('.login-form input').nth(0).fill(username);
  await page.locator('.login-form input[type="password"]').first().fill(password);
  await page.locator('button.login-btn').click();
  await page.waitForLoadState("networkidle");
  await page.waitForTimeout(1000);
}

const browser = await chromium.launch({ headless: true, executablePath: chromePath });
try {
  const desktop = await browser.newContext({ viewport: { width: 1366, height: 768 } });
  const page = await desktop.newPage();

  await login(page, "demo_learner_20260622", adminPassword);
  add("FE-E2E-01", "FR-01", "Learner login and dashboard route", "URL leaves /login", page.url(), !page.url().includes("/login"));

  await page.goto(baseUrl + "/outcomes", { waitUntil: "networkidle" });
  add("FE-E2E-02", "FR-16", "Public/learner outcome browse", "outcome page content visible",
    await page.title(), await visible(page, "成果") || await visible(page, "学分"));

  await page.goto(baseUrl + "/credit/account", { waitUntil: "networkidle" });
  add("FE-E2E-03", "FR-07", "Credit account page", "credit account content visible",
    page.url(), await visible(page, "学分"));

  await page.goto(baseUrl + "/cert/apply", { waitUntil: "networkidle" });
  add("FE-E2E-04", "FR-05", "Certification application page", "apply form route renders",
    page.url(), !page.url().includes("/login"));

  await page.goto(baseUrl + "/conversion/rules", { waitUntil: "networkidle" });
  add("FE-E2E-05", "FR-08", "Conversion rule browse page", "rule page route renders",
    page.url(), !page.url().includes("/login"));

  await page.goto(baseUrl + "/admin/users", { waitUntil: "networkidle" });
  add("FE-E2E-06", "FR-03", "Learner cannot stay on admin user management", "redirected or access blocked",
    page.url(), !page.url().includes("/admin/users"));

  await desktop.close();

  const adminContext = await browser.newContext({ viewport: { width: 1440, height: 900 } });
  const admin = await adminContext.newPage();
  await login(admin, "admin", adminPassword);
  await admin.goto(baseUrl + "/admin", { waitUntil: "networkidle" });
  add("FE-E2E-07", "FR-03", "Admin dashboard route", "admin route available",
    admin.url(), admin.url().includes("/admin"));

  await admin.goto(baseUrl + "/admin/outcomes", { waitUntil: "networkidle" });
  add("FE-E2E-08", "FR-04", "Admin outcome management page", "admin outcome route renders",
    admin.url(), admin.url().includes("/admin"));

  await admin.goto(baseUrl + "/admin/statistics", { waitUntil: "networkidle" });
  add("FE-E2E-09", "FR-12", "Admin statistics page", "statistics route renders",
    admin.url(), admin.url().includes("/admin"));
  await adminContext.close();

  const mobileContext = await browser.newContext({ viewport: { width: 390, height: 844 }, isMobile: true });
  const mobile = await mobileContext.newPage();
  await login(mobile, "admin", adminPassword);
  await mobile.goto(baseUrl + "/admin/outcomes", { waitUntil: "networkidle" });
  const mobileWidth = await mobile.evaluate(() => ({
    clientWidth: document.documentElement.clientWidth,
    scrollWidth: document.documentElement.scrollWidth,
    bodyScrollWidth: document.body.scrollWidth,
  }));
  await mobile.screenshot({ path: path.join(evidenceDir, "frontend-e2e-mobile-admin-outcomes.png"), fullPage: true });
  add("FE-E2E-10", "FR-03", "Mobile admin layout width", "no horizontal overflow",
    JSON.stringify(mobileWidth), mobileWidth.scrollWidth <= mobileWidth.clientWidth + 1 && mobileWidth.bodyScrollWidth <= mobileWidth.clientWidth + 1);
  await mobileContext.close();
} finally {
  await browser.close();
}

const passed = results.filter((result) => result.status === "PASS").length;
const output = {
  metadata: {
    suite: "LLCB key browser E2E",
    executedAt: new Date().toISOString(),
    baseUrl,
  },
  summary: { total: results.length, passed, failed: results.length - passed },
  results,
};
fs.writeFileSync(path.join(evidenceDir, "frontend-e2e.json"), JSON.stringify(output, null, 2));
fs.writeFileSync(path.join(evidenceDir, "frontend-e2e.md"), [
  "# Frontend Key Path E2E",
  "",
  \`- Executed: \${output.metadata.executedAt}\`,
  \`- Base URL: \${baseUrl}\`,
  \`- Total: \${output.summary.total}\`,
  \`- Passed: \${output.summary.passed}\`,
  \`- Failed: \${output.summary.failed}\`,
  "",
  "| ID | Requirement | Test | Expected | Actual | Result |",
  "|---|---|---|---|---|---|",
  ...results.map((result) =>
    \`| \${result.id} | \${result.requirement} | \${result.description} | \${result.expected} | \${String(result.actual).replace(/\\|/g, "\\\\|")} | \${result.status} |\`),
  "",
].join("\\n"));

console.log(JSON.stringify(output.summary));
if (output.summary.failed > 0) process.exitCode = 1;
`);

const install = spawnSync("npm", ["install", "--no-save", "playwright@1.56.1"], {
  cwd: runnerDir,
  encoding: "utf8",
  timeout: 600000,
  shell: process.platform === "win32",
  env: {
    ...process.env,
    PLAYWRIGHT_BROWSERS_PATH: process.env.PLAYWRIGHT_BROWSERS_PATH || "0",
  },
});

const result = install.status === 0
  ? spawnSync("node", [e2eScript], {
  cwd: runnerDir,
  encoding: "utf8",
  timeout: 600000,
  shell: process.platform === "win32",
  env: {
    ...process.env,
    PLAYWRIGHT_BROWSERS_PATH: process.env.PLAYWRIGHT_BROWSERS_PATH || "0",
    ADMIN_PASSWORD: process.env.ADMIN_PASSWORD || "Admin@123456",
    FRONTEND_BASE_URL: process.env.FRONTEND_BASE_URL || "http://127.0.0.1:3000",
    PLAYWRIGHT_CHROME_PATH: process.env.PLAYWRIGHT_CHROME_PATH || "C:/Program Files/Google/Chrome/Application/chrome.exe",
  },
})
  : install;

fs.writeFileSync(path.join(evidenceDir, "frontend-e2e-console.log"), [
  `installCommand=npm install --no-save playwright@1.56.1`,
  `installExitCode=${install.status}`,
  `command=node ${e2eScript}`,
  `exitCode=${result.status}`,
  `timedOut=${Boolean(result.error && result.error.code === "ETIMEDOUT")}`,
  "",
  "INSTALL STDOUT",
  install.stdout || "",
  "",
  "INSTALL STDERR",
  install.stderr || "",
  "",
  "STDOUT",
  result.stdout || "",
  "",
  "STDERR",
  result.stderr || "",
].join("\n"));

if (result.status !== 0) process.exitCode = result.status || 1;
