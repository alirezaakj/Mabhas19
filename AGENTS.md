# Repository instructions

## Purpose and current stage

Build a native Kotlin Android client for https://mabhas19.ir/. This repository contains discovery evidence, a runnable Phase 1 Android scaffold, and a native architecture workspace. Read README.md, docs/discovery/README.md, docs/android/implementation-plan.md and docs/open-questions.md before implementation. Report verification from the current checkout; prior build or CI evidence does not validate new work.

## Evidence and scope

- Preserve the distinction between observed runtime behavior, shipped-client source, inferred behavior, proposed app decisions and unknowns.
- Use stable feature IDs F01–F20 and API IDs A01–A19 in changes and tests. Update the discovery and API inventory when new evidence changes a contract.
- Default scope is edition 1399-v4. Do not mix fourth- and fifth-edition tables, saved projects, rules or claims. Normalize alternate edition identifiers explicitly at API/import boundaries.
- For website investigation, read `.agents/skills/reverse-engineer-anything/SKILL.md` and its relevant references. Use the installed REA CLI or available MCP tools. CLI use does not require global Codex registration.
- Public/guest exploration is authorized. Use isolated browser profiles and synthetic guest samples. Request user interaction when login, CAPTCHA, a role-specific account or an actual invitation is required. Do not guess credentials or project IDs.
- Do not create/change authenticated projects, invitations or membership just to inspect a page without user-authorized test scope. GET routes can mutate state: never blindly follow reset/delete/logout/action links.
- Keep cookies, CSRF tokens, passwords, personal project data, HARs and complete browser captures in ignored `research/local/`. Publish only reviewed, redacted evidence. Never embed a session or secret in tests.

## Android implementation conventions (apply when scaffold exists)

- Follow `docs/android/architecture.md`: Kotlin, Compose, state-driven UI, coroutines/Flow, repositories, Room, constructor injection and a pure JVM calculation module.
- Business rules belong outside composables/ViewModels. Keep API DTOs, local entities and domain models separate; preserve null/unknown semantics.
- Use explicit units and canonical numeric values. Persian display strings must never enter arithmetic or JSON as formatted numbers. Support Persian/Arabic input normalization.
- Project/account/edition identity must scope every draft, cache, calculation, pending write and report. Switching projects/accounts must not leak state.
- Preserve preliminary/review-required/not-applicable/unknown results. A numeric pass is not certification. Honor `not_a_certificate`, `regulatory_approval` and `building_energy_grade_approved`.
- Store locally before remote sync. Use server `expected_lock_version`; do not overwrite conflicts, refresh away a stale lock, or silently fall back to guest mode after a member write fails.
- Use `start`/`end` layout semantics and Persian string resources. Check RTL, mixed-direction units, accessibility and large fonts for each feature.
- Pin compatible build dependencies in the version catalogue when scaffolding. Commit Gradle wrapper and Room schemas; keep local.properties and signing keys ignored.
- No production network in automated tests. Use fakes and MockWebServer. Keep all live smoke tests explicit and read-only unless a designated staging test allows mutation.

## Verification and delivery

For documentation/research changes, validate internal Markdown links, JSON files and Python syntax; inspect published screenshots and scan for secrets. For Android changes, run the smallest relevant unit/integration/UI checks and the CI checks defined in `docs/android/testing.md`. Report exactly what ran and what remains blocked. Do not publish, push or release unless the user requests it.
