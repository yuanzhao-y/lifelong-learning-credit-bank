# Jenkins Gate Closure Build

| Item | Actual value |
| --- | --- |
| Job | `llcb-backend-ci` |
| Build | `#9` |
| Result | SUCCESS |
| Started | 2026-06-25T14:29:28+08:00 |
| Duration | 37.139 s |
| Source commit | `30fcd870310826c87845b2e284ac34e72ec6e87c` |
| Branch | `refs/remotes/origin/develop` |
| Commit message | `test: close audit evidence gaps` |
| Java | OpenJDK 17.0.18 |
| Maven | Apache Maven 3.9.6 |
| Backend tests | 94 passed, 0 failed, 0 errors, 0 skipped |
| JaCoCo gate | `check-core-coverage` executed; analyzed 9 classes; all checks met |
| Frontend | `npm ci` and `npm run build` passed |
| Compose | `docker-compose config --quiet` passed; Jenkins binding remained `127.0.0.1:${JENKINS_PORT:-8081}:8080` |
| Archive | 356 files, approximately 67 MB |
| Fingerprints | Recorded by Jenkins |

Build #9 is the first remote CI run after the JaCoCo `check-core-coverage`
gate was committed to `pom.xml`. It closes the previous gap where Build #8
proved the pipeline before the executable coverage gate was added.

Extracted console evidence: `jenkins-build-9-summary.log`.
