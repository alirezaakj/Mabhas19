plugins { id("mabhas.jvm") }
dependencies {
    api(project(":core:model"))
    testImplementation(libs.junit)
}
