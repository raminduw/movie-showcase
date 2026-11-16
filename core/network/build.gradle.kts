// Talks to TMDB. Knows HTTP + JSON, knows nothing about the domain.
plugins {
    alias(libs.plugins.cinenote.android.library)
    alias(libs.plugins.cinenote.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.ramindu.cinenote.core.network"
    buildFeatures.buildConfig = true
}

dependencies {
    api(libs.retrofit)
    api(libs.okhttp)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.okhttp.mockwebserver)
}
