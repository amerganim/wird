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

rootProject.name = "Wird"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

include(":core:common")
include(":core:ui")
include(":core:database")
include(":core:prayertimes")

include(":feature:quran")
include(":feature:prayer")
include(":feature:alarm")
include(":feature:hifz")
include(":feature:qibla")
include(":feature:recitation")
