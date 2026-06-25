# Tested Version Manifest

| Item | Value |
| --- | --- |
| Tested branch | `develop` |
| Current pre-final local baseline | `ebc7553e867c56fc5e98ed0dcd9d9d7fb95da55d` plus this hardening pass |
| Hardening implementation commit | `4cbeefa63a3e5a29b6e6c5e672a9a6784bab57bc` |
| Stability parser fix commit | `b2291a4f13a9911613919306ccaf7307fb270d4d` |
| Final CI commit | `b2291a4f13a9911613919306ccaf7307fb270d4d` |
| Final Jenkins successful builds | `#13`, `#14` |
| Historical functional/security test commit | `2480b084bc99813cdcaa0d265fabf937503a680d` |
| Historical infrastructure CI commit | `16bbf1ed9afc1265dc03001cf6a0fc96c83a800a` |
| Historical CI gate closure commit | `30fcd870310826c87845b2e284ac34e72ec6e87c` |
| Requirements SHA-256 | `67e6354aa7e2b90c4ecf3dcf76b4aa6c1dab801c1e95d31502f438f92f6fdea7` |
| Database schema SHA-256 | `77a821c4d9186be77ddd5d3eb3b1ca60e2220362e913aeda057f46def9bb317f` |
| Original local backend Jar SHA-256 | `988064b89cbd3fec071c182b311623e34a6609d73db9c63038efa074aac24e42` |
| Post-review local Jar SHA-256 | `364fdc4ce886da407c064581e8eddc6d46116de155549a2dff4d4fada3017254` |
| Current hardened local Jar SHA-256 | `331679dc947dfd5b1886917af7ba0cc2c41dae445a21b5bdad1384237427a5b3` |
| Backend runtime image ID | `sha256:5b80fcc5b2e9d9920e9433e7a5d68d9d4d7144ae498de51817288f646c90025f` |
| Jenkins image | `jenkins/jenkins@sha256:cce1eeb79902722f3cf7a8831c41e95c39f4a42b9a2981959630cc66539b3c2d` |
| Runtime profile | Docker Compose, MySQL 8.0.33, Redis 7.2.4, Nginx 1.25.5 |
| Test date | 2026-06-25, Asia/Shanghai |
| Evidence manifest | `manifest.sha256` |
| Final remote CI evidence | `jenkins-build-13.md`, `jenkins-build-14.md`, `jenkins-stability.md` |

Frontend file-level SHA-256 values are recorded in `frontend-dist-sha256.txt`.

Source inheritance from `2480b084...` to `16bbf1ed...` is recorded in `source-diff-2480-to-16bb.md`: backend business source, frontend business source, database schema and Jenkinsfile were identical; only the Jenkins web port binding in `docker-compose.yml` changed.

This hardening pass changes tests, QA scripts, reports, evidence, and Jenkins frontend dependency audit. The final CI commit is `b2291a4f13a9911613919306ccaf7307fb270d4d`; Jenkins Build #12 failed during GitHub HTTPS checkout, then Build #13 and Build #14 completed the full pipeline successfully for the same commit. Post-CI edits in the report/evidence commit are documentation and evidence-index updates only; the relationship is recorded in `source-diff-final-ci-to-report.md`.
