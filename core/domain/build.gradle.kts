// Pure Kotlin: no Android imports allowed here. This is the innermost layer.
plugins {
    alias(libs.plugins.cinenote.jvm.library)
    alias(libs.plugins.ksp)
}

dependencies {
    // Paging's common artifact is plain Kotlin, so the domain can expose paged streams.
    api(libs.androidx.paging.common)
    api(libs.kotlinx.coroutines.core)
    // JSR-330 @Inject on use cases; the Dagger processor generates their factories.
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    testImplementation(project(":core:testing"))
    testImplementation(libs.androidx.paging.testing)
}
