// Presentation helpers shared by features: MVI base ViewModel, error → message, formatters.
plugins {
    alias(libs.plugins.cinenote.android.library.compose)
}

android {
    namespace = "dev.ramindu.cinenote.core.ui"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:designsystem"))
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.runtime.compose)

    testImplementation(project(":core:testing"))
}
