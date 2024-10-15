enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

include("common", "fabric", "bukkit")

rootProject.name = "KnockbackSync"