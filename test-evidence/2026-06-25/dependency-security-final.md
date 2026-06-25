# Backend Dependency Security Final Check

- Executed: 2026-06-25T07:08:03.047Z
- Proxy default: http://127.0.0.1:6244

| Check | Result | Evidence / limitation |
|---|---|---|
| npm audit | PASS | metadata vulnerabilities total=0 |
| Maven runtime dependency tree | PASS | Runtime dependency tree generated for audit traceability |
| OSV scanner | LIMITED | OSV scanner unavailable, timed out or failed; not declared pass |

A PASS result means the tool completed for the configured scope. LIMITED means the report remains auditable but cannot be used as a clean security conclusion.
