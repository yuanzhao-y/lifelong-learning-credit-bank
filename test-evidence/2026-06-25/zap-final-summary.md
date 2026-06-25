# ZAP Final DAST Summary

- Executed: 2026-06-25T07:41:49.810Z
- Target: http://host.docker.internal/api
- Source OpenAPI: http://127.0.0.1/api/v3/api-docs
- Rewritten OpenAPI file: zap-final-openapi.json
- Timeout seconds: 900
- Result: LIMITED

| Evidence | Value |
|---|---|
| Exit code | null |
| Timed out | true |
| Report files | zap-final-report.html, zap-final-report.json, zap-final-report.md |
| Console log | zap-final-console.log |

PASS requires the ZAP API scan to complete and emit machine-readable output. LIMITED keeps the run auditable but does not support a full DAST pass conclusion.
