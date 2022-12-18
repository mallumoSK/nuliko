pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://mallumo.jfrog.io/artifactory/gradle-dev-local")
    }
    plugins {
        id("com.android.application").version(extra["version.agp"] as String)
        id("com.android.library").version(extra["version.agp"] as String)
        kotlin("android").version(extra["version.kotlin"] as String)
        kotlin("jvm").version(extra["version.kotlin"] as String)
        kotlin("multiplatform").version(extra["version.kotlin"] as String)
        id("org.jetbrains.kotlin.plugin.serialization").version(extra["version.kotlin"] as String)
        id("com.github.johnrengelman.shadow").version(extra["version.shadow"] as String)
        id("com.google.devtools.ksp") version extra["version.ksp"] as String
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jcenter.bintray.com")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://mallumo.jfrog.io/artifactory/gradle-dev-local")
        maven("https://kotlin.bintray.com/kotlinx")
    }
}
rootProject.name = "nuliko"
include(":nuliko-rpi")

include(":nuliko-android")
include(":nuliko-shared")
