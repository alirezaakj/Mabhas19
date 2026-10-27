# Build and test

The Android Studio/Gradle root is `android/`. Phase 1 provides a Persian RTL Compose shell, local guest project creation/reopening, Room context/draft storage, DataStore theme preferences and an injectable A01 public-network adapter. Engineering input forms, reference-data integration and the air-test workflow are Phase 2.

## Toolchain

Use JDK 21 to run Gradle. Android Studio's bundled runtime may be newer and is not necessarily compatible with this pinned build: on 2026-10-27 the bundled JBR 25.0.3 made AGP abort with the bare version string `25.0.3`. The source/bytecode target is Java 17; it is distinct from the Gradle runtime.

| Component | Pin |
|---|---|
| Gradle | 8.13 |
| Android Gradle Plugin | 8.13.2 |
| Kotlin / Compose compiler | 2.2.21 |
| KSP | 2.2.21-2.0.4 |
| Compose BOM | 2025.12.01 |
| Hilt | 2.57.2 |
| Room | 2.8.3 |
| Navigation 3 | 1.0.1 |
| Android SDK | compile/target 36, build tools 36.0.0; minimum API 26 |

Full pins are in `gradle/libs.versions.toml`. This is a compatible selected baseline, not a claim to use the newest releases. The application ID remains provisional: `ir.mabhas19.android`, debug suffix `.debug`.

Set `JAVA_HOME` to JDK 21 and `ANDROID_HOME` to an SDK containing platform 36/build-tools 36.0.0; alternatively configure an ignored `local.properties` with `sdk.dir`. Install the platform through Android Studio SDK Manager or `sdkmanager`:

```sh
sdkmanager 'platforms;android-36' 'build-tools;36.0.0'
cd android
./gradlew qualityCheck
```

`qualityCheck` aggregates JVM tests in pure Kotlin modules, Android-module debug unit tests, Detekt, ktlint, Android Lint and the debug APK. It deliberately does not require an attached device. `./gradlew ktlintFormat` applies formatting.

Device checks require a running emulator or authorized device:

```sh
./gradlew connectedDebugAndroidTest
```

They cover Compose create/detail/back navigation with a fake repository, actual Room persistence across reopen, and repository draft conflicts using SQLite. A CI emulator job runs them separately. No automated test contacts the production website.

## Dependency integrity and restricted networks

The wrapper distribution SHA-256 is pinned. The generated wrapper JAR's official SHA-256 is `81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f`; CI validates the wrapper. Keep dependencies pinned and review changes to repository policy. Do not commit machine paths, downloaded toolchains, cookies or keys.

Normal builds use Google Maven, Maven Central and the Gradle Plugin Portal. If Google Maven returns network/location-related 404s, an explicit opt-in is available:

```sh
./gradlew -PuseGoogleMirror=true qualityCheck
```

This adds the HTTPS Aliyun Google Maven mirror before Google; it changes the dependency source and is not enabled in CI. The mirror is restricted by `content { includeGroupByRegex(...) }` to the Google Maven groups (`androidx.*`, `com.android[.*]`, `com.google.android[.*]`, `com.google.testing.*`) because it answers unknown coordinates with HTTP 502 instead of 404, which Gradle treats as fatal rather than falling through to Maven Central. Do not disable TLS verification or add insecure repositories. Initial checksum metadata, if regenerated, must be reviewed rather than blindly accepted as independent proof of authenticity.

## Operation and limitations

Open `android/` in Android Studio, choose Gradle JDK 21, sync, and run `app`. The launcher starts at local projects; create a named fourth-edition guest project, open its detail, return to the list, and change theme through the app bar. The project title survives app restart via Room. Unsaved title text survives supported saved-state recreation; it is not a durable project until Save succeeds.

No member session or remote mutation is implemented. App backup is disabled for the initial guest database (`allowBackup="false"` plus empty backup/data-extraction rules); import/export and account retention policy are later work. Session logging is absent. Numeric parsing/unit helpers do not issue compliance or certification decisions.

Build/test evidence and any host limitations are recorded in `docs/android/phase-1-status.md` at repository root.

References: [AGP/Gradle compatibility](https://developer.android.com/build/releases/about-agp), [Gradle JVM compatibility](https://docs.gradle.org/8.13/userguide/compatibility.html), [Navigation 3 releases](https://developer.android.com/jetpack/androidx/releases/navigation3), [Hilt 2.57.2](https://github.com/google/dagger/releases/tag/dagger-2.57.2).
