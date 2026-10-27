pluginManagement {
    repositories { gradlePluginPortal(); mavenCentral(); google() }
}
dependencyResolutionManagement {
    repositories {
        if (providers.gradleProperty("useGoogleMirror").orNull == "true") {
            maven("https://maven.aliyun.com/repository/google") {
                content {
                    includeGroupByRegex("androidx\\..*")
                    includeGroupByRegex("com\\.android(\\..*)?")
                    includeGroupByRegex("com\\.google\\.android(\\..*)?")
                    includeGroupByRegex("com\\.google\\.testing\\..*")
                }
            }
        }
        google()
        mavenCentral()
    }
    versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }
}
rootProject.name = "build-logic"
