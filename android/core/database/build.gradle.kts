plugins {
    id("mabhas.android.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}
android {
    namespace = "ir.mabhas19.core.database"
    sourceSets.getByName("androidTest").assets.srcDir("schemas")
}
room { schemaDirectory("$projectDir/schemas") }
dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.coroutines.core)
    ksp(libs.room.compiler)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.coroutines.test)
}
