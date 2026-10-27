plugins { id("mabhas.android.compose") }
android { namespace = "ir.mabhas19.core.designsystem" }
dependencies {
    api(platform(libs.compose.bom))
    api(libs.compose.material3)
    api(libs.compose.ui)
    implementation(libs.compose.ui.preview)
    debugImplementation(libs.compose.ui.tooling)
}
