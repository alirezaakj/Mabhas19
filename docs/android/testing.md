# Testing and delivery strategy

Status: test design, not an existing/passing Android suite. Introduce tests with each implementation phase. Favor fast deterministic JVM tests, real local integrations, and a smaller set of end-to-end device journeys. Production services must never be dependencies of normal CI.

## Test layers

| Layer | Tools / location after scaffold | Essential behavior |
|---|---|---|
| Pure rules and validation | JUnit parameterized tests in `core/calculation/src/test`, model/domain tests | Units, classification boundaries, applicability/unknowns, rule tables, approval flags and input normalization |
| ViewModels/use cases | Fakes, `kotlinx-coroutines-test`, StateFlow assertions; feature `src/test` | Loading/edit/save/error/retry/conflict states, cancellation, stale inputs and project switching |
| Network contract | MockWebServer + serialization tests in `core/network/src/test` | Exact methods/paths/body fields, application-vs-HTTP errors, redirects/HTML, nullable fields, session/CSRF integration and lock serialization |
| Repositories/sync | Fake services + real transaction-capable local test data where appropriate | Durable local-before-remote writes, dirty revisions, conflict preservation, concurrent edits, retry/outcome reconciliation |
| Room/migrations | AndroidX test + in-memory/file Room and MigrationTestHelper; `core/database/src/androidTest` | DAO isolation, actual SQLite constraints, every supported schema upgrade, crash-safe transactions and persisted recovery |
| Compose/navigation | Compose UI tests, fake DI, emulator/physical device | Persian fields/errors, navigation/back/project identity, item editors, process-restart recovery and accessibility semantics |
| Reports/files | Pure ReportModel goldens, file integration and rendered PDF visual checks | Deterministic snapshots, non-lossy supported JSON round-trip, Persian shaping, A4 table/page layout and safe cancellation |
| Performance | Macrobenchmark + baseline profiles | Startup, large material lists, many elements and report generation on representative devices |
| Real service UAT | Explicit staging suite/manual engineer review | Actual roles, authenticated contracts, numeric parity and concurrent web/Android edits |

Use fakes for repositories and clocks rather than mocking implementation details. Inject dispatcher/clock/ID providers. Assert meaningful state/output, not method-call sequences or snapshots of every UI detail. Use shared reviewed fixtures with provenance rather than constructing expected values from the same production formula.

## Initial required cases

| Test ID | Related features/APIs | Cases and assertions |
|---|---|---|
| T01 | F02/F03, A01–A03 | City/occupancy lookup, aliases/province filter; group matrix including unknown city/use. Boundary area 1999.99/2000/2000.01 and floors 8/9/10. |
| T02 | F20 | Persian `۱۲۳٫۴`, Arabic `١٢٣٫٤`, ASCII, empty, grouping, negative, malformed/NaN/infinite/overflow inputs. Null remains distinct from zero. Exact comma policy documented. |
| T03 | F05–F07 | cm-to-m resistance, λ versus explicit R, source/geometry flags, parallel paths, ordered layers and no double-counted surface resistances; invalid denominators stay invalid. |
| T04 | F12, A12 | Captured sample: 6750/3000 = 2.25; below EC limit 3; **review_required**, no regulatory/building approval. Full fixture in `research/evidence/airtightness-sample.json`. |
| T05 | F12 | Height exactly 3 versus just above, strict equality fails numeric threshold, non-50-Pa test, zero/missing V/A, EC/EC+/EC++ and around 5000 m² subdivision. Capture server boundary fixtures before asserting server contract. |
| T06 | F08/F09, A06–A10 | Uw/SHGC/Tv bounds and Tv/SHGC zero handling; orientation/grade/need matrix; shading geometry cannot confer unsupported exception. |
| T07 | F10, A11 | Overlapping/disjoint same-façade windows, no windows, incomplete inventory/plan evidence, unsupported geometry and task-lighting checks. |
| T08 | F11/F12/F15/F16, A16 | Air-sealing save preserves measured test; air-test save preserves evidence; mechanical edit preserves electrical answers in shared chapter draft. |
| T09 | F20, A16/A17 | Client A edits base lock, B saves, A sees conflict; local/remote retained. New edits during upload remain dirty after earlier revision acknowledgment. |
| T10 | F19/F20 | Session expiry, lost auth, denied role, account/project switch during fetch/upload; no cross-account caches/uploads and no silent guest downgrade. |
| T11 | F20 | Timeout after server acceptance → OutcomeUnknown → reconcile before repeat; offline queue, reconnect/backoff, cancellation and duplicate worker scheduling. |
| T12 | F18/F20 | Schema/edition validation; corrupt/oversized JSON, file cancellation, older format migration, broken references, atomic import and export round-trip. |
| T13 | F18 | Dossier from a fixed immutable snapshot; missing/stale sections never pass; preliminary flags preserved; all text fits in Persian A4 output. |
| T14 | F01–F20 | RTL/Latin unit ordering, TalkBack/focus, dark/light, 200% font scale, small phone/tablet/landscape and IME. |
| T15 | F02/F20 | Kill/restart app after local save, load old schema, storage failure, rapid project switching and recovery without blanking the last draft. |

Not all these expected engineering values are known yet. Cases T01/T03/T05–T07 need independent table/formula verification and captured or engineer-approved goldens. Keep pending cases visible instead of manufacturing a complete regulatory test oracle from website comments.

## CI progression

Create workflows only after the scaffold supplies working tasks. Set default Gradle working directory to `android/`. Representative commands below are targets to configure and verify, not commands that work in the current planning repository:

```sh
./gradlew --no-daemon ktlintCheck detekt testDebugUnitTest lintDebug assembleDebug
./gradlew :core:calculation:test
./gradlew connectedDebugAndroidTest
```

Ensure the root quality tasks actually aggregate all relevant modules, including pure JVM tests (`testDebugUnitTest` alone does not cover every JVM module). Use targeted tests during editing, then PR checks on affected components and the aggregate build.

- Pull requests: format/static checks, JVM/domain/DTO/ViewModel tests, lint, debug build and a focused emulator integration/UI suite. Upload test/lint results and failure screenshots.
- Nightly/pre-release: supported API/device-size matrix, full Room migration and Compose journeys, JSON/PDF QA, larger datasets and staging-only opt-in checks. Keep secrets and permissions scoped.
- Release: all required checks, engineering goldens, real-device user acceptance, signed release build and server compatibility evidence. Branch protection should require configured checks once CI exists.

Use a proposed minimum API 26 device and a current stable supported API device, plus a tablet. Pin exact emulator/SDK images at scaffold time. API target/store policies must be rechecked at release. Never log cookie/token bodies in reports or CI artifacts.

## Exit criteria

Every implemented feature has meaningful tests for its core behavior and important failure path. All supported rule-table cells and explicit unknown/forbidden cases have traceable expectations. No open data-loss, incorrect numeric comparison or misleading approval defect. Coverage reports identify gaps; a line-coverage percentage alone is not acceptance. Performance thresholds are set from measured representative devices rather than arbitrary numbers.

Official reference: [Android testing strategies](https://developer.android.com/training/testing/fundamentals/strategies), [coroutine testing](https://developer.android.com/kotlin/coroutines/test), [Room migration testing](https://developer.android.com/training/data-storage/room/migrating-db-versions#test), [Compose testing](https://developer.android.com/develop/ui/compose/testing).
