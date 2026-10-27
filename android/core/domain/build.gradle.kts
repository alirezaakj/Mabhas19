plugins { id("mabhas.jvm") }
dependencies {
    api(project(":core:model"))
    api(libs.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
