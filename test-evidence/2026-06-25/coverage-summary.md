# JaCoCo Coverage Summary

Tested local working tree: `ebc7553e867c56fc5e98ed0dcd9d9d7fb95da55d` plus this hardening pass before final CI alignment.

JUnit/Surefire tests: 111 executed, 0 failures/errors.

| Scope | Line coverage | Branch coverage | Assessment |
| --- | ---: | ---: | --- |
| Entire backend | 51.62% | 8.36% | Recorded; overall branch coverage improved but not used as the primary pass/fail gate |
| `service package` | 72.53% | 49.67% | Audited package-level signal |
| `security package` | 92.20% | 75.00% | Audited package-level signal |
| `controller package` | 29.54% | 32.84% | Audited package-level signal |
| `common package` | 92.59% | 16.36% | Audited package-level signal |
| Selected core services and security components | 83.76% | 60.81% | JaCoCo build gate met |

The selected core scope remains the executable JaCoCo gate configured as ``check-core-coverage`` in ``pom.xml``. The gate requires line coverage >= 80% and branch coverage >= 55%, and ``mvn -B clean verify`` passed after the new controller, exception handling and security utility branch tests.

Overall branch coverage remains heavily affected by entity/Lombok-generated methods and broad non-core branches, so the report uses it as an improvement signal rather than a release gate.

Raw evidence: ``jacoco.xml``, ``jacoco.csv`` and ``surefire/TEST-*.xml``.
