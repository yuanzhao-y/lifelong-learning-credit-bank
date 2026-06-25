import { execFileSync } from "node:child_process";
import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "../..");
const evidenceDir = path.join(root, "test-evidence", "2026-06-25");
fs.mkdirSync(evidenceDir, { recursive: true });

const job = process.env.JENKINS_JOB || "llcb-backend-ci";
const expectedCommit = process.env.EXPECTED_COMMIT || execFileSync("git", ["rev-parse", "HEAD"], {
  cwd: root,
  encoding: "utf8",
}).trim();

function dockerShell(command) {
  return execFileSync("docker", ["exec", "llcb-jenkins", "sh", "-lc", command], {
    encoding: "utf8",
  }).trim();
}

const buildsRoot = `/var/jenkins_home/jobs/${job}/builds`;
const buildNumbers = dockerShell(`find ${buildsRoot} -maxdepth 1 -type l -o -type d | sed 's#.*/##' | grep -E '^[0-9]+$' | sort -n | tail -5`)
  .split(/\r?\n/)
  .filter(Boolean);

const builds = buildNumbers.map((number) => {
  const base = `${buildsRoot}/${number}`;
  const log = dockerShell(`test -f ${base}/log && cat ${base}/log || true`);
  const buildXml = dockerShell(`test -f ${base}/build.xml && cat ${base}/build.xml || true`);
  const result = (buildXml.match(/<result>([^<]+)<\/result>/) || [null, "UNKNOWN"])[1];
  const duration = (buildXml.match(/<duration>([^<]+)<\/duration>/) || [null, "UNKNOWN"])[1];
  const checkoutMatch = log.match(/Checking out Revision\s+([0-9a-f]{40})/);
  const buildDataMatch = buildXml.match(/<marked>\s*<SHA1>([0-9a-f]{40})<\/SHA1>/);
  const archiveMatch = log.match(/Archiving artifacts[\s\S]*?(?:Recording fingerprints|Finished:)/);
  return {
    number,
    result,
    durationMilliseconds: duration,
    commit: checkoutMatch?.[1] || buildDataMatch?.[1] || null,
    hasCleanWorkspaceSignal: /Checkout|Checking out Revision/.test(log),
    hasBackendVerify: /mvn -B clean verify[\s\S]*Tests run:\s*111, Failures:\s*0, Errors:\s*0, Skipped:\s*0[\s\S]*check-core-coverage/.test(log),
    hasFrontendBuild: /npm ci[\s\S]*npm audit --audit-level=low[\s\S]*npm run build/.test(log),
    hasComposeCheck: /docker-compose config --quiet/.test(log),
    hasArchive: /Archiving artifacts|Recording fingerprints/.test(log),
    archiveExcerpt: archiveMatch?.[0]?.slice(0, 1000) || "",
  };
});

const expectedBuilds = builds.filter((build) => build.commit === expectedCommit && build.result === "SUCCESS");
const checks = {
  atLeastTwoRecentSuccessfulBuildsForExpectedCommit: expectedBuilds.length >= 2,
  allExpectedBuildsRanBackendVerify: expectedBuilds.every((build) => build.hasBackendVerify),
  allExpectedBuildsRanFrontendBuild: expectedBuilds.every((build) => build.hasFrontendBuild),
  allExpectedBuildsRanComposeCheck: expectedBuilds.every((build) => build.hasComposeCheck),
  allExpectedBuildsArchivedArtifacts: expectedBuilds.every((build) => build.hasArchive),
};

const output = {
  metadata: {
    executedAt: new Date().toISOString(),
    job,
    expectedCommit,
  },
  builds,
  matchingSuccessfulBuilds: expectedBuilds.map((build) => build.number),
  checks,
  passed: Object.values(checks).every(Boolean),
};

fs.writeFileSync(path.join(evidenceDir, "jenkins-stability.json"), JSON.stringify(output, null, 2));
fs.writeFileSync(path.join(evidenceDir, "jenkins-stability.md"), [
  "# Jenkins Stability Evidence",
  "",
  `- Executed: ${output.metadata.executedAt}`,
  `- Job: ${job}`,
  `- Expected commit: ${expectedCommit}`,
  `- Matching successful builds: ${output.matchingSuccessfulBuilds.length ? output.matchingSuccessfulBuilds.join(", ") : "none"}`,
  "",
  "| Build | Result | Commit | Backend verify | Frontend build | Compose check | Archive |",
  "|---|---|---|---|---|---|---|",
  ...builds.map((build) =>
    `| #${build.number} | ${build.result} | ${build.commit || "unknown"} | ${build.hasBackendVerify ? "yes" : "no"} | ${build.hasFrontendBuild ? "yes" : "no"} | ${build.hasComposeCheck ? "yes" : "no"} | ${build.hasArchive ? "yes" : "no"} |`),
  "",
  "| Check | Result |",
  "|---|---|",
  ...Object.entries(checks).map(([name, value]) => `| ${name} | ${value ? "PASS" : "FAIL"} |`),
  "",
  `Final result: **${output.passed ? "PASS" : "FAIL"}**`,
  "",
  "This evidence confirms repeated Jenkins success for the expected commit when the matching build count is at least two. A Jenkins process restart is recorded separately if performed.",
  "",
].join("\n"));

console.log(JSON.stringify({ matchingSuccessfulBuilds: output.matchingSuccessfulBuilds, passed: output.passed }));
if (!output.passed) process.exitCode = 1;
