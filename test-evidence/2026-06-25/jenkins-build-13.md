# Jenkins Final SHA Build 13

| Item | Actual value |
| --- | --- |
| Job | `llcb-backend-ci` |
| Build | `#13` |
| Result | SUCCESS |
| Started | 2026-06-25T16:46:42+08:00 |
| Duration | 38.255 s |
| Source commit | `b2291a4f13a9911613919306ccaf7307fb270d4d` |
| Branch | `refs/remotes/origin/develop` |
| Commit message | `test: improve Jenkins stability evidence parsing` |
| Backend tests | 111 passed, 0 failed, 0 errors, 0 skipped |
| JaCoCo gate | `check-core-coverage` executed |
| Frontend | `npm ci`, `npm audit --audit-level=low`, `npm run build` passed |
| Compose | `docker-compose config --quiet` passed |
| Archive | 405 files, approximately 72 MB |
| Archived Jar SHA-256 | `8ef711f4f7ebfd6bb7ba1536f2442bfcd9f53cd248e51f6c776297dc8c370a60` |
| Fingerprints | Recorded by Jenkins |

Build #13 is the first successful Jenkins build for the final CI commit in this
hardening pass.

Extracted console evidence: `jenkins-build-13-summary.log`.
