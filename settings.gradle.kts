pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CineNote"

include(":app")

// Core layers
include(":core:domain")       // pure Kotlin: models, repository contracts, use cases
include(":core:data")         // repository implementations, mappers, paging
include(":core:network")      // Retrofit API + DTOs
include(":core:database")     // Room entities + DAO
include(":core:designsystem") // theme + reusable composables
include(":core:ui")           // MVI base, shared presentation helpers
include(":core:testing")      // fakes + test utilities shared by unit tests

// Features (never depend on each other)
include(":feature:movies")
include(":feature:detail")
include(":feature:favorites")
