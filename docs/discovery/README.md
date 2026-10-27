# Website discovery

Inspected **2026-10-27**. Target: https://mabhas19.ir/. Visible product: Persian building-energy assessment under Mabhas 19, **fourth edition, ۱۳۹۹**. The user confirmed this edition for the first release and asked to defer authenticated/team inspection. No backend source was supplied.

## Findings that shape the app

- This is an engineering workspace, not just a content website: project classification, envelope calculations, references, evidence checklists, collaboration and printable dossiers are central.
- Public guest use is functional. Browser drafts and authenticated server records coexist; guest data does not imply server persistence.
- Nineteen API paths are referenced by shipped clients. Several public reads and one calculation were exercised; authenticated writes and most calculation response contracts remain pending.
- Server-side writes use PHP session cookies, CSRF values and optimistic locks in observed client code. No OAuth, bearer-token or native login contract was established.
- The homepage and calculation screens have different visual treatments. A native design system should unify them while preserving Persian terminology and discipline colors.
- The air-test UI's numeric pass and server's `review_required` result are distinct. A whole-building grade must not be derived from one passing metric.

## Evidence types

| Label | Meaning |
|---|---|
| Runtime | Actual REA browser observation or captured request/response |
| HTTP | Direct bounded GET and returned status/body |
| Source | Publicly served HTML, CSS or JavaScript; proves client implementation, not server execution |
| Proposed | Android/product decision for implementation |
| Unknown | No sufficient evidence yet |

Capture provenance, source URLs and SHA-256 digests are in [manifest.json](../../research/evidence/manifest.json). It records **45 page URLs and 134 asset URLs**, including duplicates/variants and failed routes; these are not 45 distinct screens. A bounded collector followed links only; it did not enumerate hidden backend routes. Its initial run followed a `?reset=1` guest page with no retained account session; the collector was corrected to exclude reset links before reuse. No authenticated project mutation was performed.

## REA evidence ledger

| Investigation | REA Evidence ID | Result and limitation |
|---|---|---|
| Passive homepage | `ev_9b87e8f5108d44ee0e2ac3507e6a1551e3035c7acb26477d7b8d9747130870d9` | DOM/accessibility/scripts/storage key inventory; network starts at attachment |
| Mobile homepage, 390 × 900 | `ev_3b186ef241865b7b968a168df3cefe6dcd9815252fd647049e3ca4a1dc94681f` | Explicit launch/wait scenario; screenshot and network events |
| Architecture start, 1440 × 900 | `ev_6fcf00b0100467f70328cd9587aeabbf8b221be26e14e2cbf438cd916f7e274f` | Explicit launch/wait; populated project context and architecture step navigation |
| Guest air-test sample | `ev_f70d44801dd16726aae404d14897a4ad875f1ff8edb4f4f021b839d767875d64` | REA clicked the sample and calculation buttons; POST 200 with retained JSON |

All three scenarios report successful step outcomes and terminated owned browsers, but overall event completeness is **incomplete** (`missing_sections: ["events"]`); only network events were selected. Do not interpret them as exhaustive runtime coverage. Retained API transaction bodies and screenshots remain usable for their stated observations. Full captures are ignored locally; the reviewed air-test fixture is included in the repository working tree.

An additional REA static homepage-bundle analysis reached its explicitly bounded 45-second total deadline. Its partial Evidence ID is `ev_e0a640e3ffd0b03fa2d95021534d564ea28a8342751693633ce763604c8a593b`; full error/partial evidence is retained locally in `research/local/rea-home-bundle.json`. No completeness claim depends on that analysis. Endpoint inventory was corroborated by direct inspection of the served scripts and selected runtime transactions instead.

## Coverage boundary

| Question | State | Next evidence needed |
|---|---|---|
| Public feature and screen list | Answered within linked fourth-edition surface | Verify final release scope with owner |
| Public design and responsive behavior | Partially answered | Light-mode runtime, all form factors, licensed font/art assets |
| Endpoint names/client request contracts | Answered for served clients | Authenticated traces and all calculation success/error examples |
| Guest air-test result | Answered for one sample | Boundary and invalid cases in staging |
| All mathematical/regulatory rules | Partially answered | Backend source or approved reference/golden cases and engineer review |
| Login, roles, invite lifecycle and revisions | Unresolved | User-controlled test session/staging |
| Fifth edition | Unresolved | Separate access and source contracts |
| Native API support and distribution | Unresolved | Backend owner and product decisions |

See [open questions](../open-questions.md) for concrete next actions. Research is sufficient to plan the whole implementation; it is not a claim that every website workflow has been reverse engineered.
