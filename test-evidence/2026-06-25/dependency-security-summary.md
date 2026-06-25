# Dependency And Dynamic Security Scan Summary

| Check | Result | Evidence / limitation |
| --- | --- | --- |
| `npm audit` | PASS | 0 known vulnerabilities; `npm-audit.json` |
| Maven OWASP Dependency-Check | LIMITED | External vulnerability data download did not complete within five minutes |
| OSV container scan | LIMITED | No vulnerability findings were returned, but Maven resolution was partially affected by repository rate limiting; `osv-scan.json` must not be read as a full clean bill |
| ZAP first API scan | INVALID CONFIGURATION | OpenAPI relative server URL was resolved to the gateway root, producing 19 irrelevant 502 alerts |
| ZAP corrected API scan | LIMITED | Corrected OpenAPI imported 120 URLs, but the active scan exceeded the 10-minute limit and was stopped; `zap-api-corrected-timeout.log` |
| Manual/API security suite | PASS | Authentication, RBAC, horizontal access, JWT invalidation, rate limiting, SQL-shaped input, XSS sanitization, CSV injection, upload forgery, CORS and security headers passed in `api-audit.json` |

Dynamic and dependency scanning are therefore **not** declared production-complete.
