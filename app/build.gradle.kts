import java.util.Properties

plugins {
    alias(libs.plugins.cinenote.android.application)
    alias(libs.plugins.cinenote.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// The TMDB token never goes into git: read it from local.properties (or the TMDB_TOKEN env var on CI).
val tmdbToken: String = Properties()
    .apply { rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load) }
    .getProperty("TMDB_TOKEN")
    ?.takeIf { it.isNotBlank() }
    ?: System.getenv("TMDB_TOKEN").orEmpty()

android {
    namespace = "dev.ramindu.cinenote"

    defaultConfig {
        applicationId = "dev.ramindu.cinenote"
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "TMDB_TOKEN", "\"$tmdbToken\"")
    }

    buildFeatures.buildConfig = true

    buildTypes {
        release {
            isMinifyEnabled = false
            // Debug-signed so a release build can be installed for demos.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    // :app is the composition root – the only module that sees every layer.
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":feature:movies"))
    implementation(project(":feature:detail"))
    implementation(project(":feature:favorites"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
