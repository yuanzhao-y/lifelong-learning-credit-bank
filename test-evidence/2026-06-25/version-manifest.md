# Tested Version Manifest

| Item | Value |
| --- | --- |
| Tested branch | `develop` |
| Functional/security test commit | `2480b084bc99813cdcaa0d265fabf937503a680d` |
| Final infrastructure CI commit | `16bbf1ed9afc1265dc03001cf6a0fc96c83a800a` |
| `llcb/develop` at CI gate closure | `30fcd870310826c87845b2e284ac34e72ec6e87c` |
| Requirements SHA-256 | `67e6354aa7e2b90c4ecf3dcf76b4aa6c1dab801c1e95d31502f438f92f6fdea7` |
| Database schema SHA-256 | `77a821c4d9186be77ddd5d3eb3b1ca60e2220362e913aeda057f46def9bb317f` |
| Local backend Jar SHA-256 | `988064b89cbd3fec071c182b311623e34a6609d73db9c63038efa074aac24e42` |
| Post-review local Jar SHA-256 | `364fdc4ce886da407c064581e8eddc6d46116de155549a2dff4d4fada3017254` |
| Backend image ID | `sha256:5b80fcc5b2e9d9920e9433e7a5d68d9d4d7144ae498de51817288f646c90025f` |
| Jenkins image | `jenkins/jenkins@sha256:cce1eeb79902722f3cf7a8831c41e95c39f4a42b9a2981959630cc66539b3c2d` |
| Runtime profile | Docker Compose, MySQL 8.0.33, Redis 7.2.4, Nginx 1.25.5 |
| Test date | 2026-06-25, Asia/Shanghai |
| Evidence manifest | `manifest.sha256` |
| Final remote CI evidence | Jenkins Build #9, SUCCESS, `30fcd870310826c87845b2e284ac34e72ec6e87c` |

Frontend file-level SHA-256 values are recorded in `frontend-dist-sha256.txt`.
Source inheritance from `2480b084...` to `16bbf1ed...` is recorded in
`source-diff-2480-to-16bb.md`: backend business source, frontend business source,
database schema and Jenkinsfile are identical; only the Jenkins web port binding
in `docker-compose.yml` changed.
The local backend Jar SHA-256 identifies the pre-hardening final `mvn verify`
artifact used in the original 2026-06-25 evidence pass. The post-review local
Jar SHA-256 identifies the artifact generated after adding the executable
JaCoCo core coverage gate. Jenkins Build #9 executed the committed gate and
archived the backend Jar, Surefire reports, JaCoCo reports and
`test-evidence/**`; Jenkins fingerprints provide the CI-side artifact identity.
Artifact relationships are expanded in `artifact-lineage.md`. Evidence source
and Jenkins-generation status are mapped in `evidence-map.md`.
