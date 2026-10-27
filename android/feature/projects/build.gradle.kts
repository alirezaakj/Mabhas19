plugins {
    id("mabhas.android.compose")
    alias(libs.plugins.ksp)
}
android { namespace = "ir.mabhas19.feature.projects" }
dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(project(":core:testing"))
}
