# Implementation roadmap

Planning baseline: 2026-10-27. Target: native Kotlin app covering the fourth-edition website workflows, as confirmed by the user. Authenticated/team inspection is deferred at the user's direction; its future implementation remains planned with explicit prerequisites. Checklist items below are planned work unless explicitly checked. Full feature mapping is in [features](../discovery/features.md); contracts in [API inventory](../discovery/api.md).

## Phase 0 — Finish contracts and agree scope

- [x] Install project-local official REA skill; run its browser workflow through CLI.
- [x] Inventory linked public pages/assets; extract features, API names/client inputs, RTL design and persistence.
- [x] Capture public reference reads, guest behavior and one server calculation with provenance.
- [x] Record architecture, tests, unknowns and repository instructions.
- [x] Confirm fourth edition first and defer authenticated/team inspection.
- [ ] Confirm package name, device/distribution targets, native scope and brand assets.
- [ ] With test-account interaction, verify login/session bootstrap, member project/draft/library writes, role boundaries, invitation lifecycle and conflict outcomes.
- [ ] Capture representative success/error/unknown examples for A03–A18; resolve nonuniform envelopes.
- [ ] Obtain owner/engineer-approved sources and golden calculations for offline rules.

Deliverable: reviewed release scope and contract matrix. Gate: unknown contracts stay behind interfaces/disabled capabilities; implementation may proceed on verified guest/local behavior. Login-dependent work waits for a supported session contract.

## Phase 1 — Reproducible skeleton and Persian UI foundation

Dependencies: public discovery; no account required.

Phase 1 status (2026-10-27): items 1–4 are implemented and locally verified; item 5 (CI) is written but unproven, and the device-side tests exist without having run. See [phase-1-status.md](phase-1-status.md).

1. [x] Select compatible stable tool versions and commit Gradle wrapper, checksums/verification policy, version catalogue and convention plugins under `android/`.
2. [x] Create app + model/domain/data/network/database/designsystem/testing modules needed for the first slice. Add remaining feature modules incrementally.
3. [x] Configure Kotlin/Compose/Hilt, build types, manifest/RTL, theme, Persian resources, baseline navigation, logging redaction and local configuration policy. (No logging is emitted yet; the redaction policy applies when logging is added.)
4. [x] Add Room project/context/draft schema, migration export, fake repositories and injectable coroutine dispatchers/clock.
5. [ ] Add CI for formatting/static analysis, JVM tests, lint and debug assembly; configure a device smoke job. (Workflow written; first run failed in action setup before Gradle, see [phase-1-status.md](phase-1-status.md); next run unobserved.)

Acceptance: clean checkout builds with documented SDK/JDK setup; empty/project screens render in Persian RTL and both themes; CI runs real tests. Tests: digit parsing/units, repository round-trip and one Compose navigation test. Do not ship an empty UI as the feature release.

Evidence (2026-10-27): the documented build and the JVM tests pass locally and the debug APK assembles; RTL/theme rendering was not inspected on a device, the Room/Compose device tests have not run and CI has not executed. This acceptance is therefore **not yet met**.

## Phase 2 — Projects and air-test vertical slice

Features F01–F03, F12 and initial F20; APIs A01/A02 guest reads/A12. This is the first executable milestone.

1. Implement versioned reference download/cache and selectors; keep field-level provenance.
2. Implement offline project create/edit/reopen and active context; validate required fields and classify group with approved source fixtures.
3. Implement air-test inputs, automatic volume, local draft persistence and online service evaluation against the captured sample contract.
4. Implement a local calculator only after boundary fixtures/engineering validation; show online/local origin and review-required status.
5. Restore draft after process death; switch between two projects without state leakage; invalidate results when dependent inputs change.
6. Add JSON export for a supported explicit schema and user-selected destination.

Acceptance: create project → set context → calculate sample → see 2.25 h⁻¹ below 3 while still review-required → kill/restart → recover draft → export. Air-sealing aggregate fields survive air-test edits. Offline mode preserves editing and reports unverified online calculations unavailable. Tests: strict equality, height 3 boundary, parser/units, fake server, local persistence, stale results and basic UI journey.

## Phase 3 — Envelope, reference catalogues and assemblies

Features F04–F07; APIs A03–A05/A16/A17 as supported. Depends on stable project/context and versioned references.

1. Introduce reusable layer/material editors, source/geometry evidence and assembly snapshots.
2. Implement walls and wall IDs, insulation/adjacency choices, parallel-path/bridge flags and online calculations.
3. Implement multi-location roofs/floors and ground/perimeter controls; preserve location IDs/revisions.
4. Implement occupancy sections, assignments and context-dependent applicability after exact contract confirmation.
5. Implement assembly library with local revisions and import/export validation; member sync can follow Phase 7.
6. Port verified layer and matrix rules one family at a time, recording parity fixtures and unresolved exceptions.

Acceptance: reopen a multilayer multi-location project without loss; results cite reference/rule versions; unsupported/forbidden table cells do not become numeric defaults. Tests: cm↔m conversion, area/floor/mixed-use boundaries, wall mandatory equality, layer ordering, ground geometry and schema migration/import.

## Phase 4 — Openings and supplemental architecture

Features F08–F11/F13/F14; APIs A06–A14. Depends on wall/occupancy IDs and reference data.

1. Implement openings linked to wall/location, Appendix 9 calculators and optical/thermal constraints.
2. Implement shading with opening references, orientation/geometry and scope controls.
3. Implement daylight rooms/windows/task zones and explicit geometry-completeness states.
4. Implement air-sealing evidence, boundaries and thermal bridges; merge the air-sealing/test aggregate correctly.
5. Wire dependency invalidation for changed walls, openings, context and library revisions.

Acceptance: a complete architectural draft can be entered/reopened/evaluated with honest incomplete states and preserved cross-references. Tests: ratio/boundary matrices, overlapping multiwindow area, invalid references, aggregate merge, and unsupported geometry. Port complex local solvers only with approved golden cases.

## Phase 5 — Systems and chapter evidence

Features F15/F16; APIs A15/A16. Depends on context and stable clause IDs.

1. Reconcile 95-clause versus 255-subclause inventories and version the clause dataset.
2. Build one checklist engine with architecture/mechanical/electrical/renewables filtered views and preserved technical answers.
3. Implement applicability and declared evidence as distinct states; retain hidden/nonapplicable answers without silently applying them.
4. Implement chapter audit result mapping, progress/missing-evidence counts and partial-domain merges.

Acceptance: changes in project/system context update visible controls and stale results consistently; editing one discipline preserves another's evidence. Tests: unknown/NA/declared/review states, clause applicability matrices, shared section merge and corrupt/imported answers.

## Phase 6 — Dossier, import/export and printing

Feature F18 and expanded F20; API A18. Depends on section snapshots/results.

1. Build immutable ReportModel from local snapshots; add server comparison only where verified.
2. Render coverage, envelope/openings/control/system summaries, missing evidence and preliminary approval state.
3. Implement versioned JSON import with preview/validation and explicit replacement/merge decisions; no silent destructive import.
4. Implement A4 PDF/print/share, verifying Persian shaping, line wrapping, tables, page breaks and long projects.
5. Compare local and server dossier coverage/values using approved examples.

Acceptance: exported report is internally consistent and clearly identifies missing/stale inputs; JSON round-trip retains supported data; cancellation leaves project unchanged. Tests: golden report-model snapshots, corrupt/oversized/wrong-edition import, file permissions/cancellation and visual PDF QA.

## Phase 7 — Member sessions, collaboration and synchronization

Features F19, F17 and server aspects of F02–F20; APIs A02–A04/A16–A19. Requires test/staging account and owner-supported auth contracts. Auth investigation can run earlier; implementation integrates here.

1. Implement supported member login/bootstrap, expiry/logout and secure session persistence; verify native/backend cookie and CSRF behavior.
2. Add authorized project download and section/library read/save adapters; enforce role permissions in UI and rely on server enforcement.
3. Add pending-write queue, WorkManager scheduling, acknowledged locks, conflict comparison and timeout reconciliation.
4. Add explicit guest-to-member migration after backend policy is agreed; preserve local backups and identity mapping.
5. Add invitation join/create/removal and reviewer flows only with verified test roles/action values.

Acceptance: two authorized clients editing one section cause a visible conflict with no data loss; account switch cannot upload another account's drafts; expired sessions retain unsynced work. Tests: full fake-server session/conflict/race suite plus staging role and simultaneous-write scenarios. Do not treat a passing mock as proof of production authorization.

## Phase 8 — Hardening and engineering acceptance

Dependencies: all planned release features and closed contract gaps.

1. Engineer reviews rule provenance/golden cases and the numeric-versus-approval semantics across all screens/reports.
2. Exercise offline/reconnect, process death, storage pressure, locale digits, large projects, slow/failing services and migrations from all released schemas.
3. Validate accessibility, RTL, themes, font scale and phone/tablet layouts.
4. Add Macrobenchmark/baseline profiles after measuring startup, large lists and reports; agree regression budgets on representative devices.
5. Review session storage/logs/backups, export exposure, certificate handling and dependency licenses.

Acceptance: the release test matrix passes, no unresolved data-loss/calculation/approval defects, backend compatibility agreed and known limitations documented. Unsupported fifth-edition features remain explicitly out of this release.

## Phase 9 — Release and maintenance

1. Confirm store/distribution route, application ID, signing ownership, target SDK requirements and privacy/support information.
2. Build signed staging/release candidates with CI-managed secrets, R8 rules and real-service staging smoke verification.
3. Conduct user acceptance with representative engineers; check reports and migration recovery on actual devices.
4. Publish only on owner instruction; define rollout, crash triage, rollback/recovery and rule/reference update policy.
5. Track feature/API/rule versions and retain schema migrations and calculation fixtures for future releases.

## Work packaging and estimates

Create small implementation issues per phase/feature with: source/evidence links, exact behavior, dependencies, API contracts, unit/UI tests and acceptance criteria. Suggested first issues: BUILD-01 reproducible scaffold; UI-01 Persian controls; DATA-01 durable projects; REF-01 reference snapshots; CALC-01 air-test fixture; FLOW-01 create/evaluate/recover/export journey.

Estimate after Phase 2 measures form, calculation and backend effort. The feature set and unverified auth/engineering contracts make a fixed delivery date premature. Envelope/openings/checklists can proceed in parallel only after common context/model/contract decisions are stable; reports need stable section models, and member sync needs verified locks/auth.
