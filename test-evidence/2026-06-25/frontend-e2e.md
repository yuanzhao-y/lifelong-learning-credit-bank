# Frontend Key Path E2E

- Executed: 2026-06-25T07:14:24.109Z
- Base URL: http://127.0.0.1:3000
- Total: 10
- Passed: 10
- Failed: 0

| ID | Requirement | Test | Expected | Actual | Result |
|---|---|---|---|---|---|
| FE-E2E-01 | FR-01 | Learner login and dashboard route | URL leaves /login | http://127.0.0.1:3000/ | PASS |
| FE-E2E-02 | FR-16 | Public/learner outcome browse | outcome page content visible | 终身学习学分银行 | PASS |
| FE-E2E-03 | FR-07 | Credit account page | credit account content visible | http://127.0.0.1:3000/credit/account | PASS |
| FE-E2E-04 | FR-05 | Certification application page | apply form route renders | http://127.0.0.1:3000/cert/apply | PASS |
| FE-E2E-05 | FR-08 | Conversion rule browse page | rule page route renders | http://127.0.0.1:3000/conversion/rules | PASS |
| FE-E2E-06 | FR-03 | Learner cannot stay on admin user management | redirected or access blocked | http://127.0.0.1:3000/ | PASS |
| FE-E2E-07 | FR-03 | Admin dashboard route | admin route available | http://127.0.0.1:3000/admin | PASS |
| FE-E2E-08 | FR-04 | Admin outcome management page | admin outcome route renders | http://127.0.0.1:3000/admin/outcomes | PASS |
| FE-E2E-09 | FR-12 | Admin statistics page | statistics route renders | http://127.0.0.1:3000/admin/statistics | PASS |
| FE-E2E-10 | FR-03 | Mobile admin layout width | no horizontal overflow | {"clientWidth":390,"scrollWidth":390,"bodyScrollWidth":390} | PASS |
