pluginManagement {
    includeBuild("build-logic")
    repositories {
        if (providers.gradleProperty("useGoogleMirror").orNull == "true") {
            maven("https://maven.aliyun.com/repository/google") {
                // The mirror proxies Google Maven only; keep Central/Portal artifacts away from it
                // because it answers unknown coordinates with HTTP 502 instead of 404.
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
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (providers.gradleProperty("useGoogleMirror").orNull == "true") {
            maven("https://maven.aliyun.com/repository/google") {
                // See pluginManagement: only Google Maven groups are proxied by this mirror.
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
}

rootProject.name = "Mabhas19"
include(":app")
include(":core:model", ":core:domain", ":core:calculation", ":core:network")
include(":core:database", ":core:data", ":core:designsystem", ":core:testing")
include(":feature:projects")
include(":feature:home")
include(":feature:architecture")
