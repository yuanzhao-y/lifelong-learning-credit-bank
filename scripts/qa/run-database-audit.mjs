import { execFileSync } from "node:child_process";
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

const sql = `
SELECT 'users', COUNT(*) FROM sys_user WHERE deleted=0;
SELECT 'qa_users', COUNT(*) FROM sys_user WHERE deleted=0 AND username LIKE 'qa_%';
SELECT 'credit_flows', COUNT(*) FROM credit_flow WHERE deleted=0;
SELECT 'conversion_transactions', COUNT(*) FROM conversion_transaction WHERE deleted=0;
SELECT 'operation_logs', COUNT(*) FROM sys_operation_log WHERE deleted=0;
SELECT 'negative_credit_accounts', COUNT(*) FROM credit_account
 WHERE deleted=0 AND (balance < 0 OR frozen_credit < 0 OR total_earned < 0 OR total_deducted < 0);
SELECT 'duplicate_certification_outcomes', COUNT(*) FROM (
 SELECT source_application_id FROM learner_outcome
 WHERE deleted=0 AND source_type='certification'
 GROUP BY source_application_id HAVING COUNT(*) > 1
) duplicates;
SELECT 'duplicate_conversion_transactions', COUNT(*) FROM (
 SELECT application_id FROM conversion_transaction
 WHERE deleted=0 GROUP BY application_id HAVING COUNT(*) > 1
) duplicates;
SELECT 'stale_processing_certifications', COUNT(*) FROM cert_application
 WHERE deleted=0 AND status='processing' AND audited_at < DATE_SUB(NOW(), INTERVAL 10 MINUTE);
SELECT 'stale_processing_conversions', COUNT(*) FROM conversion_application
 WHERE deleted=0 AND status='processing' AND audited_at < DATE_SUB(NOW(), INTERVAL 10 MINUTE);
EXPLAIN SELECT * FROM cert_application WHERE applicant_id=1 ORDER BY submitted_at DESC LIMIT 10;
EXPLAIN SELECT * FROM conversion_application WHERE status='pending' ORDER BY submitted_at ASC LIMIT 10;
EXPLAIN SELECT * FROM credit_flow WHERE user_id=1 ORDER BY created_at DESC LIMIT 10;
`;

const output = execFileSync("docker", [
  "exec", "llcb-mysql", "mysql", "-ullcb", `-p${env.MYSQL_PASSWORD}`,
  "--batch", "--raw", "--skip-column-names", "llcb", "-e", sql,
], { encoding: "utf8" });

fs.writeFileSync(path.join(evidenceDir, "database-audit.txt"),
  `LLCB database audit\nExecuted: ${new Date().toISOString()}\n\n${output}`);
console.log(output);
