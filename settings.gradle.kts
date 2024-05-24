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
    plugins {
        id("com.github.ben-manes.versions") version "0.39.0"
        //alias(libs.plugins.jetbrainsKotlinAndroid)
        //alias(libs.plugins.jetbrainsKotlinAndroid)
        //alias(libs.plugins.jetbrainsKotlinAndroid)

    }
}


dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "freebite2"
include(":app")
 