# Source Diff Evidence: `2480b084` to `16bbf1ed`

| Item | Value |
| --- | --- |
| Functional/security/performance evidence commit | `2480b084bc99813cdcaa0d265fabf937503a680d` |
| Infrastructure CI commit | `16bbf1ed9afc1265dc03001cf6a0fc96c83a800a` |
| Diff command | `git diff --name-status 2480b084bc99813cdcaa0d265fabf937503a680d..16bbf1ed9afc1265dc03001cf6a0fc96c83a800a` |
| Changed paths | `M docker-compose.yml` |

## Tree And Blob Checks

| Check | `2480b084` | `16bbf1ed` | Result |
| --- | --- | --- | --- |
| Backend business source tree `src/main/java` | `6f677a90366be25540746fe160e9c70dceca97aa` | `6f677a90366be25540746fe160e9c70dceca97aa` | Same |
| Frontend business source tree `credit-bank-frontend/src` | `50c4c3f0f57c9f827580b4fc5cd4e64eff7d6380` | `50c4c3f0f57c9f827580b4fc5cd4e64eff7d6380` | Same |
| Database schema blob `src/main/resources/db/schema.sql` | `6d490c7ce9b5109d85c3ddb1318a00c0c9ba181b` | `6d490c7ce9b5109d85c3ddb1318a00c0c9ba181b` | Same |
| CI pipeline blob `Jenkinsfile` | `2fa443fec57ad4fb1379c0c0c5a621bd4bd3df11` | `2fa443fec57ad4fb1379c0c0c5a621bd4bd3df11` | Same |
| Compose file blob `docker-compose.yml` | `39b9beb612b1af64a9b7d9405bde58f97674eb8b` | `95e5b95cdcc241de5fd620cae2f5b96b00176988` | Changed |

## Changed File Detail

```diff
diff --git a/docker-compose.yml b/docker-compose.yml
@@
-      - "${JENKINS_PORT:-8081}:8080"
+      - "127.0.0.1:${JENKINS_PORT:-8081}:8080"
```

The only code change between the functional/security/performance evidence commit
and the infrastructure CI commit binds the Jenkins web port to localhost. Backend
business code, frontend business code, database schema and the Jenkins pipeline
were unchanged. Therefore functional, application-security, concurrency and
performance conclusions produced at `2480b084...` can be inherited by
`16bbf1ed...`, with the explicit limitation that the Jenkins network exposure
configuration changed and was validated by Build #8.
