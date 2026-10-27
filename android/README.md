# Android application root

Gradle root for the native app. Phase 1 provides a Persian RTL Compose shell whose start screen is a native workspace mirroring the website main page (discipline cards, quick access, local project overview), plus local guest project creation/reopening, Room project/context/draft storage, DataStore theme preference and an injectable A01 public-network adapter.

Local verification on 2026-10-27: `qualityCheck` (JVM tests, ktlint, detekt, Android Lint, debug APK) passed; the device/Room/Compose tests are written but not yet executed. Exact commands, results and host caveats are in [phase-1-status.md](../docs/android/phase-1-status.md). Build instructions are in [BUILDING.md](BUILDING.md).

See [architecture and module structure](../docs/android/architecture.md), [implementation sequence](../docs/android/implementation-plan.md), and [test strategy](../docs/android/testing.md).
