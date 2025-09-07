// 1) Where Gradle finds plugin binaries (Android, Kotlin, etc.)
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

// 2) (Optional) Let Gradle auto-download JDKs (toolchains)
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

// 3) Where your project dependencies are resolved
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// 4) Project name + modules (make sure folder names match!)
rootProject.name = "emrtd-example"
include(":app", ":emrtd-core")
