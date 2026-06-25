# Artifact Lineage

| Artifact | Source SHA | Built by | SHA-256 / ID | Purpose | Jenkins generated in latest CI |
| --- | --- | --- | --- | --- | --- |
| Original local backend Jar | `2480b084bc99813cdcaa0d265fabf937503a680d` | Local `mvn verify` | `988064b89cbd3fec071c182b311623e34a6609d73db9c63038efa074aac24e42` | Original functional/security/performance evidence pass | No |
| Post-review local backend Jar | local working tree after `16bbf1ed9afc1265dc03001cf6a0fc96c83a800a` | Local `mvn -B clean verify` | `364fdc4ce886da407c064581e8eddc6d46116de155549a2dff4d4fada3017254` | JaCoCo gate verification before Build #9 | No |
| Current hardened local backend Jar | pre-final working tree after `ebc7553e867c56fc5e98ed0dcd9d9d7fb95da55d` | Local `mvn -B clean verify` | `331679dc947dfd5b1886917af7ba0cc2c41dae445a21b5bdad1384237427a5b3` | 111 tests, improved branch evidence and final local gate | No |
| Backend runtime image | `16bbf1ed9afc1265dc03001cf6a0fc96c83a800a` runtime baseline | Docker build | `sha256:5b80fcc5b2e9d9920e9433e7a5d68d9d4d7144ae498de51817288f646c90025f` | Local demonstration runtime and QA scripts | No |
| Frontend package lock | current working tree | Git-tracked lockfile | See Git object / `package-lock.json` | Clean `npm ci` dependency identity | No |
| Frontend dist files | current working tree | Local `npm run build` | See `frontend-dist-sha256.txt` | Frontend build artifact identity | No |
| Jenkins Build #8 archived Jar family | `16bbf1ed9afc1265dc03001cf6a0fc96c83a800a` | Jenkins `llcb-backend-ci` Build #8 | Jenkins fingerprint, archived in Build #8 | Infrastructure CI evidence before JaCoCo gate was committed | Yes |
| Jenkins Build #9 archived Jar family | `30fcd870310826c87845b2e284ac34e72ec6e87c` | Jenkins `llcb-backend-ci` Build #9 | Jenkins fingerprint, archived in Build #9 | Remote CI evidence after JaCoCo gate was committed | Yes |
| Final Jenkins archived Jar family | To be filled after final SHA build | Jenkins `llcb-backend-ci` | Jenkins fingerprint | Remote CI evidence for this hardening pass | Yes |

Build #8 and #9 archived `test-evidence/**` from the repository in addition to Jenkins-generated Surefire, JaCoCo and Jar outputs. Being archived by Jenkins does not mean each evidence file was produced by that Jenkins run. The source for each evidence category is recorded in `evidence-map.md`.

The final Jenkins build for this pass must be recorded after commit and push. It should include the updated Jenkinsfile frontend `npm audit --audit-level=low` step, 111 backend tests, JaCoCo core coverage gate, frontend build, Compose validation and artifact archival.
