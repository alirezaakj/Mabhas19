plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

val qualityCheck by tasks.registering {
    group = "verification"
    description = "Checks every JVM and Android module, lint, style and debug assembly."
}
val ktlintCheck by tasks.registering { group = "verification" }
val ktlintFormat by tasks.registering { group = "formatting" }
val detekt by tasks.registering { group = "verification" }

val ktlintVersion = extensions.getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java)
        .named("libs")
        .findVersion("ktlint")
        .get()
        .requiredVersion

subprojects {
    fun configureQuality() {
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
        apply(plugin = "io.gitlab.arturbosch.detekt")
        extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
            version.set(ktlintVersion)
            filter { exclude("**/build/**") }
        }
        extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
            buildUponDefaultConfig = true
            config.setFrom(rootProject.file("config/detekt.yml"))
        }
        rootProject.tasks.named("ktlintCheck") { dependsOn(tasks.named("ktlintCheck")) }
        rootProject.tasks.named("ktlintFormat") { dependsOn(tasks.named("ktlintFormat")) }
        rootProject.tasks.named("detekt") { dependsOn(tasks.named("detekt")) }
        qualityCheck.configure { dependsOn(tasks.named("ktlintCheck"), tasks.named("detekt")) }
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        configureQuality()
        qualityCheck.configure { dependsOn(tasks.named("test")) }
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.android") {
        configureQuality()
        qualityCheck.configure { dependsOn(tasks.named("testDebugUnitTest"), tasks.named("lintDebug")) }
    }
    pluginManager.withPlugin("com.android.application") {
        qualityCheck.configure { dependsOn(tasks.named("assembleDebug")) }
    }
}
