# Phase 1 verification status

Recorded: **2026-10-27**. Scope: reproducible Gradle skeleton, Persian RTL shell, local guest projects, Room persistence, DataStore theme and the A01 public adapter — the Phase 1 slice of the [implementation plan](implementation-plan.md).

This file records what was executed and observed on one host plus the CI runs that followed. It is not a claim that release builds, visual RTL/a11y checks or the staging service have been verified.

## Host and toolchain actually used

| Item | Value |
|---|---|
| Host | macOS 26.6.2, arm64 |
| Gradle runtime | JDK 21.0.13 (Homebrew `openjdk@21`, `JAVA_HOME=/opt/homebrew/opt/openjdk@21`) |
| Android SDK | `~/Library/Android/sdk` via `ANDROID_HOME`; platforms `android-36`, build-tools `36.0.0` |
| Gradle / AGP / Kotlin | 8.13 / 8.13.2 / 2.2.21 (wrapper, pinned catalogue) |
| Compile / target / min SDK | 36 / 36 / 26 |

Android Studio's bundled JBR **25.0.3** is installed but unusable here: AGP aborts with the bare version string `25.0.3`. A JDK 17–24 runtime is required; Gradle 8.13 itself launches on 25.

## Commands and observed results

```sh
cd android
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export ANDROID_HOME="$HOME/Library/Android/sdk"

./gradlew -PuseGoogleMirror=true :core:model:test
./gradlew -PuseGoogleMirror=true qualityCheck
./gradlew -PuseGoogleMirror=true clean qualityCheck
./gradlew -PuseGoogleMirror=true \
  :core:model:test :core:calculation:test :core:network:test \
  :feature:projects:testDebugUnitTest --rerun-tasks
```

| Check | Result |
|---|---|
| Script/plugin/build configuration | Passes |
| `clean qualityCheck` | `BUILD SUCCESSFUL` — 354 tasks (130 executed, 201 from build cache, 23 up-to-date), no failures |
| Kotlin/JVM unit tests (fresh `--rerun-tasks` run) | 7 tests in 4 classes, 0 failures, 0 skipped |
| — `EngineeringNumberTest` (model) | 2 passed — Persian/Arabic/ASCII digits, grouping, malformed inputs |
| — `LayerResistanceTest` (calculation) | 2 passed — cm→m resistance, zero conductivity rejected |
| — `PublicApiTest` (network) | 1 passed — MockWebServer GET `/api/reference.php`, application-level error preserved |
| — `ProjectsViewModelTest` (feature) | 2 passed — invalid title writes nothing, storage failure keeps input, single save |
| ktlint (`android_studio` style) | 10 module checks pass |
| Detekt (root `config/detekt.yml`) | 10 module checks pass |
| Android Lint (`warningsAsErrors`) | 5 `lintDebug` checks pass |
| `:app:assembleDebug` | `app-debug.apk` produced, 13,979,691 bytes |
| Debug instrumentation APK assembly | 5 modules (`app`, `core:data`, `core:database`, `core:designsystem`, `feature:projects`) build `assembleDebugAndroidTest`; 487-task `qualityCheck` green |
| Room schema export | `core/database/schemas/ir.mabhas19.core.database.MabhasDatabase/1.json` (3 entity tables), committed for migration tests |
| Physical device, SM-A546E (Galaxy A54 5G), Android 14 / API 34, arm64-v8a, 1080x2340, attached over wireless debugging | All 3 instrumentation suites pass (`connectedDebugAndroidTest`: app navigation, Room reopen round-trip, draft-conflict). The installed debug build renders the workspace home and project list in Persian RTL under the dark theme, with no crash from the app package |
| CI run [40510309053](https://github.com/alirezaakj/Mabhas19/actions/runs/40510309053) (`75a457b`) | Both jobs **success**: `quality` ran `qualityCheck` on `ubuntu-24.04`; `device-smoke` booted an API 36 `google_apis` x86_64 emulator and ran `connectedDebugAndroidTest` (the androidTest report artifact was produced, so the suite executed rather than being skipped) |

No test contacts the production website. The A01 adapter was exercised only against MockWebServer.

## Workspace home added (F01 surface)

On 2026-10-27 the app's start destination became a native workspace mirroring the observed website main page instead of the bare project list. Source: `feature/home` (new module), wired in `app/MabhasNavigation.kt`.

- Live state: theme switch, guest/local notices, project count, the three most recently updated local projects, active-project card, and every navigation path into existing screening (project list, create project, project detail).
- Mirrored static content: edition eyebrow and «ویرایش ۴» badge, five discipline cards with the site's titles/descriptions/action labels, five quick-access rows, the three overview cells, and the footer — all as Persian string resources in `feature/home`.
- Deliberate gaps: the website building photo, motion aurora and Vazirmatn font are **not** bundled (rights/hosting unresolved); climate/province/occupancy chips are omitted because the app has no reference-data session yet, so no invented البرز/کرج values are shown; discipline, dossier, team, dashboard and login cards open an explicit “این بخش هنوز در برنامه نیست” screen (`SectionPlaceholderScreen`) rather than a fake form or a simulated calculation.
- Theme tokens were retuned to the observed home palette (`#040507`/`#00DF81`/`#F2AA22` dark, `#F5F7FA`/`#009B60`/`#B96C00` light) in `core/designsystem`.

Verification for this change:

```sh
./gradlew --offline -PuseGoogleMirror=true qualityCheck
./gradlew --offline -PuseGoogleMirror=true :app:connectedDebugAndroidTest
```

| Check | Result |
|---|---|
| `qualityCheck` | `BUILD SUCCESSFUL` — 571 tasks; ktlint, Detekt, Android Lint, JVM/unit tests and both debug APK assemblies pass |
| `feature:home` unit tests | 4 passed — section keys/order/unknown key, Persian digit formatting |
| `:app:connectedDebugAndroidTest` on SM-A546E | 2/2 pass — home is the start screen; create→detail→back→project list; section card→placeholder→back |
| Visual inspection (SM-A546E, 1080x2340, API 34) | Dark and light home screens checked by scrolling; RTL order, accent cards, Persian strings and the bottom cards render. Captures kept locally only (`research/local/device-home-{dark,light,bottom}.png`, git-ignored) |
| Not checked | 200% font scale, TalkBack semantics, landscape/tablet, and the 3-across overview row on small-width phones |

## Environment problems found and how the build was made green

1. **Google Maven unreachable from this host.** Every `dl.google.com/dl/android/maven2/...` request returns 404, including for known-old artifacts, so no AndroidX/AGP artifact can be fetched directly. The documented opt-in `-PuseGoogleMirror=true` is therefore required here.
2. **The mirror poisons plugin resolution.** The Aliyun Google proxy answers unknown coordinates with HTTP **502**, not 404, and Gradle treats that as fatal instead of falling through. The mirror repository is now restricted with `content { includeGroupByRegex(...) }` to Google-Maven groups (`androidx.*`, `com.android[.*]`, `com.google.android[.*]`, `com.google.testing.*`); Maven Central and the Plugin Portal resolve their own artifacts normally. See `android/settings.gradle.kts` and `android/build-logic/settings.gradle.kts`.
3. **AGP wanted build-tools 35.0.0** while only 36.0.0 is installed and CI installs only 36.0.0. `buildToolsVersion = "36.0.0"` is now pinned in the shared conventions; this would have failed CI too.
4. **Root script compiled against a version-catalog accessor that is not visible inside `subprojects {}`.** The ktlint version now comes from `VersionCatalogsExtension` on the root project.
5. **Formatting drift.** `./gradlew ktlintFormat` applied the `android_studio` style (statement wrapping, no trailing commas). Code was formatted, not rewritten.
6. **Lint failed on version-availability notices** because `warningsAsErrors = true`. Deliberately pinned versions are exempted in `android/config/lint.xml`; dependency upgrades stay a separate, tested change. Genuine findings were fixed instead: backup extraction rules added for API 31+ (`data_extraction_rules.xml`) with the pre-12 equivalent (`backup_rules.xml`), and an unused string removed.
7. **The instrumentation tests never compiled.** `RoomDatabase` is not a `java.io.Closeable`, so `.use { }` in the `core:data` and `core:database` tests failed. This blocked the first real device run before any test executed and was invisible to `qualityCheck`, which only built production sources. The tests now close the database in `finally`, and `qualityCheck` depends on every module's `assembleDebugAndroidTest`.
8. **The Compose test timed out on real hardware.** The 5 s `waitUntil` was enough on the CI emulator but not after IME input on the SM-A546E, so the test flaked once before passing; the timeout is now 15 s.
9. **CI itself failed twice before it could verify anything.** Run [40507549888](https://github.com/alirezaakj/Mabhas19/actions/runs/40507549888) died inside `setup-android@v3` and the emulator step on `ubuntu-latest`; run [40508748666](https://github.com/alirezaakj/Mabhas19/actions/runs/40508748666) passed `quality` but the device job still failed on the androidTest compile error above. The workflow now pins `ubuntu-24.04` and current action majors, and run 40510309053 is green.

## Not verified

| Item | Why | Follow-up |
|---|---|---|
| Release build, R8 shrinking and signing | Out of Phase 1 scope; no signing configuration exists | Phase 9 |
| Visual RTL, light theme, font scale, TalkBack | Dark- and light-theme RTL of the workspace home and project list were inspected on the SM-A546E; 200% font scale, landscape/tablet layouts and TalkBack semantics have not been checked, and the Compose tests assert navigation/text rather than layout direction or contrast | Recheck the remaining combinations on device |
| Room migration upgrade path | Only schema version 1 exists, so there is nothing to migrate yet | Add migration tests with the first schema change |
| `main` history | Phase 1 landed as `c568281`, `c4d43b5`, `469506d` and `75a457b` on 2026-10-27 | Review the commits |

## Consequence for the plan

Phase 1 build items 1–4 are evidenced above and item 5 is now demonstrated by a green CI run that executed both the quality job and the emulator suite. The remaining Phase 1 acceptance gap is human inspection of font scaling, adaptive layouts and TalkBack on a device (dark- and light-theme RTL are now confirmed on the SM-A546E). Phase 1 is therefore **not** fully accepted, and Phase 2 should not be described as started.
