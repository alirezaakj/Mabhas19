# Build versions and wrapper

The checked wrapper is Gradle 8.13 with a pinned `distributionSha256Sum`; `gradle/libs.versions.toml` pins the compatible JDK/AGP/Kotlin/Compose/SDK set and is the only place to change versions. Keep the wrapper JAR checksum in [BUILDING.md](../BUILDING.md) in sync if the wrapper is regenerated. Never commit local SDK paths or signing secrets.
