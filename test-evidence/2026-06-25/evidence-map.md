# Evidence Map

| Evidence ID | Scope | Source file/path | Produced by | Environment | SHA | Jenkins generated in latest CI |
| --- | --- | --- | --- | --- | --- | --- |
| EV-SRC-001 | Historical version inheritance | `source-diff-2480-to-16bb.md` | Git tree/blob comparison | Local repo | `2480b084...` to `16bbf1ed...` | No |
| EV-SRC-002 | Final CI/report diff | `source-diff-final-ci-to-report.md` | Git diff after final CI | Local repo | `b2291a4...` to post-CI evidence commit | No |
| EV-API-001 | API audit summary | `api-audit-summary.md` | `scripts/qa/run-api-audit.mjs` | Local Docker/Nginx | `2480b084...` | No |
| EV-API-002 | API audit raw result | `api-audit.json` | `scripts/qa/run-api-audit.mjs` | Local Docker/Nginx | `2480b084...` | No |
| EV-DB-001 | Database consistency | `database-audit.txt` | `scripts/qa/run-database-audit.mjs` | Local MySQL container | `2480b084...` | No |
| EV-UNIT-001 | JUnit/Surefire reports | `surefire/TEST-*.xml` | Maven Surefire | Local Maven | current working tree before final CI | No |
| EV-COV-001 | Coverage report | `jacoco.xml`, `jacoco.csv`, `coverage-summary.md` | JaCoCo | Local Maven | current working tree before final CI | No |
| EV-FE-001 | Historical browser functional summary | `frontend-functional.md` | Browser automation/manual verification | Local Chrome Headless | `2480b084...` | No |
| EV-FE-002 | Historical mobile screenshot | `frontend-admin-outcomes-mobile.png` | Browser screenshot | Local Chrome Headless | `2480b084...` | No |
| EV-FE-003 | Frontend artifact hashes | `frontend-dist-sha256.txt` | SHA-256 hashing | Local npm build | current working tree before final CI | No |
| EV-FE-004 | Key browser E2E | `frontend-e2e.md`, `frontend-e2e.json`, `frontend-e2e-mobile-admin-outcomes.png` | `scripts/qa/run-frontend-e2e.mjs` | Local Vite preview + Chrome | current working tree before final CI | No |
| EV-PERF-001 | Historical JMeter aggregate | `performance-summary.md`, `performance-summary.json` | Apache JMeter 5.6.3 | Local Docker/Nginx | `2480b084...` | No |
| EV-PERF-002 | Historical JMeter raw samples | `performance-*.jtl` | Apache JMeter 5.6.3 | Local Docker/Nginx | `2480b084...` | No |
| EV-PERF-003 | Historical container metrics | `performance-docker-stats.csv` | Docker stats sampling | Local Docker | `2480b084...` | No |
| EV-PERF-004 | Stepped local capacity baseline | `performance-stepped-summary.md`, `performance-stepped-summary.json`, `performance-stepped-*.jtl`, `performance-stepped-docker-stats.csv` | `scripts/qa/run-stepped-performance.mjs` | Local Docker/Nginx/JMeter | current working tree before final CI | No |
| EV-CONC-001 | Local multi-instance concurrency | `multinode-concurrency.md`, `multinode-concurrency.json` | `scripts/qa/run-multinode-concurrency.mjs` | Two local backend containers + shared MySQL/Redis | current working tree before final CI | No |
| EV-REC-001 | Restart/persistence audit | `recovery-audit.md`, `recovery-audit.json` | `scripts/qa/run-recovery-audit.mjs` | Local Docker Compose | `16bbf1ed...` | No |
| EV-REC-002 | Basic backup restore audit | `backup-restore-audit.md`, `backup-restore-audit.json` | `scripts/qa/run-backup-restore-audit.mjs` | Local Docker MySQL/Redis/Jenkins | current working tree before final CI | No |
| EV-SEC-001 | npm audit | `npm-audit.json` | npm audit | Local frontend toolchain | current working tree before final CI | No |
| EV-SEC-002 | Historical dependency/DAST limitations | `dependency-security-summary.md`, `osv-scan.json` | Dependency-Check/OSV/ZAP | Local/network-limited | `2480b084...` | No |
| EV-SEC-003 | Final dependency security check | `dependency-security-final.md`, `dependency-security-final.json`, `maven-dependency-tree.txt` | `scripts/qa/run-dependency-security.mjs` | Local toolchain with proxy default `127.0.0.1:6244` | current working tree before final CI | No |
| EV-ZAP-001 | Invalid first ZAP scan | `zap-report.md`, `zap-report.html`, `zap-report.json` | ZAP | Local ZAP | `2480b084...` | No |
| EV-ZAP-002 | Historical corrected ZAP timeout | `zap-api-corrected-timeout.log` | ZAP corrected import | Local ZAP | `2480b084...` | No |
| EV-ZAP-003 | Final ZAP attempt | `zap-final-summary.md`, `zap-final-summary.json`, `zap-final-report.*`, `zap-final-openapi.json`, `zap-final-console.log` | `scripts/qa/run-zap-final.mjs` | Local Docker ZAP | current working tree before final CI | No |
| EV-CI-007 | Jenkins Build #7 | `jenkins-build-7.md`, `jenkins-build-7-stage-view.png` | Jenkins pipeline | Jenkins container | `2480b084...` | Yes |
| EV-CI-008 | Jenkins Build #8 | `jenkins-build-8.md` | Jenkins pipeline | Jenkins container | `16bbf1ed...` | Yes |
| EV-CI-009 | Jenkins Build #9 | `jenkins-build-9.md`, `jenkins-build-9-summary.log` | Jenkins pipeline | Jenkins container | `30fcd870310826c87845b2e284ac34e72ec6e87c` | Yes |
| EV-CI-012 | Jenkins Build #12 checkout failure | `jenkins-build-12.md`, `jenkins-build-12-summary.log` | Jenkins pipeline | Jenkins container | `b2291a4f13a9911613919306ccaf7307fb270d4d` | Yes |
| EV-CI-013 | Jenkins Build #13 final SHA success | `jenkins-build-13.md`, `jenkins-build-13-summary.log` | Jenkins pipeline | Jenkins container | `b2291a4f13a9911613919306ccaf7307fb270d4d` | Yes |
| EV-CI-014 | Jenkins Build #14 final SHA success | `jenkins-build-14.md`, `jenkins-build-14-summary.log` | Jenkins pipeline | Jenkins container | `b2291a4f13a9911613919306ccaf7307fb270d4d` | Yes |
| EV-CI-STAB | Jenkins stability | `jenkins-stability.md`, `jenkins-stability.json` | `scripts/qa/collect-jenkins-stability.mjs` | Jenkins container | `b2291a4f13a9911613919306ccaf7307fb270d4d` | Yes |
| EV-LOCAL-001 | Post-review local gate | `post-review-verification.md` | Maven/npm verification | Local toolchain | local working tree after `16bbf1ed...` | No |
| EV-LINEAGE-001 | Artifact lineage | `artifact-lineage.md` | Hash/fingerprint consolidation | Local/Jenkins | mixed, stated per row | Mixed |
| EV-MANIFEST-001 | Evidence package manifest | `manifest.sha256` | SHA-256 hashing | Local filesystem | current evidence package | No |

## Requirement Traceability

| Requirement | Evidence IDs | Concrete cases or files |
| --- | --- | --- |
| FR-01 Login and registration | EV-API-001, EV-API-002, EV-UNIT-001, EV-FE-004 | `BB-01-01A/L/U/E`, `IT-AUTH-01/02/04`, `UT-AUTH-04-API`, `FE-E2E-01` |
| FR-02 User profile | EV-API-001, EV-UNIT-001 | `BB-02-01`, `BB-02-03`, `IT-SEC-03` |
| FR-03 Roles and authorization | EV-API-001, EV-FE-004 | `BB-03-01`, `IT-RBAC-02`, `FE-E2E-06`, `FE-E2E-07`, `FE-E2E-10` |
| FR-04 Outcome catalog | EV-API-001, EV-FE-004 | `BB-04-01`, `IT-CSV-01A/01B/02`, `BB-04-05`, `FE-E2E-08` |
| FR-05 Certification application | EV-API-001, EV-FE-004 | `BB-05-05`, `IT-FILE-01`, `IT-TOS-01`, `IT-CONC-03`, `FE-E2E-04` |
| FR-06 Certification audit | EV-API-001, EV-DB-001, EV-CONC-001 | `BB-06-01`, `IT-CONC-01`, `IT-IDEMP-01`, `MN-CONC-01` |
| FR-07 Credit account | EV-API-001, EV-DB-001, EV-FE-004 | `BB-07-01`, credit-flow consistency, `FE-E2E-03` |
| FR-08 Conversion rules | EV-API-001, EV-FE-004 | `BB-08-01`, rule review/effective-state checks, `FE-E2E-05` |
| FR-09 Conversion application | EV-API-001, EV-DB-001 | `BB-09-05`, conversion freeze and stale-processing checks |
| FR-10 Conversion audit | EV-API-001, EV-DB-001, EV-CONC-001 | `BB-10-01`, `IT-CONC-02`, `IT-IDEMP-02`, `MN-CONC-02` |
| FR-11 Expert review | EV-API-001 | `BB-11-03` |
| FR-12 Statistics | EV-API-001, EV-UNIT-001, EV-FE-004 | `BB-12-01`, `StatisticsServiceTest`, `FE-E2E-09` |
| FR-13 Messages | EV-API-001, EV-UNIT-001 | `BB-13-01`, `IT-SEC-02M`, `MessageServiceTest` |
| FR-14 Operation logs | EV-API-001, EV-DB-001 | `BB-14-01`, `IT-CSV-03`, operation log count |
| FR-15 Dictionary | EV-API-001, EV-UNIT-001 | `BB-15-01`, `DictCacheServiceTest` |
| FR-16 Public outcome browse | EV-API-001, EV-FE-001, EV-FE-002, EV-FE-004 | `BB-16-01`, outcome search/detail/certification navigation, `FE-E2E-02` |
| FR-17 Public conversion rules | EV-API-001 | `BB-17-01` |
| FR-18 Learner archive | EV-API-001 | `BB-18-01`, `BB-18-02`, `IT-SEC-02P` |
| FR-19 Announcements | EV-API-001, EV-UNIT-001 | `BB-19-01`, `IT-XSS-01/02`, `HtmlSecurityTest` |
| FR-20 Feedback | EV-API-001 | `BB-20-01` |

The requirement rows establish module-level evidence coverage. They do not claim complete acceptance-criteria coverage for every form control and business branch in each module.
