# Feature catalogue and screen inventory

Evidence: public HTML and shipped JavaScript in [manifest](../../research/evidence/manifest.json), plus REA captures listed in the [discovery ledger](README.md). “Observed” below means visible UI/source unless explicitly called runtime-tested. Priorities are proposed release sequencing: P0 foundation, P1 first full engineering release, P2 collaboration/reviewer completion.

| ID | Feature / website entry | Extracted behavior and Android acceptance target | Priority |
|---|---|---|---|
| F01 | Workspace `/`, `/index.php` | Edition identity, active project/climate/group, discipline entry cards, theme and account entry. Android home shows correct project context and draft/sync status. | P0 |
| F02 | Projects `/projects.php` | Create/edit title, code, province/city, address, occupancy, area/floors, owner, architect/license, mechanical/electrical engineers, permit number. Guest browser records; member server path. Preserve an offline draft across app death and reopen it. | P0 |
| F03 | General `/architecture/arch_general.php` | City/occupancy classification, energy degree, dominant need, group 1–4, design method, target grade EC/EC+/EC++, year, inertia, solar utilization and façade areas. Changing context invalidates dependent results. | P0 |
| F04 | Occupancy `/architecture/arch_occupancy.php` | Separate uses/sections and assign envelope elements; UI cites a mixed-use boundary above 150 m². Keep stable section IDs and show pending applicability until verified. | P1 |
| F05 | Walls `/architecture/arch_walls.php` | Multiple wall IDs, adjacency, insulation placement, material layers, thickness/λ or explicit R, source/geometry evidence, thermal-bridge and parallel-path flags; calculate and save summaries. | P1 |
| F06 | Roof/floor `/architecture/arch_roof_floor.php` | Multiple locations, roof/uncontrolled ceiling/floor over air/uncontrolled space/ground, reusable assembly, insulation scheme, level and perimeter width. Source declares up to 60 locations. | P1 |
| F07 | Materials and assemblies `/architecture/arch_assemblies.php?group=wall` or `roof` | Search material/component catalogues; reusable type IDs/revisions, layers, import/export and optional member library sync. Source enforces 200 local types. | P1 |
| F08 | Glazing `/architecture/arch_glazing.php` | Opening IDs, parent wall association, orientation, area, frame/glass thermal values, Uw/SHGC/Tv and ratio controls; Appendix 9 calculators and geometry/source evidence. | P1 |
| F09 | Shading `/architecture/arch_shading.php` | Link opening, direction/type, projection dimensions, sun angles/day/hour, drawing reference and scope. Numeric geometry does not alone establish a solar exception. | P1 |
| F10 | Daylight `/architecture/arch_daylight.php` | Rooms and windows, single/same-façade multiwindow geometry, reflectance, obstacles, task plane, lux bands, zone lighting/uniformity and document references. Source limits imported rooms/zones to 60. | P1 |
| F11 | Air-sealing design `/architecture/arch_airtightness.php` | Eleven sensitive-joint checklist items: yes/no/na/unknown, drawing/evidence and comment. Distinct from measured air leakage, but shares aggregate storage. | P1 |
| F12 | Air test `/supervision/airtightness.php` | Pressure/airflow/volume/area/height, EC class, subdivision, report reference; n50 or q50 and strict limit. Sample and calculate runtime-tested. | P0 vertical slice |
| F13 | Thermal bridges `/architecture/arch_thermal_bridges.php` | Connection type/count/length, ψ/χ, coefficient source and detail, area/base U, dimensions and calculation scope; preliminary results. | P1 |
| F14 | Boundaries `/architecture/arch_boundaries.php` | Adjacent walls and space connections, document declared conditions and evaluate separation. Exact result schema pending. | P1 |
| F15 | Discipline checklists `/architecture/arch_checklists.php?domain=architecture/mechanical/electrical/renewables` | The Android mechanical slice now presents the source page's six mechanical topics, 25 mechanical-view clauses (including its injected evaporative-cooler control), and 41 mechanical technical fields. Clause answers and evidence notes are a project-scoped local projection in `arch_chapter45`; applicability and calculations remain unknown until their shared context/engine contracts are implemented. Electrical and renewables views remain planned. | P1 |
| F16 | Full chapter controls `/architecture/arch_chapter45.php` | Chapter 4/5 technical/evidence evaluations. UI describes 95 clauses and 255 atomic subclauses in different views; reconcile stable IDs and counts before seeding Android. | P1 |
| F17 | Reviewer `/architecture/arch_requirements.php?mode=controller` | Reviewer state, drawing/document reference, note and printable controls. Four visible states: awaiting review, evidence registered, not applicable, needs correction. Role enforcement and save contract unverified. | P2 |
| F18 | Dossier `/architecture/arch_dossier.php` | Aggregate browser drafts or server version into A4 sheets; JSON export, print and audit link. Coverage, missing information and evidence status belong in the report. Server guest GET returns 403. | P1 |
| F19 | Team `/team.php` | Member login required; invitation code joins a project/discipline, role-based invitation/removal client handlers. Actual authenticated workflow not exercised. | P2 |
| F20 | Drafts/import/export/theme/cross-cutting | Local save, explicit server upload/download, JSON imports/exports, print, responsive RTL, active project propagation and account-scoped storage. Android needs file picker/sharing, draft recovery, explicit conflict UI and accessible themes. | P0–P2 |

## Main journeys

1. Guest creates project → selects city/use/area/floors → selects design method/grade → edits envelope and systems → evaluates → fixes missing evidence → exports dossier.
2. Returning guest opens local project → recovers all drafts → recalculates stale results after context changes → exports JSON backup.
3. Member signs in → lists authorized projects → downloads section/revision → edits locally → submits with expected lock → handles conflict → regenerates report. Pending authenticated validation.
4. Collaborator joins an authorized invitation → edits only assigned discipline → shared dossier updates. Pending test account and explicit invitation scope.
5. Reviewer opens dossier → examines evidence and incomplete controls → records review status without treating self-declaration as approval. Pending role validation.

## State and navigation requirements

Every engineering screen needs loading, editable draft, validation errors, calculating, numeric result, needs review, saved locally, sync pending, conflict and unavailable states as applicable. Preserve the last valid draft on errors. Switching city, occupancy, grade, method or referenced assembly must mark dependent results stale.

Proposed Android navigation: Projects → Project workspace → Architecture / Systems / Air test / Dossier / Team. Architecture retains the website's six-step grouping: Start, Walls, Roof/floor, Windows, Supplemental controls, Dossier. On phones use hierarchical screens and repeatable item editors; on tablets use list/detail panes.

The website links `/dashboard.php` and `/login.php`, both returning 404 in this inspection; `/architecture/arch_report.php?project_id=guest` returned 403. Preserve these as unresolved account/audit flows, not working native destinations.
