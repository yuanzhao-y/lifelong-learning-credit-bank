# Jenkins Final SHA Build 14

| Item | Actual value |
| --- | --- |
| Job | `llcb-backend-ci` |
| Build | `#14` |
| Result | SUCCESS |
| Started | 2026-06-25T16:47:42+08:00 |
| Duration | 37.349 s |
| Source commit | `b2291a4f13a9911613919306ccaf7307fb270d4d` |
| Branch | `refs/remotes/origin/develop` |
| Commit message | `test: improve Jenkins stability evidence parsing` |
| Backend tests | 111 passed, 0 failed, 0 errors, 0 skipped |
| JaCoCo gate | `check-core-coverage` executed |
| Frontend | `npm ci`, `npm audit --audit-level=low`, `npm run build` passed |
| Compose | `docker-compose config --quiet` passed |
| Archive | 405 files, approximately 72 MB |
| Archived Jar SHA-256 | `4b02ca52e881f6ef7c291c87ea7cd9e2c9b8fb2ba5d41a5a5a7e6dcd5a956d6e` |
| Fingerprints | Recorded by Jenkins |

Build #14 is the second consecutive successful Jenkins build for the final CI
commit in this hardening pass.

Extracted console evidence: `jenkins-build-14-summary.log`.
