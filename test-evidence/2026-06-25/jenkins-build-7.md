# Jenkins Remote Pipeline Evidence

| Item | Actual value |
| --- | --- |
| Jenkins | `2.541.3-lts-jdk17` official image |
| Job | `llcb-backend-ci` |
| SCM | `https://github.com/yuanzhao-y/lifelong-learning-credit-bank` |
| Branch | `develop` |
| Build | `#7` |
| Source commit | `2480b084bc99813cdcaa0d265fabf937503a680d` |
| Result | SUCCESS |
| Duration | 47.677 s |
| Backend tests | 94 passed, 0 failed, 0 errors, 0 skipped |
| Frontend | `npm ci` and production build passed |
| Compose | Configuration validation passed |
| Archive | 294 files, approximately 62 MB |
| Persistence | Job and builds `#1` through `#7` remained after Jenkins image and container restart |

Build `#5` first failed because Jenkins did not recognize the manually written
JDK tool XML. Build `#6` then exposed an out-of-sync frontend lockfile after
the backend tests passed. Both issues were fixed before Build `#7`.

Extracted console evidence: `jenkins-build-7-summary.log`.
