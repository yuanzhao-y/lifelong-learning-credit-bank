# Test Evidence Index

| Evidence | Purpose |
| --- | --- |
| `version-manifest.md` | Tested source, requirements, schema, artifact and image identities |
| `api-audit.json`, `api-audit-summary.md` | 58 API, security and concurrency checks |
| `database-audit.txt` | Post-test consistency counts and index plans |
| `surefire/TEST-*.xml` | 24 test classes and 94 JUnit cases |
| `jacoco.xml`, `jacoco.csv`, `coverage-summary.md` | Coverage measurements and scope |
| `frontend-functional.md`, `frontend-admin-outcomes-mobile.png` | Browser functional and responsive verification |
| `frontend-dist-sha256.txt`, `npm-audit.json` | Frontend artifact identity and dependency audit |
| `performance-*.jtl`, `performance-*.log`, `performance-summary.*` | JMeter raw samples and aggregate metrics |
| `performance-docker-stats.csv` | Container CPU and memory samples during load |
| `recovery-audit.json`, `recovery-audit.md` | Container restart and volume persistence checks |
| `zap-report.*` | First ZAP scan; retained as invalid-configuration evidence |
| `zap-api-corrected-timeout.log` | Corrected scan imported 120 URLs but exceeded its time limit |
| `osv-scan.json`, `dependency-security-summary.md` | Dependency and dynamic scanning limitations |
| `jenkins-build-7-summary.log`, `jenkins-build-7.md`, `jenkins-build-7-stage-view.png` | First complete remote Pipeline and page evidence |
| `jenkins-build-8-summary.log`, `jenkins-build-8.md` | Final infrastructure commit Pipeline result |

All evidence was checked for non-empty files where applicable. Tokens,
AccessKey values, verification codes and real passwords are not included.
