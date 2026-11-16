// Hand-written fakes and test helpers shared by every module's unit tests.
plugins {
    alias(libs.plugins.cinenote.jvm.library)
}

dependencies {
    api(project(":core:domain"))
    api(libs.kotlinx.coroutines.test)
    api(platform(libs.junit.bom))
    api(libs.junit.jupiter)
}
