# Post Review Verification

Executed: 2026-06-25T14:14:30+08:00

| Check | Result | Evidence |
| --- | --- | --- |
| Backend mvn verify | PASS | 94 tests, JaCoCo report, check-core-coverage analyzed 9 classes and passed |
| Core coverage gate | PASS | pom.xml execution check-core-coverage, line >= 80%, branch >= 55% |
| Frontend npm ci | PASS | 199 packages installed, 0 vulnerabilities during install audit |
| Frontend production build | PASS | Vite built 2,474 modules; large chunk and third-party pure annotation warnings retained as risk |
| Frontend npm audit --audit-level=low | PASS | 0 vulnerabilities |
| Evidence manifest verification | PASS | manifest.sha256 regenerated and locally verified |

Local backend Jar after post-review verification:

| Item | Value |
| --- | --- |
| File | target/lifelong-learning-credit-bank-0.1.0-SNAPSHOT.jar |
| SHA-256 | 364fdc4ce886da407c064581e8eddc6d46116de155549a2dff4d4fada3017254 |
| Size bytes | 60386718 |

This post-review verification is local. Jenkins Build #8 remains the final
remote CI evidence for commit 16bbf1ed9afc1265dc03001cf6a0fc96c83a800a; the
new JaCoCo gate will be enforced by the next Jenkins build because it is now in
pom.xml.
