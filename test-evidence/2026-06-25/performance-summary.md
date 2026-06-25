# Business Performance Summary

- Tool: Apache JMeter 5.6.3
- Executed: 2026-06-25T02:21:48.028Z
- Load: 30 users, 30-second ramp-up, 300-second duration, one-second pacing
- Gateway: Nginx on 127.0.0.1:80

| ID | Scenario | Requests | Failures | Error rate | Avg ms | P90 | P95 | P99 | Max | Throughput/s | Result |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| ST-PERF-03 | public-outcome-query | 8325 | 0 | 0% | 20 | 26 | 31 | 45 | 94 | 27.89 | PASS |
| ST-PERF-04 | authenticated-credit-account | 8321 | 0 | 0% | 21.57 | 29 | 33 | 47 | 104 | 27.84 | PASS |
| ST-PERF-05 | password-login | 7760 | 0 | 0% | 93.83 | 120 | 131 | 156 | 213 | 26.02 | PASS |
