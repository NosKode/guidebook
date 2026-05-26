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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://artifactory.yandex.net/artifactory/yandex_mobile_sdk_maven/") }
    }
}

rootProject.name = "Guidebook"

include(":app", ":backend")
project(":app").projectDir = file("frontend/app")
project(":backend").projectDir = file("backend")
