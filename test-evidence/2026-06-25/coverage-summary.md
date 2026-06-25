# JaCoCo Coverage Summary

Tested source commit: `2480b084bc99813cdcaa0d265fabf937503a680d`

| Scope | Line coverage | Branch coverage | Assessment |
| --- | ---: | ---: | --- |
| Entire backend | 49.23% | 7.72% | Recorded only; not used as a pass/fail gate |
| `service` package | 72.53% | 49.67% | Line target met; branch coverage remains an improvement item |
| `security` package | 85.82% | 63.46% | Target met |
| `common` package | 74.07% | 16.36% | Utility line coverage met; exception/response branches remain sparse |
| Selected core services and security components | 83.76% | 59.91% | Build gate met |

The selected core scope contains `CreditService`, `CertificationService`,
`ConversionService`, `ConversionRuleService`, `AuthService`,
`SensitiveDataService`, `JwtService`, `JwtAuthenticationFilter` and
`LoginAttemptService`.

The executable gate is configured in `pom.xml` as `check-core-coverage`.
It includes the classes above and requires line coverage >= 80% and branch
coverage >= 55%. It was added during the post-review report hardening pass and
verified locally with `mvn -B clean verify`; Jenkins Build #9 also ran the same
gate and passed. The full-backend aggregate is intentionally not used as a pass/fail
gate. Entity, DTO and configuration code affects aggregate line coverage, while
low aggregate branch coverage mainly comes from controller paths, exception
handling, validation, security filters and non-core business condition branches
that remain tracked as improvement scope.

Raw evidence: `jacoco.xml`, `jacoco.csv`.
