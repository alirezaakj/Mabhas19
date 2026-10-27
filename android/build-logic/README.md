# Build conventions

Included build with convention plugins for Kotlin/JVM (`mabhas.jvm`), Android library (`mabhas.android.library`), Compose (`mabhas.android.compose`) and application (`mabhas.android.application`). They set compile/target SDK 36, build-tools 36.0.0, minSdk 26, Java 17 bytecode, shared lint policy and the debug/release build types.

The root `qualityCheck` task aggregates every module's `test`/`testDebugUnitTest`, ktlint, detekt, `lintDebug` and the debug APK. The device suite is separate (`connectedDebugAndroidTest`) and CI runs it in its own job. See [testing](../../docs/android/testing.md).
