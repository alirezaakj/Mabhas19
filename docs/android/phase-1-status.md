# Phase 1 verification status

Recorded: **2026-10-27**. Scope: reproducible Gradle skeleton, Persian RTL shell, local guest projects, Room persistence, DataStore theme and the A01 public adapter — the Phase 1 slice of the [implementation plan](implementation-plan.md).

This file records what was executed and observed on one host. It is not a claim that CI, device tests or release builds pass.

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
| Room schema export | `core/database/schemas/ir.mabhas19.core.database.MabhasDatabase/1.json` (3 entity tables), committed for migration tests |

No test contacts the production website. The A01 adapter was exercised only against MockWebServer.

## Environment problems found and how the build was made green

1. **Google Maven unreachable from this host.** Every `dl.google.com/dl/android/maven2/...` request returns 404, including for known-old artifacts, so no AndroidX/AGP artifact can be fetched directly. The documented opt-in `-PuseGoogleMirror=true` is therefore required here.
2. **The mirror poisons plugin resolution.** The Aliyun Google proxy answers unknown coordinates with HTTP **502**, not 404, and Gradle treats that as fatal instead of falling through. The mirror repository is now restricted with `content { includeGroupByRegex(...) }` to Google-Maven groups (`androidx.*`, `com.android[.*]`, `com.google.android[.*]`, `com.google.testing.*`); Maven Central and the Plugin Portal resolve their own artifacts normally. See `android/settings.gradle.kts` and `android/build-logic/settings.gradle.kts`.
3. **AGP wanted build-tools 35.0.0** while only 36.0.0 is installed and CI installs only 36.0.0. `buildToolsVersion = "36.0.0"` is now pinned in the shared conventions; this would have failed CI too.
4. **Root script compiled against a version-catalog accessor that is not visible inside `subprojects {}`.** The ktlint version now comes from `VersionCatalogsExtension` on the root project.
5. **Formatting drift.** `./gradlew ktlintFormat` applied the `android_studio` style (statement wrapping, no trailing commas). Code was formatted, not rewritten.
6. **Lint failed on version-availability notices** because `warningsAsErrors = true`. Deliberately pinned versions are exempted in `android/config/lint.xml`; dependency upgrades stay a separate, tested change. Genuine findings were fixed instead: backup extraction rules added for API 31+ (`data_extraction_rules.xml`) with the pre-12 equivalent (`backup_rules.xml`), and an unused string removed.

## Not verified

| Item | Why | Follow-up |
|---|---|---|
| `connectedDebugAndroidTest` (Room reopen/round-trip, stale-revision conflict, Compose create→detail→back) | Tests exist but no AVD is configured, no `cmdline-tools`/`avdmanager` is installed, no device is attached; only an `android-37.0` `google_apis_playstore` system image is present | Create an API 36 AVD or attach a device, then run the task; add the result here |
| GitHub Actions (`.github/workflows/android.yml`) | Workflow has never run; repository has not been pushed | Observe the first run before treating the job as required |
| Release build, R8 shrinking and signing | Out of Phase 1 scope; no signing configuration exists | Phase 9 |
| Visual RTL, dark theme, font scale, TalkBack | Compose renders were not inspected on a device/emulator | Phase 1 acceptance recheck on device |
| `main` history | Phase 1 work is a single reviewable change, not yet committed when this was written | Commit after review |

## Consequence for the plan

Phase 1 build items 1–4 are evidenced above; item 5 (CI) is written but unproven. The Phase 1 acceptance sentence "CI runs real tests" and the device-side expectations remain open, so Phase 1 is **not** complete and Phase 2 should not be described as started.
