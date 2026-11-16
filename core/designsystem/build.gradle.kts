// Theme + reusable, stateless composables. No ViewModels, no domain knowledge.
plugins {
    alias(libs.plugins.cinenote.android.library.compose)
}

android {
    namespace = "dev.ramindu.cinenote.core.designsystem"
}

dependencies {
    api(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
