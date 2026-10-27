# Open questions, access and decision log

Confirmed by the user: **fourth edition (۱۳۹۹) first; defer authenticated/team inspection for now**. Proposed implementation defaults: native Kotlin/Compose, offline durable draft editing, online calculations until each local rule family is verified, and public engineering coverage before member collaboration.

## Interaction/access needed

| ID | Needed from user/owner | Why / affected work | Work that can continue |
|---|---|---|---|
| Q01 | **Deferred at user direction**: test/staging account, ideally owner/designer/collaborator/reviewer roles | Future login, project read/save, CSRF, locks, roles and team capture. Current public login/dashboard links return 404. Affects future member work, not this planning delivery. | Guest implementation, local data, verified public API and UI work |
| Q02 | **Resolved: fourth edition first** | Fifth-edition labels in CSS describe styling, not proof of supported calculations. Any later fifth-edition support requires separate discovery. | Fourth-edition plan and models with edition isolation |
| Q03 | Backend owner contact or API/backend source/documentation | Native session/bootstrap, supported API version, idempotency, limits, errors and backward compatibility. Affects DTO hardening/member sync. | Service interfaces, mocked contracts, public calculations |
| Q04 | Approved regulatory sources/golden examples and engineer review | Validate client/server discrepancies, all rule matrices and offline formulae. Affects engineering acceptance/local calculators. | Online adapters and draft UI; label unknowns |

Please do not paste passwords, cookies or CSRF values into chat/docs. Login/CAPTCHA or invitation acceptance can be performed directly by the user. A real team invitation/removal or authenticated save should use an explicitly designated test project and accounts.

## Product decisions before release

| ID | Decision | Proposed default / implication |
|---|---|---|
| Q05 | Offline scope | Draft creation/edit/export and cached references immediately; numeric offline rules phased after validation. Full offline solver would expand engineering work. |
| Q06 | Native app or embedded web parity | Native Compose app is the plan; any temporary report/login web surface needs a supported integration and clear boundaries. |
| Q07 | Minimum devices/package/distribution | Provisional minSdk 26 and `ir.mabhas19.android`; confirm ownership, device audience and Google Play/Cafe Bazaar/direct APK targets. |
| Q08 | Brand/fonts/background rights | Obtain approved logo/images and licensed Persian font. Use native design tokens with explicit owner review. |
| Q09 | Report acceptance | Confirm whether Android reports are design aids, required submission artifacts, or must match server report layout exactly; preserve current preliminary flags. |
| Q10 | Accounts, backup and guest migration | Agree supported guest→member migration, on-device retention/logout behavior, app backup and account deletion. Browser drafts cannot be read directly by Android. |
| Q11 | Analytics/crash support and privacy | Select only after owner policy; reports may contain engineer/owner/project data. No analytics service assumed. |
| Q12 | Schedule/team and priority | Establish after first vertical slice and unresolved backend effort; choose whether reviewer/team capabilities are release-one or later. |

## Concrete risks and mitigation

- **Unversioned PHP API:** isolate DTOs/adapters, retain captured contracts and agree compatibility support with backend owner.
- **Session-only auth with broken public links:** verify supported bootstrap before building member login; guest mode remains useful.
- **Local/server divergence:** explicit dirty/base locks, conflict UI, no automatic overwrite; never substitute guest mode on a failed member save.
- **Browser UI versus server result mismatch:** captured air sample is below limit but still review-required; preserve both states.
- **Reference-source verification metadata varies:** retain row/version/source status and require engineering approval for rule ports.
- **Shared draft aggregates:** field-aware merge prevents air-test/checklist disciplines overwriting one another.
- **Dense Persian engineering UI/PDFs:** native repeatable-item editors, TalkBack/font-scale checks and rendered A4 QA.
- **Raw capture sensitivity:** ignored local artifacts, reviewed public sample only, no session credentials in fixtures.

## Decision log

| Date | Decision / status | Basis |
|---|---|---|
| 2026-10-27 | Installed official REA skill in `.agents/skills/reverse-engineer-anything` | Explicit user request for local installation/use |
| 2026-10-27 | Used REA CLI with npm-cache Node 24.22.1 + REA 6.8.0; did not change global agent configuration | Browser investigation works now without restarting the active task |
| 2026-10-27 | Published a single synthetic air-test transaction and public guest screenshots | Useful regression/design evidence without account data |
| 2026-10-27 | Fourth edition first; authenticated/team inspection deferred | Explicit user answers during this task |
| 2026-10-27 | Planning/scoping only in this task; Android scaffold is next implementation work | User requested extracted Markdown, plan, structure and AGENTS initialization |
