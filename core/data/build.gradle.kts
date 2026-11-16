// Implements the domain's repository contracts using :core:network and :core:database.
plugins {
    alias(libs.plugins.cinenote.android.library)
    alias(libs.plugins.cinenote.hilt)
}

android {
    namespace = "dev.ramindu.cinenote.core.data"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":core:testing"))
    testImplementation(libs.androidx.paging.testing)
}
