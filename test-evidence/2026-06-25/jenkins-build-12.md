# Jenkins Checkout Retry Failure

| Item | Actual value |
| --- | --- |
| Job | `llcb-backend-ci` |
| Build | `#12` |
| Result | FAILURE |
| Started | 2026-06-25T16:45:32+08:00 |
| Duration | 8.897 s |
| Source commit | `b2291a4f13a9911613919306ccaf7307fb270d4d` |
| Branch | `refs/remotes/origin/develop` |
| Commit message | `test: improve Jenkins stability evidence parsing` |
| Failure phase | SCM checkout |
| Failure reason | GitHub HTTPS fetch ended with `GnuTLS recv error (-110)` |

Build #12 failed before Maven, frontend, Compose or archive stages ran. It is
recorded as a CI infrastructure/network retry event, not as a product or test
failure. Builds #13 and #14 reran the same commit and completed successfully.

Extracted console evidence: `jenkins-build-12-summary.log`.
