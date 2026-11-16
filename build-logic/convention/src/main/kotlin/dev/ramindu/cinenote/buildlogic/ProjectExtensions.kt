package dev.ramindu.cinenote.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

object AppConfig {
    const val COMPILE_SDK = 36
    const val TARGET_SDK = 36
    const val MIN_SDK = 26
    val JAVA_VERSION = JavaVersion.VERSION_17
}

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { IllegalStateException("Missing library '$alias' in libs.versions.toml") }

/** Shared Android + Kotlin setup used by both application and library modules. */
internal fun Project.configureKotlinAndroid(extension: CommonExtension<*, *, *, *, *, *>) {
    extension.apply {
        compileSdk = AppConfig.COMPILE_SDK
        defaultConfig {
            minSdk = AppConfig.MIN_SDK
        }
        compileOptions {
            sourceCompatibility = AppConfig.JAVA_VERSION
            targetCompatibility = AppConfig.JAVA_VERSION
        }
        testOptions {
            unitTests.isReturnDefaultValues = true
        }
    }
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions { applyCommonOptions() }
    }
    configureUnitTests()
}

internal fun Project.configureKotlinJvm() {
    extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
        sourceCompatibility = AppConfig.JAVA_VERSION
        targetCompatibility = AppConfig.JAVA_VERSION
    }
    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions { applyCommonOptions() }
    }
    configureUnitTests()
}

private fun KotlinCommonCompilerOptions.applyCommonOptions() {
    if (this is org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions) {
        jvmTarget.set(JvmTarget.JVM_17)
    }
    freeCompilerArgs.addAll(
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-opt-in=kotlinx.coroutines.FlowPreview",
    )
}

/** Every module runs its unit tests on JUnit 5 with the same assertion/mocking stack. */
private fun Project.configureUnitTests() {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging { events("failed") }
    }
    dependencies {
        "testImplementation"(platform(libs.lib("junit-bom")))
        "testImplementation"(libs.lib("junit-jupiter"))
        "testRuntimeOnly"(libs.lib("junit-platform-launcher"))
        "testImplementation"(libs.lib("mockk"))
        "testImplementation"(libs.lib("truth"))
        "testImplementation"(libs.lib("turbine"))
        "testImplementation"(libs.lib("kotlinx-coroutines-test"))
    }
}

/** Enables Compose for an Android module and adds the BOM-managed basics. */
internal fun Project.configureCompose(extension: CommonExtension<*, *, *, *, *, *>) {
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
    extension.buildFeatures.compose = true
    dependencies {
        val bom = libs.lib("androidx-compose-bom")
        "implementation"(platform(bom))
        "implementation"(libs.lib("androidx-compose-ui"))
        "implementation"(libs.lib("androidx-compose-ui-tooling-preview"))
        "implementation"(libs.lib("androidx-compose-material3"))
        "implementation"(libs.lib("androidx-compose-material-icons-core"))
        "debugImplementation"(libs.lib("androidx-compose-ui-tooling"))
    }
}
