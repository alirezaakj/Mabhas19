plugins { id("mabhas.jvm") }
dependencies {
    api(project(":core:domain"))
    api(libs.coroutines.test)
    api(libs.junit)
}
