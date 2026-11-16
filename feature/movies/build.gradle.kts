plugins {
    alias(libs.plugins.cinenote.android.feature)
}

android {
    namespace = "dev.ramindu.cinenote.feature.movies"
}

dependencies {
    implementation(libs.androidx.paging.compose)
    testImplementation(libs.androidx.paging.testing)
}
