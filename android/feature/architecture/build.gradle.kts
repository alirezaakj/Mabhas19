plugins {
    id("mabhas.android.compose")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}
android { namespace = "ir.mabhas19.feature.architecture" }
dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:calculation"))
    implementation(project(":core:network"))
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.serialization.json)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(project(":core:testing"))
}
