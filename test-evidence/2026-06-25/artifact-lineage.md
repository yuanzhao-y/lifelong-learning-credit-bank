# Artifact Lineage

| Artifact | Source SHA | Built by | SHA-256 / ID | Purpose | Jenkins generated in latest CI |
| --- | --- | --- | --- | --- | --- |
| Original local backend Jar | `2480b084bc99813cdcaa0d265fabf937503a680d` | Local `mvn verify` | `988064b89cbd3fec071c182b311623e34a6609d73db9c63038efa074aac24e42` | Original functional/security/performance evidence pass | No |
| Post-review local backend Jar | local working tree after `16bbf1ed9afc1265dc03001cf6a0fc96c83a800a` | Local `mvn -B clean verify` | `364fdc4ce886da407c064581e8eddc6d46116de155549a2dff4d4fada3017254` | JaCoCo gate verification before remote CI rerun | No |
| Backend runtime image | `16bbf1ed9afc1265dc03001cf6a0fc96c83a800a` runtime baseline | Docker build | `sha256:5b80fcc5b2e9d9920e9433e7a5d68d9d4d7144ae498de51817288f646c90025f` | Local demonstration runtime | No |
| Frontend package lock | `2480b084bc99813cdcaa0d265fabf937503a680d` | Git-tracked lockfile | `063dfabdd5980b347a9eea384ade9d7ec69d32b44a458a1c9d56d380f48a3154` | Clean `npm ci` dependency identity | No |
| Frontend dist files | `2480b084bc99813cdcaa0d265fabf937503a680d` | Local `npm run build` | See `frontend-dist-sha256.txt` | Frontend build artifact identity | No |
| Jenkins Build #8 archived Jar family | `16bbf1ed9afc1265dc03001cf6a0fc96c83a800a` | Jenkins `llcb-backend-ci` Build #8 | Jenkins fingerprint, archived in Build #8 | Infrastructure CI evidence before JaCoCo gate was committed | Yes |
| Jenkins Build #9 archived Jar family | `30fcd870310826c87845b2e284ac34e72ec6e87c` | Jenkins `llcb-backend-ci` Build #9 | Jenkins fingerprint, archived in Build #9 | Remote CI evidence after JaCoCo gate was committed | Yes |

Build #8 archived `test-evidence/**` from the repository in addition to
Jenkins-generated Surefire, JaCoCo and Jar outputs. Being archived by Jenkins
does not mean each evidence file was produced by that Jenkins run. The source
for each evidence category is recorded in `evidence-map.md`.

Build #9 archived 356 files, including the backend Jar, Surefire reports,
JaCoCo reports and repository-carried `test-evidence/**`.
