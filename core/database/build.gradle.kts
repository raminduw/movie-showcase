// Local persistence for favourites + notes. Knows SQL, knows nothing about the domain.
plugins {
    alias(libs.plugins.cinenote.android.library)
    alias(libs.plugins.cinenote.hilt)
    alias(libs.plugins.room)
}

android {
    namespace = "dev.ramindu.cinenote.core.database"
    // Robolectric needs merged Android resources to boot an in-memory database.
    testOptions.unitTests.isIncludeAndroidResources = true
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    api(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DAO tests run on the JVM via Robolectric, which needs the JUnit 4 runner (Vintage engine).
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit4)
    testRuntimeOnly(libs.junit.vintage.engine)
}
