# Frontend Functional Verification

| Check | Actual result | Conclusion |
| --- | --- | --- |
| Learner login | Login succeeded; learner dashboard and learner menus rendered | PASS |
| Learner role boundary | Direct navigation to `/admin/users` redirected to `/` | PASS |
| Outcome search | Searching `软件设计师` returned two matching records | PASS |
| Outcome detail | Detail displayed certification requirements and applicable scope | PASS |
| Certification navigation | “申请认证该成果” opened the application page with the outcome preselected | PASS |
| Administrator login | Login succeeded; administrator menus and dashboard rendered | PASS |
| Administrator outcome search | Exact QA outcome code returned one record | PASS |
| Mobile layout before fix | 390 px viewport produced 859 px document width | FAIL, fixed |
| Mobile layout after fix | `clientWidth=390`, `scrollWidth=390`, `bodyScrollWidth=390` | PASS |
| Mobile menu | Menu button opened a 260 px sidebar; overlay closed it | PASS |
| Outcome export UI | Programmatic object-URL download was not captured by the browser driver | INCONCLUSIVE; API export independently passed |

Screenshot evidence: `frontend-admin-outcomes-mobile.png`.

Build evidence: `npm ci`, `npm run build` and `npm audit` all completed
successfully after the lockfile was synchronized. The build still reports
large Element Plus and ECharts chunks.
