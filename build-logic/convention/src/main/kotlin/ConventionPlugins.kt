import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import dev.ramindu.cinenote.buildlogic.AppConfig
import dev.ramindu.cinenote.buildlogic.configureCompose
import dev.ramindu.cinenote.buildlogic.configureKotlinAndroid
import dev.ramindu.cinenote.buildlogic.configureKotlinJvm
import dev.ramindu.cinenote.buildlogic.lib
import dev.ramindu.cinenote.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project

/** `cinenote.android.application` – the :app module (Android + Kotlin + Compose). */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")
        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)
            configureCompose(this)
            defaultConfig.targetSdk = AppConfig.TARGET_SDK
        }
    }
}

/** `cinenote.android.library` – any Android library without UI. */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")
        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
        }
    }
}

/** `cinenote.android.library.compose` – Android library that ships composables. */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("cinenote.android.library")
        configureCompose(extensions.getByType<LibraryExtension>())
    }
}

/**
 * `cinenote.android.feature` – a self-contained screen module.
 * Features see the domain + shared UI layers, never each other or the data layer.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("cinenote.android.library.compose")
        pluginManager.apply("cinenote.hilt")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
        dependencies {
            "implementation"(project(":core:domain"))
            "implementation"(project(":core:ui"))
            "implementation"(project(":core:designsystem"))
            "implementation"(libs.lib("androidx-navigation-compose"))
            "implementation"(libs.lib("hilt-navigation-compose"))
            "implementation"(libs.lib("androidx-lifecycle-runtime-compose"))
            "implementation"(libs.lib("androidx-lifecycle-viewmodel-compose"))
            "testImplementation"(project(":core:testing"))
        }
    }
}

/** `cinenote.jvm.library` – pure Kotlin module (no Android SDK). */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        configureKotlinJvm()
    }
}

/** `cinenote.hilt` – Hilt + KSP for an Android module. */
class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.dagger.hilt.android")
        dependencies {
            "implementation"(libs.lib("hilt-android"))
            "ksp"(libs.lib("hilt-compiler"))
        }
    }
}
