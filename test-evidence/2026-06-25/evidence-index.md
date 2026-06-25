# Test Evidence Index

| Evidence | Purpose |
| --- | --- |
| `version-manifest.md` | Tested source, requirements, schema, artifact and image identities |
| `source-diff-2480-to-16bb.md` | Historical source equivalence evidence between functional/security testing commit and infrastructure CI commit |
| `source-diff-final-ci-to-report.md` | Reserved/final source diff evidence between final CI SHA and any report-only follow-up commit |
| `artifact-lineage.md` | Commit, Jar, image, frontend artifact and Jenkins archive relationship |
| `evidence-map.md` | Evidence IDs with paths, source, environment, SHA and Jenkins-generation status |
| `environment.md` | Host, toolchain, service exposure, image digest and data-scale context |
| `manifest.sha256` | SHA-256 manifest for the evidence package |
| `post-review-verification.md` | Local verification after report hardening and JaCoCo gate addition |
| `api-audit.json`, `api-audit-summary.md` | 58 API, security and single-node concurrency checks |
| `database-audit.txt` | Post-test consistency counts and index plans |
| `surefire/TEST-*.xml` | 29 test classes and 111 JUnit/Surefire cases |
| `jacoco.xml`, `jacoco.csv`, `coverage-summary.md` | Coverage measurements and scope explanations |
| `frontend-functional.md`, `frontend-admin-outcomes-mobile.png` | Historical browser functional and responsive verification |
| `frontend-e2e.md`, `frontend-e2e.json`, `frontend-e2e-mobile-admin-outcomes.png` | Key browser E2E paths and mobile screenshot |
| `frontend-dist-sha256.txt`, `npm-audit.json` | Frontend artifact identity and dependency audit |
| `dependency-security-final.md`, `dependency-security-final.json`, `maven-dependency-tree.txt` | Final dependency security check, Maven runtime tree and OSV limitation |
| `zap-final-openapi.json`, `zap-final-summary.*`, `zap-final-report.*`, `zap-final-console.log` | Final ZAP attempt using rewritten OpenAPI; 120 URLs imported, scan timed out |
| `performance-*.jtl`, `performance-*.log`, `performance-summary.*` | Historical 30-user long-run JMeter raw samples and aggregate metrics |
| `performance-stepped-*.jtl`, `performance-stepped-summary.*`, `performance-stepped-docker-stats.csv` | 30/60/100 user stepped local capacity baseline |
| `performance-docker-stats.csv` | Historical container CPU and memory samples during load |
| `recovery-audit.json`, `recovery-audit.md` | Container restart and volume persistence checks; not disaster recovery |
| `backup-restore-audit.json`, `backup-restore-audit.md` | Basic MySQL/Redis/Jenkins backup-restore audit |
| `multinode-concurrency.json`, `multinode-concurrency.md` | Local two-backend-instance concurrency audit |
| `zap-report.*` | First ZAP scan; retained as invalid-configuration evidence |
| `zap-api-corrected-timeout.log` | Corrected historical scan imported 120 URLs but exceeded its time limit |
| `osv-scan.json`, `dependency-security-summary.md` | Historical dependency and dynamic scanning limitations |
| `jenkins-build-7-summary.log`, `jenkins-build-7.md`, `jenkins-build-7-stage-view.png` | First complete remote Pipeline and page evidence |
| `jenkins-build-8-summary.log`, `jenkins-build-8.md` | Final infrastructure commit Pipeline result |
| `jenkins-build-9-summary.log`, `jenkins-build-9.md` | Remote CI gate closure after JaCoCo check was committed |
| `jenkins-stability.md`, `jenkins-stability.json` | To be generated after final SHA repeated Jenkins builds |

All evidence was checked for non-empty files where applicable. Tokens, AccessKey values, verification codes and real passwords are not included. Use `manifest.sha256` to verify file integrity when the evidence package is copied outside the repository.
