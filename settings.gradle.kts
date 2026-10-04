pluginManagement {
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

rootProject.name = "GameLauncher"
include(":app")

// Core modules
include(":core:common")
include(":core:database")
include(":core:metadata")
include(":core:filesystem")
include(":core:launcher")
include(":core:package-manager")
include(":core:networking")
include(":core:images")

// Feature modules
include(":feature:home")
include(":feature:library")
include(":feature:game-detail")
include(":feature:search")
include(":feature:scanner")
include(":feature:metadata-editor")
include(":feature:play-history")
include(":feature:statistics")
include(":feature:settings")

// Engine modules
include(":engine:detector")
include(":engine:classifier")
include(":engine:launch-adapters")
:core:domain
