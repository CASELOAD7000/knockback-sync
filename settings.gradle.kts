enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

include("common", "fabric", "bukkit")

rootProject.name = "KnockbackSync"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}