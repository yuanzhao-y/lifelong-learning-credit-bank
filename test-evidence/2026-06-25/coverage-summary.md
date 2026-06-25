# JaCoCo Coverage Summary

Tested source commit: `2480b084bc99813cdcaa0d265fabf937503a680d`

| Scope | Line coverage | Branch coverage | Assessment |
| --- | ---: | ---: | --- |
| Entire backend | 49.23% | 7.72% | Recorded only; generated models, controllers and configuration reduce the aggregate |
| `service` package | 72.53% | 49.67% | Line target met; branch coverage remains an improvement item |
| `security` package | 85.82% | 63.46% | Target met |
| `common` package | 74.07% | 16.36% | Utility line coverage met; exception/response branches remain sparse |
| Selected core services and security components | 83.76% | 59.91% | Planned 70% line / 55% branch gate met |

The selected core scope contains `CreditService`, `CertificationService`,
`ConversionService`, `ConversionRuleService`, `AuthService`,
`SensitiveDataService`, `JwtService`, `JwtAuthenticationFilter` and
`LoginAttemptService`.

Raw evidence: `jacoco.xml`, `jacoco.csv`.
