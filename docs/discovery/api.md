# API inventory and integration contract

Base observed: `https://mabhas19.ir/api/`. Discovery date: 2026-10-27. These are PHP JSON endpoints, not a verified versioned REST API. Names/methods come from served JavaScript; a listed method does not establish public access, idempotency, or a complete backend contract.

Evidence: [source manifest](../../research/evidence/manifest.json), direct GET summaries below, and [captured air-test transaction](../../research/evidence/airtightness-sample.json). Paths resolve relative to the site root. No authenticated writes were submitted.

## Endpoint inventory

| ID | Path | Client methods / inputs | Known outputs and evidence status |
|---|---|---|---|
| A01 | `reference.php` | GET | HTTP 200 `{ok, reference}`; city, occupancy and material/component catalogues with provenance/version metadata. Runtime request also seen from mobile home. |
| A02 | `projects.php` | GET; POST `{id?, title, project_code, province, city, address, use_type, total_area_m2, floors_count, engineer_name, engineer_license, mech_engineer, elec_engineer, owner_name, map_permit_no, _csrf}` | GET 200 `{ok:true,mode:"guest",projects:[]}`. Member list/save shapes and permissions pending. Source: `assets/projects.js`. |
| A03 | `arch_general.php` | GET `project_id?`; POST `{_csrf,project_id,expected_lock_version,payload}` | Guest GET 200 `{ok:true,mode:"guest"}`. Member client reads saved payload, lock and CSRF; complete member schema pending. Source: `assets/arch-general.js`. |
| A04 | `arch_walls.php` | GET `project_id?`; POST `{request:"evaluate" or "save",payload,project_id?,_csrf?,expected_lock_version?}` | Guest GET 200 `{ok,mode,rules,csrf}`. Client expects `{result,saved?}`. Guest uses evaluate even when local Save clicked; do not infer server save from UI label. |
| A05 | `roof_floor.php` | POST active assembly payload: context, surface, insulation, ground geometry, layers | Source expects `{ok,result}`. Result not runtime-validated. Source: `assets/roof-floor.js`. Multi-location draft storage is separate A16. |
| A06 | `arch_glazing.php` | POST `{city_id,occupancy_id,net_area_m2,floor_count,target_grade,dominant_need,dominant_need_source,openings}` | Source expects `{ok,result}`. Source: `assets/arch-glazing.js`. |
| A07 | `appendix9_window.php` | POST `{frame_type,opening_type,U_frame_W_m2K or lambda_frame_W_mK,U_glass_W_m2K}` | Conditional frame parameter; source-driven calculator, runtime contract pending. |
| A08 | `appendix9_glass.php` | POST `{table,air_gap_mm,emissivity}`; emissivity can be null for clear glass | Source-driven calculator, runtime contract pending. |
| A09 | `arch_shading.php` | POST `{items:[{window_id,orientation,type,shading_installed,mandatory_for_window,win_u,win_shgc,win_tv,glare_status,control_day,control_hour,window_height_m,depth_m,top_gap_m,profile_angle_deg,drawing_ref,sun_source,note}]}` | Source expects numeric/preliminary result. Source: `assets/arch-shading.js`; exact schema pending. |
| A10 | `arch_shading_scope.php` | POST `{city_id,occupancy_id,net_area_m2,floor_count,target_grade,design_method,openings}`; opening fields include `opening_id,orientation,adjacency,U_wm2k,SHGC,Tv` and optional `appendix10_shading_requested` | Source candidate for shading applicability; response accessed as `{ok,result:{dominant_need_verified,building_group,issues,openings}}`. Runtime success/error still pending. Source: `assets/arch-shading69.js`, `assets/arch-glazing-scope69.js`. |
| A11 | `arch_daylight.php` | POST mode-specific payload: `prescriptive`, `mandatory`, or `mandatory_zones` | Client rendering reads coverage, rooms, issues, numerical completeness; geometry unsupported cases remain unresolved, not zero. Source: `assets/arch-daylight.js`. |
| A12 | `arch_airtightness.php` | POST `{edition,entries,design,test}` | **Runtime POST 200**. `{ok,result}` includes items/counts/test/normative_precheck/overall and explicit approval flags. See worked example. |
| A13 | `arch_thermal_bridges.php` | POST `{method,discontinuous_insulation,balance_exterior_reference,dimension_basis,length_unit,calculation_requested,envelope_area_m2,base_u,bridges}` | Source expects `{ok,result}`. Bridge rows include count/length/ψ/χ/source/reference/drawing. Source: `assets/arch-thermal-bridges.js`. |
| A14 | `arch_boundaries.php` | POST `{adjacent_walls,space_connections}` | Source expects `{ok,result}`. Source: `assets/arch-boundaries.js`. |
| A15 | `arch_chapter45.php` | POST `{edition:"1399-v4",project_id,drafts}` with chapter/context/evidence/systems and other sections | Source reads `chapter45_atomic_audit.rules` and `universal_context_gate`; do not assume universal `{result}` wrapper. Source: `assets/arch-chapter45.js`, `assets/arch-checklists.js`. |
| A16 | `architecture_drafts.php` | GET `project_id,section`; POST `{project_id,section,payload,expected_lock_version,_csrf}` | Guest GET 200 `{ok:true,mode:"guest",server_storage:false,error:null}`. Client member GET uses `{mode,csrf,draft:{payload,lock_version,revision,saved_at}}`; POST uses `{saved,...}`. Authenticated shapes pending. |
| A17 | `architecture_assemblies.php` | GET `project_id`; POST `{project_id,library,expected_lock_version,_csrf}` | Guest GET 200 `{ok:true,mode:"guest",server_storage:false,error:null}`. Member library GET/save and lock flow in `assets/v4-assembly-server.js`, not runtime-tested. |
| A18 | `architecture_dossier.php` | GET `project_id` for saved server dossier; POST `{edition:"1399-v4",project_id,drafts}` for browser aggregation | Guest server GET **403** `{ok:false,error:...}`. Source shows browser-draft POST but it was not exercised. Source: `assets/arch-dossier.js`. |
| A19 | `team.php` | GET session metadata; POST `{action,code? or project_id/role/user_id,_csrf}` | Guest GET **422** with login message, not assumed 401. Source join action confirmed; other action values originate in authenticated page attributes and need capture. Source: `assets/team59.js`. |

## Request and result details

General-design payload includes `design_method`, `city_id`, `occupancy_id`, `target_grade`, `design_year`, `ministerial_min_grade`, `solar_utilization`, `se_to_sw_glazing_area_m2`, `solar_obstacle_angle_deg`, `usage_continuity`, `net_conditioned_area_m2`, `controlled_floor_count`, `thermal_inertia`, `inertia_documented`, `v4_climate_documented`, `no_ministerial_restriction_confirmed`, and directional `facades` with façade/glazing areas and absent flags. These are source-derived field names, not a validated OpenAPI schema.

Walls use `building_group`, `target_grade`, `city_id`, `occupancy_id`, `net_area_m2`, `floor_count`, `group_documented`, `wall_checklist`, and `walls[]`. Each wall has stable ID, assembly ID/revision, adjacency/insulation, evidence flags and layers. Layer fields include material ID, Appendix 8 ID, thickness in cm, λ in W/(m·K), resistance in m²·K/W, geometry inputs and source references. Preserve API-specific names through mappers; do not globally rename all area fields.

Draft section keys observed in served clients: `arch_roof_floor`, `arch_glazing`, `arch_daylight`, `arch_shading`, `arch_airtightness`, `arch_boundaries`, `arch_thermal_bridges`, `arch_chapter45`; occupancy also uses section-based draft code and requires focused schema confirmation. General/walls have their own persistence APIs. Assembly library has its own API and format.

The sync dock enforces source-side serialized byte limits of **425,000** for roof/floor and **56,000** for other mapped sections. These are browser checks, not independently verified server maxima. Validate UTF-8 byte counts, not character counts; get server limits before locking Android import/write policy.

## Captured air-test example

Actual request from the website's sample button, isolated guest browser:

```json
{
  "edition": "1399-v4",
  "entries": {},
  "design": {
    "energy_class": "EC",
    "controlled_area_m2": 1000,
    "average_height_m": 3,
    "subzone_test": "no"
  },
  "test": {
    "pressure_pa": 50,
    "airflow_m3h": 6750,
    "volume_m3": 3000,
    "source": "AIR-1404-01"
  }
}
```

Response subset: `ok:true`; `result.test.ach_at_test_pressure:2.25`; `result.normative_precheck.selected_metric:"volumetric_n50"`; `calculated_value:2.25`; `limit:3`; `strict_comparison:"<"`; `numeric_precheck:"below_limit"`; `regulatory_approval:false`; `result.overall:"review_required"`; `result.building_energy_grade_approved:false`. Eleven air-sealing entries remain unknown. The [full public fixture](../../research/evidence/airtightness-sample.json) preserves exact fields.

## Authentication, errors and concurrency

- HTTP responses set a secure PHP session cookie. Clients send `credentials:'same-origin'`. A native client needs an explicitly supported login/session bootstrap; a browser sign-in does not automatically authenticate OkHttp.
- CSRF values are returned by some reads and submitted in JSON `_csrf` for mutations. Treat as session-scoped; never commit captured values. No refresh-token or mobile bearer scheme was observed.
- Optimistic writes carry `expected_lock_version`; successful writes update revision/lock. Exact stale-write status/body is **unknown**, so tests should introduce captured fixtures before assuming HTTP 409.
- `ok:false` and `error` can occur with non-2xx; guest `server_storage:false` can occur with 200. Parse transport and application status separately. Detect HTML/login redirects instead of decoding them as JSON.
- Do not assume all POSTs are pure. Distinguish calculate/evaluate from save; retries of writes require a confirmed idempotency policy. A timed-out write is an uncertain outcome.

## Contract completion checklist

For each A01–A19: record auth/role, exact request, successful JSON, missing/invalid inputs, empty/null values, denied access, concurrency behavior, version/size limits and whether it mutates data. Obtain owner guidance for rate limits, supported app API base/version, TLS/network behavior, auth/session expiration and backward compatibility. Convert verified contracts to reviewed DTOs and MockWebServer fixtures; do not generate speculative OpenAPI as a claimed backend specification.
