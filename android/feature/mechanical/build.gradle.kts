plugins {
    id("mabhas.android.compose")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}
android { namespace = "ir.mabhas19.feature.mechanical" }
dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.serialization.json)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
