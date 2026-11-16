plugins {
    alias(libs.plugins.cinenote.android.feature)
}

android {
    namespace = "dev.ramindu.cinenote.feature.detail"
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
}
