# Multi-node Concurrency Audit

- Executed: 2026-06-25T07:15:03.658Z
- Gateway URL: http://127.0.0.1/api
- Backend node URLs: http://127.0.0.1:8080/api, http://127.0.0.1:8082/api
- Total: 5
- Passed: 5
- Failed: 0

| ID | Requirement | Test | Expected | Actual | Result |
|---|---|---|---|---|---|
| MN-AUTH-L | FR-01 | learner login for multinode audit | HTTP 200 and token | HTTP 200; roles=learner | PASS |
| MN-AUTH-A | FR-01 | auditor login for multinode audit | HTTP 200 and token | HTTP 200; roles=auditor | PASS |
| MN-ENV-01 | ENV | All configured backend nodes are reachable | every node returns 200/UP | [{"url":"http://127.0.0.1:8080/api","status":200,"health":"UP"},{"url":"http://127.0.0.1:8082/api","status":200,"health":"UP"}] | PASS |
| MN-CONC-01 | FR-06 | Certification approval race across configured nodes | one success, remaining business rejected, at least two target nodes used | success=1; rejected=11; nodes=http://127.0.0.1:8082,http://127.0.0.1:8080 | PASS |
| MN-CONC-02 | FR-10 | Conversion approval race across configured nodes | one success, remaining business rejected, at least two target nodes used | success=1; rejected=11; nodes=http://127.0.0.1:8082,http://127.0.0.1:8080 | PASS |

This audit proves only the configured local node set. It is not a production cluster capacity or failover certification.
