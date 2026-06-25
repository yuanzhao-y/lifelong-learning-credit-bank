# API Audit Summary

- Executed: 2026-06-25T02:03:54.302Z
- Total: 58
- Passed: 58
- Failed: 0

| ID | Requirement | Test | Expected | Actual | Result |
|---|---|---|---|---|---|
| IT-ENV-02 | ENV | Direct backend health | 200/UP | 200/UP | PASS |
| IT-ENV-03 | ENV | Nginx health proxy | 200/UP | 200/UP | PASS |
| IT-ENV-04 | ENV | OpenAPI document | 200 | 200 | PASS |
| BB-01-01A | FR-01 | admin password login | HTTP 200 and expected role | HTTP 200; roles=admin | PASS |
| BB-01-01L | FR-01 | learner password login | HTTP 200 and expected role | HTTP 200; roles=learner | PASS |
| BB-01-01U | FR-01 | auditor password login | HTTP 200 and expected role | HTTP 200; roles=auditor | PASS |
| BB-01-01E | FR-01 | expert password login | HTTP 200 and expected role | HTTP 200; roles=expert | PASS |
| IT-AUTH-04 | FR-01 | Protected endpoint without token | 401 | 401 | PASS |
| IT-RBAC-02 | FR-03 | Learner calls administrator API | 403 | 403 | PASS |
| IT-SEC-04A | SEC | Reject untrusted CORS origin | 403 and no ACAO | 403; ACAO=none | PASS |
| IT-SEC-04B | SEC | Security response headers | nosniff/DENY/no-referrer | nosniff/DENY/no-referrer | PASS |
| BB-02-01 | FR-02 | Learner profile | 200 | 200 | PASS |
| BB-03-01 | FR-03 | Administrator role list | 200 | 200 | PASS |
| BB-04-01 | FR-04 | Outcome catalog management list | 200 | 200 | PASS |
| BB-05-05 | FR-05 | Certification application history | 200 | 200 | PASS |
| BB-06-01 | FR-06 | Certification audit queue | 200 | 200 | PASS |
| BB-07-01 | FR-07 | Credit account | 200 | 200 | PASS |
| BB-08-01 | FR-08 | Conversion rule management | 200 | 200 | PASS |
| BB-09-05 | FR-09 | Conversion application history | 200 | 200 | PASS |
| BB-10-01 | FR-10 | Conversion audit queue | 200 | 200 | PASS |
| BB-11-03 | FR-11 | Expert review queue | 200 | 200 | PASS |
| BB-12-01 | FR-12 | Statistics overview | 200 | 200 | PASS |
| BB-13-01 | FR-13 | Learner messages | 200 | 200 | PASS |
| BB-14-01 | FR-14 | Operation logs | 200 | 200 | PASS |
| BB-15-01 | FR-15 | Dictionary management | 200 | 200 | PASS |
| BB-16-01 | FR-16 | Public outcome browse | 200 | 200 | PASS |
| BB-17-01 | FR-17 | Public conversion rules | 200 | 200 | PASS |
| BB-18-01 | FR-18 | Learner education archive | 200 | 200 | PASS |
| BB-19-01 | FR-19 | Public announcements | 200 | 200 | PASS |
| BB-20-01 | FR-20 | Learner feedback history | 200 | 200 | PASS |
| SEC-INJECT-01 | SEC | SQL injection-shaped search input | 200 | 200 | PASS |
| SEC-INJECT-01B | SEC | Search result remains paginated | records <= 5 | 0 | PASS |
| UT-AUTH-04-API | FR-01 | Weak password registration | 400 | 400 | PASS |
| IT-SEC-LOGIN-RATE | SEC | Login brute-force throttling | five 400 then 429 | 400,400,400,400,400,429 | PASS |
| IT-SEC-SMS-RATE | SEC | SMS code request throttling | 200 then 429 | 200,429 | PASS |
| IT-AUTH-01 | FR-01 | Create isolated QA learner | 200 | 200 | PASS |
| IT-AUTH-02 | FR-01 | learner password login | HTTP 200 and expected role | HTTP 200; roles=learner | PASS |
| BB-02-03 | FR-02 | Change password | 200 | 200 | PASS |
| IT-SEC-03 | SEC | Old JWT after password change | 401 | 401 | PASS |
| IT-AUTH-02B | FR-01 | learner password login | HTTP 200 and expected role | HTTP 200; roles=learner | PASS |
| BB-18-02 | FR-18 | Create education experience | 200 | 200 | PASS |
| IT-SEC-02P | SEC | Horizontal update of education experience | 404 | 404 | PASS |
| IT-SEC-02M | SEC | Horizontal read of another user's message | 404 | 404 | PASS |
| IT-XSS-01 | FR-19 | Create announcement containing unsafe HTML | 200 | 200 | PASS |
| IT-XSS-02 | SEC | Public announcement HTML is sanitized | safe paragraph without script/event handler | <p>safe</p> | PASS |
| IT-CSV-01A | FR-04 | CSV BOM/quoted newline/invalid formula row | created=1 skipped=1 | HTTP 200; created=1; skipped=1 | PASS |
| IT-CSV-01B | FR-04 | Repeated outcome code updates existing row | updated=1 skipped=1 | HTTP 200; updated=1; skipped=1 | PASS |
| IT-CSV-02 | FR-04 | CSV row limit | HTTP 400 | 400 | PASS |
| BB-04-05 | FR-04 | Outcome catalog export | CSV attachment with data | HTTP 200; bytes=1786 | PASS |
| IT-CSV-03 | FR-14 | Filtered operation log export | UTF-8 BOM CSV attachment | HTTP 200; bytes=1310 | PASS |
| IT-FILE-01 | FR-05 | Reject forged upload type and extension | HTTP 400 | 400 | PASS |
| IT-TOS-01 | FR-05 | Valid TOS upload and object read | upload 200 and object 200 | upload=200; objectReachable=true | PASS |
| IT-SEC-02C | SEC | Horizontal read of certification audit trail | 404 | 404 | PASS |
| IT-CONC-01 | FR-06 | Ten concurrent certification approvals | one success and nine rejected | success=1; rejected=9 | PASS |
| IT-IDEMP-01 | FR-06 | Repeated certification approval | HTTP 400 | 400 | PASS |
| IT-CONC-03 | FR-06 | Withdraw versus approval race | exactly one succeeds | 200,400 | PASS |
| IT-CONC-02 | FR-10 | Ten concurrent conversion approvals | one success and nine rejected | success=1; rejected=9 | PASS |
| IT-IDEMP-02 | FR-10 | Repeated conversion approval | HTTP 400 | 400 | PASS |
