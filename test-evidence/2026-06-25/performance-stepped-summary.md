# Stepped Business Performance Summary

- Tool: Apache JMeter 5.6.3
- Executed: 2026-06-25T08:11:44.499Z
- Load levels: 30, 60, 100 users
- Duration per scenario: 60s, ramp-up: 20s
- Gateway: Nginx on 127.0.0.1:80

| ID | Scenario | Users | Requests | Failures | HTTP/JMeter error rate | Avg ms | P90 | P95 | P99 | Max | Throughput/s | Result |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| ST-PERF-S1 | public-outcome-query | 30 | 1454 | 0 | 0% | 20.04 | 26 | 29 | 39 | 109 | 24.79 | PASS |
| ST-PERF-S2 | authenticated-credit-account | 30 | 1456 | 0 | 0% | 20.11 | 26 | 31 | 37 | 137 | 24.74 | PASS |
| ST-PERF-S3 | password-login | 30 | 1379 | 0 | 0% | 75.95 | 88 | 94 | 110 | 145 | 23.5 | PASS |
| ST-PERF-S1 | public-outcome-query | 60 | 2903 | 0 | 0% | 19.03 | 25 | 29 | 44 | 92 | 49.31 | PASS |
| ST-PERF-S2 | authenticated-credit-account | 60 | 2909 | 0 | 0% | 20.03 | 27 | 32 | 38 | 53 | 49.35 | PASS |
| ST-PERF-S3 | password-login | 60 | 2763 | 0 | 0% | 78.78 | 97 | 107 | 125 | 167 | 46.9 | PASS |
| ST-PERF-S1 | public-outcome-query | 100 | 4842 | 0 | 0% | 18.9 | 24 | 28 | 33 | 42 | 82.22 | PASS |
| ST-PERF-S2 | authenticated-credit-account | 100 | 4820 | 0 | 0% | 23 | 30 | 33 | 50 | 425 | 81.94 | PASS |
| ST-PERF-S3 | password-login | 100 | 4549 | 0 | 0% | 85.81 | 105 | 113 | 127 | 197 | 77.18 | PASS |

This is a local capacity baseline only. It does not extrapolate production capacity because host resources, data scale, network topology and JVM/container sizing differ from production.
