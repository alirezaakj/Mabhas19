# Native architecture workspace (F03–F14 / F20)

Implemented on 2026-10-27 under `android/feature/architecture`. The home card
«پوسته خارجی (معماری)» now opens the native workspace instead of a placeholder.
It keeps the website's six-step grouping and fourth-edition identity.

## Source and behavior

Public guest HTML and served JavaScript were retrieved from
`https://www.mabhas19.ir/architecture/` and its linked assets. The `www` host was
reachable using Windows curl with ordinary certificate validation; the bare
host failed TLS from this Windows environment. Raw files remain ignored under
`research/local/architecture/`. The existing reviewed
[website screenshot](../../research/evidence/screenshots/architecture-desktop.png)
provides the visual reference. [Capture provenance](../../research/evidence/architecture-workspace-sources.json)
records the selected public source digests.

REA 6.8.0 statically analyzed the four selected general/wall/roof/glazing scripts.
Parent Evidence ID:
`ev_55bfd229eef55c4b3c1ca13d9862068e65fa488e18cb9ca9932546409e689ebb`.
The complete local Evidence bundle is ignored; analysis did not execute page
JavaScript or establish runtime backend behavior. Chrome launch/debugging
commands were rejected by the client's automatic approval policy, without a
reason. Browser-panel navigation separately failed with `ERR_CONNECTION_CLOSED`.
An interactive Chrome parity comparison has therefore not been completed.

| Surface | Native behavior |
|---|---|
| Start / conditions (F03) | 31 provinces, all 245 source cities and 46 occupancies; area/floors, source-derived building group, design method/year/grade, solar geometry, continuity, inertia/evidence and four façade WWR editors |
| Occupancy (F04) | Repeatable occupancy sections and envelope assignments |
| Walls (F05) | Repeatable walls/layers, adjacency/insulation, thermal coefficients, source/product/geometry evidence, drawing checks and layer-only resistance preview |
| Roof/floor (F06) | Repeatable locations, source surface/ground/perimeter controls, layers and evidence |
| Windows (F08) | Repeatable openings linked by wall ID, orientation, area, U/SHGC/Tv and source fields |
| Supplemental controls (F09–F14) | Shading geometry/applicability, daylight rooms/windows, eleven-point air-sealing checklist, thermal bridges and boundary rows |
| Dossier (F18 partial) | Local context and entered-section summary; not the website's complete printable/calculated dossier |

The form is native Compose, Persian RTL, with light workspace colors and green
step controls. Phone layouts stack fields instead of copying the desktop's
wide rows. The website's complete material/product picker, font/brand imagery,
full clause inventory, assembly library, import/export and printable reports
are not implemented here. This is not a claim of exact feature or pixel parity.

## Persistence and sample

The user explicitly selects or creates a local project before editing. The
native `android_architecture_v1` payload is scoped by project, edition and
section using the existing Room draft repository and revision checks. It is
not a server-compatible import format. Valid city/use/area/floor context is also
written to the project's shared context. Edits autosave after a short pause;
the Save action retries explicitly. Unsupported schema/edition data is not
overwritten. Failed writes and conflicts retain the editor contents.

«ساخت و تکمیل نمونه» creates a separate, clearly labeled educational project.
Its base context uses the reviewed screenshot's Karaj/c181, residential/o01,
1000 m² and four floors. Additional geometry, layers and section entries are
synthetic examples. Checklists/evidence remain unconfirmed; the sample is not
a real building record or a known compliant engineering fixture.

## Calculations and limits

`ArchitectureRules` ports observed browser-side preliminary screening, with
canonical Persian/Arabic numeric parsing. The source catalogue is not an
independently approved regulatory dataset. Missing obstruction angle stays
unknown (a conservative difference from the browser, which permits a blank
angle). Exact WWR 40% does not pass the strictly-less-than condition; the solar
condition uses strictly greater than 1/9 and strictly less than 25 degrees.

A04–A14 adapters produce guest calculation requests only, using ordinary TLS.
Wall requests force `request=evaluate`. No project save, member mutation, CSRF
or login is invented. Input validation prevents incomplete/invalid values from
silently becoming zero. Responses remain separate from drafts and become stale
when inputs change; delayed responses cannot attach to another project.

- Multiple roof/floor locations can be edited and saved, but online evaluation
  currently requires exactly one location.
- Daylight requests cover the source prescriptive room mode, not every
  mandatory/zone solver.
- Air-sealing evidence is distinct from measured pressure/airflow; absent
  measurements stay unknown.
- Layer resistance excludes surface films, thermal bridges and ground effects.
- Method availability is preliminary; chapter 6/7/8 solvers and full engineering
  acceptance remain outside this change.
- Final regulatory approval and energy certification are never inferred from
  a numerical pass or from completing a sample.

## Windows development and physical preview

Use JDK 21 and SDK platform/build-tools 36. The ignored `android/local.properties`
records the local SDK location. See [build instructions](../../android/BUILDING.md).

The phone's preinstalled debug app has a different signing certificate from
this Windows checkout. Build a separate preview without deleting its data:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
cd android
.\gradlew.bat '-PuseGoogleMirror=true' '-PdebugApplicationIdSuffix=.preview' qualityCheck
```

The default debug ID remains `ir.mabhas19.android.debug`; the optional suffix
produces `ir.mabhas19.android.preview`. Release configuration is unchanged.
The main activity accepts `open_architecture=true` to open the workspace.

## Verification of this change

The final preview build passed `qualityCheck` with the options above: ktlint,
Detekt, Android Lint, debug APK/instrumentation assembly and 47 unit tests
(nine suites, zero failures/errors). The ignored local build log is
`research/local/architecture-quality-verified.log`.

On the paired Samsung Galaxy A06 (SM-A065F, Android API 36), the app's three
navigation tests, the database test and the data repository test all passed
using `:app:connectedDebugAndroidTest`,
`:core:database:connectedDebugAndroidTest` and
`:core:data:connectedDebugAndroidTest`. The ignored local log is
`research/local/architecture-device-tests.log`. Automated tests use fakes and
do not call the production calculation service.

The final APK was installed as `ir.mabhas19.android.preview` and cold-launched
on that phone. A separately labeled educational sample was created through the
native UI. Manual inspection confirmed the wall's WL-001 identifier, title,
adjacency, insulation position and entered layer values. After force-stop and
cold launch, selecting the existing sample restored Karaj/c181, residential/o01,
1000 m², four floors and preliminary group 2. The filled context was left open
on the phone. Local screenshots remain under ignored `research/local/`.

Research validation and `git diff --check` passed. Interactive Chrome parity,
live service-response parity, TalkBack, 200% font scaling and tablet layouts
remain unverified. This evidence validates the implemented preview; it does
not close the feature gaps listed above.
