plugins { id("mabhas.android.compose") }
android { namespace = "ir.mabhas19.feature.home" }
dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    testImplementation(libs.junit)
}
