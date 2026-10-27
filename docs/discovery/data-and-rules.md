# Data, persistence and engineering rules

Evidence is website implementation, not independent confirmation of the regulation. Every rule port needs an edition, source/table/page, boundary behavior, approved fixture and engineering review. See [API contracts](api.md).

## Reference data observed

`GET /api/reference.php` returned 31 provinces, 245 city/station records, 46 occupancies, 262 base material rows, 460 material-catalog entries and 424 component-catalog entries. It also exposes imported Excel candidates, Appendix 8 candidates, audit information and distinct bank versions. These counts refer to different collections and must not be added together as distinct verified materials.

The response labels provenance and verification status. City rows include station IDs, energy degree, dominant need, appendix/page references, aliases and administrative province information. Material rows include λ, density, source page and `verified_v4`; other collections have different verification semantics. Some metadata has older wording alongside later bank-version/audit fields. Preserve those facts and resolve discrepancies with the owner; do not collapse everything into a single “approved” badge.

Proposed reference snapshot metadata: edition, dataset kind, schema/version, content digest, fetched timestamp, provenance and review status. Reference updates must not silently change the basis of an existing report. Store the versions used for each calculation.

## Proposed domain entities

| Entity | Core fields / relationships |
|---|---|
| AccountSession | Account identity, supported auth mechanism, session expiry; sensitive values outside normal Room records |
| Project | Local UUID, optional server ID, edition, owner/account scope, title/code, address, engineer/owner metadata |
| ProjectContext | City/occupancy IDs, conditioned area, floors, group, degree/need, method, target grade, design year, documentation flags |
| OccupancySection | Stable ID, project, occupancy, area, derived group, assigned walls/openings |
| Facade | Direction, opaque/glazed areas, absence status, associated element IDs |
| MaterialReference | Stable reference ID, version, conductivity/resistance, density, source and verification status |
| AssemblyType / Revision | Project/type IDs, kind, immutable versioned layer snapshot and provenance |
| EnvelopeElement | Wall/roof/floor kind, stable location ID, assembly snapshot/revision, adjacency and geometry |
| MaterialLayer | Ordered layer ID, thickness/value/units, material source, geometry and evidence flags |
| Opening / ShadingItem | Opening-to-wall and shading-to-opening links; orientation, geometry and thermal/optical values |
| DaylightRoom / TaskZone | Stable room/zone IDs, windows, geometric basis, lux values, inventory completeness and evidence |
| AirSealing / AirTest | Separate checklist and measured test data, aggregated for the shared remote section |
| ThermalBridge / Boundary | Connection geometry, units, calculation scope and evidence |
| ChecklistEntry | Stable clause/heading ID, discipline, applicability, declared evidence, reviewer state and technical answers |
| SectionDraft | Account/project/edition/section, payload schema, local revision, acknowledged server lock/revision, dirty state |
| CalculationResult | Input/reference/rule digests, numeric result, source (server/local), issues, preliminary/approval flags, stale status |
| DossierSnapshot | Immutable input/result/reference revisions, included sections, completeness, generated timestamp and artifacts |
| PendingWrite / Conflict | Base server revision, local revision/payload, operation ID, retry/outcome state, remote competing revision |

Names are proposed Kotlin/domain concepts, not claims about the backend database schema. Separate DTOs and Room entities from them.

## Browser storage and migration

Observed key families: `m19_v4_guest_projects_v1`, `m19_v4_project_profile_<id>`, `m19_v4_arch_general_guest_v2_<suffix>`, wall guest drafts, `m19_v4_prelim_roof_floor_<id>`, `m19_v4_glazing_<id>`, `m19_v4_daylight_<id>`, `m19_v4_shading_<id>`, `m19_v4_airtightness_<id>`, boundaries/thermal bridges, and `m19_v4_chapter45_step43_<id>`. Exact suffixes vary: `guest`, `default` and project IDs are not interchangeable.

For signed-in accounts, `assets/account-storage59.js` wraps Storage methods and prefixes `m19_v4_` keys with `m19_account_<id>::`. It is account scoping, not encryption or proof of server synchronization. `sessionStorage` shares the prototype wrapping.

Edition spellings include `1399-v4` and `M19-1399-4`. Roof/floor uses `m19_v4_roof_floor_locations_v2`; chapter tracking uses `chapter45_evidence_tracking_step47`; assembly JSON includes its own format. Import must explicitly recognize each supported format and preserve unknown metadata only where safe. A website JSON export is the realistic migration entry point; Android cannot read Chrome's localStorage directly.

Both design air-sealing and measured air-test pages use `m19_v4_airtightness_<project>` and server section `arch_airtightness`. Saving either must merge its owned subfields, not erase the other. Discipline checklists likewise share one chapter aggregate. Model section ownership and merge behavior explicitly.

## Rules discovered and boundary tests to derive

| Topic | Observed implementation | Required verification |
|---|---|---|
| Building group | Occupancy الف: high/medium/low → 1/2/3; ج → 2/3/3; د → 4. ب uses `area > 2000 OR floors > 9`: high → 1 if large else 2; medium → 2 if large else 3; low → 3. Missing/invalid ب area/floors yields unknown. | Full city/use matrix; 1999.99/2000/2000.01 m² and 8/9/10 floors; ensure pure Kotlin equals approved sources. |
| Mixed use | UI cites sections above 150 m². | Resolve exact applicability and assignments; 149.99/150/150.01; no guessed rule port. |
| Layers | λ layer R = `(thickness_cm / 100) / lambda_w_mk`; explicit R alternative. Roof/floor source uses boundary surface resistances and insulation-only ground checks in some branches. | Unit conversion, invalid/unknown λ, zero thickness, series vs parallel paths, no double-counting surface resistances. |
| Wall mandatory threshold | Guest rules expose `mandatory_min_exclusive:0.5`, table 19-4-1. | Equality is not greater-than; validate prescriptive matrices and special null/forbidden cells separately. |
| Roof/floor | Source distinguishes total-layer and insulation-only resistance, arrangement/ground/perimeter columns, groups/grades and missing cells. | Ground height/width thresholds, adjacent uncontrolled space and unknowns; full authoritative table fixture set before offline use. |
| Glazing | Source contains group/grade/need/orientation matrices for U, SHGC bounds and Tv/SHGC; evidence and shading scope affect conclusions. | Stable orientation mapping, upper/lower boundaries, ratio with zero SHGC, both heating/cooling need and inapplicable constraints. |
| Air leakage | At 50 Pa: h ≤ 3 m uses `Q50/V`, h > 3 m uses `Q50/A`. EC/EC+/EC++ limits: 3/1.5/0.75 h⁻¹ or 9/4.5/2.25 m³/(h·m²), strict `<`. | h=3 and just above; exactly at limit; non-50-Pa; zero/missing denominator; area around 5000 m² and subdivision conditions. |
| Daylight | Same-façade rectangular multiwindow union avoids double-counting; unsupported geometry remains incomplete. Coverage and evidence are separate from approval. | Overlap/disjoint windows, no windows, incomplete inventory, obstruction/plan assumptions and unsupported skylights/cross-façade cases. |
| Thermal bridges | Linear ψ and point χ inputs with dimensional/source checks; scope/method can suppress a numeric evaluation. | Correct W/K aggregation, lengths/counts, missing evidence, unsupported coefficient geometry and explicit dimensions. |
| Dossier | Aggregates many independently incomplete sections. | Never equate missing data to pass; preserve stale-result and review-required flags; consistent snapshot across sections. |

These are extraction findings and proposed test cases, not current legal/regulatory advice. The Android implementation should initially call online calculation services and label unverified offline results unavailable. Port one calculation family at a time into pure Kotlin after golden/reference validation.

## Local persistence and sync proposal

Room is the durable draft source read by UI. Commit edits locally, record a local revision, then schedule authorized remote synchronization. Keep the last acknowledged server lock from the base snapshot. Use explicit states: LocalOnly, Clean, PendingUpload, Uploading, Conflict, Failed and OutcomeUnknown. Display whether a result is calculated online/offline and whether its inputs changed.

For a save, snapshot the exact local revision, submit with its base lock, then acknowledge only that snapshot; edits made while a request is running remain dirty. On conflict keep local and remote copies and let the user compare/reload/reapply. Fetching a fresh lock is not permission to overwrite a teammate's changes. After a timeout, reconcile the server state before repeating a potentially non-idempotent write. Member authorization failures must retain local edits and request reauthentication, not create a guest copy silently.
