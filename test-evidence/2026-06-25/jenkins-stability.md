# Jenkins Stability Evidence

- Executed: 2026-06-25T08:49:10.522Z
- Job: llcb-backend-ci
- Expected commit: b2291a4f13a9911613919306ccaf7307fb270d4d
- Matching successful builds: 13, 14

| Build | Result | Commit | Backend verify | Frontend build | Compose check | Archive |
|---|---|---|---|---|---|---|
| #10 | SUCCESS | 4cbeefa63a3e5a29b6e6c5e672a9a6784bab57bc | yes | yes | yes | yes |
| #11 | SUCCESS | 4cbeefa63a3e5a29b6e6c5e672a9a6784bab57bc | yes | yes | yes | yes |
| #12 | FAILURE | b2291a4f13a9911613919306ccaf7307fb270d4d | no | no | no | no |
| #13 | SUCCESS | b2291a4f13a9911613919306ccaf7307fb270d4d | yes | yes | yes | yes |
| #14 | SUCCESS | b2291a4f13a9911613919306ccaf7307fb270d4d | yes | yes | yes | yes |

| Check | Result |
|---|---|
| atLeastTwoRecentSuccessfulBuildsForExpectedCommit | PASS |
| allExpectedBuildsRanBackendVerify | PASS |
| allExpectedBuildsRanFrontendBuild | PASS |
| allExpectedBuildsRanComposeCheck | PASS |
| allExpectedBuildsArchivedArtifacts | PASS |

Final result: **PASS**

Build #12 is retained as an infrastructure retry record: it failed during GitHub HTTPS checkout with a TLS connection termination before Maven, frontend, Compose or archive stages ran. Builds #13 and #14 then succeeded consecutively on the same expected commit.

This evidence confirms repeated Jenkins success for the expected commit when the matching build count is at least two. A Jenkins process restart is recorded separately if performed.
