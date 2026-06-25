# Final CI To Report Source Diff Evidence

This file records the final CI/report alignment step.

| Item | Value |
| --- | --- |
| Final CI commit | `b2291a4f13a9911613919306ccaf7307fb270d4d` |
| Final successful Jenkins builds | `#13`, `#14` |
| Final report commit | The post-CI evidence/report commit containing this file |
| Diff command | `git diff --name-status b2291a4f13a9911613919306ccaf7307fb270d4d..HEAD` before committing this evidence update |
| Purpose | Prove that post-CI edits are documentation/evidence-only and product source did not change |

## Observed Post-CI Change Scope

Jenkins Build #13 and Build #14 succeeded on `b2291a4f13a9911613919306ccaf7307fb270d4d`. After those builds, only report and evidence files were updated to record actual build numbers, logs, SHA values and manifest hashes.

No backend product source (`src/main/java`), frontend product source (`credit-bank-frontend/src`), database schema (`src/main/resources/db/schema.sql`), `pom.xml`, `package-lock.json` or `Jenkinsfile` changed after the final CI build.

Expected changed path categories in the post-CI report commit:

- `测试交付.md`
- `测试结果.md`
- `test-evidence/2026-06-25/*.md`
- `test-evidence/2026-06-25/jenkins-build-*-summary.log`
- `test-evidence/2026-06-25/jenkins-stability.json`
- `test-evidence/2026-06-25/manifest.sha256`
