plugins { `kotlin-dsl` }
dependencies {
    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle)
    implementation(libs.compose.gradle)
}
gradlePlugin {
    plugins {
        register("jvm") { id = "mabhas.jvm"; implementationClass = "JvmConventionPlugin" }
        register("library") { id = "mabhas.android.library"; implementationClass = "AndroidLibraryConventionPlugin" }
        register("compose") { id = "mabhas.android.compose"; implementationClass = "ComposeConventionPlugin" }
        register("app") { id = "mabhas.android.application"; implementationClass = "AppConventionPlugin" }
    }
}
