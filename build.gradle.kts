import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.3" apply false
    id("fabric-loom") version "1.10.2" apply false
}

val fullVersion = "1.3.5"
val snapshot = true
val githubRepo = System.getenv("GITHUB_REPOSITORY") ?: project.findProperty("githubRepo").toString()

extra["shadePE"] = project.findProperty("shadePE")?.toString()?.toBoolean()
    ?: System.getenv("SHADE_PE")?.toBoolean()
    ?: true

allprojects {
    fun getVersionMeta(includeHash: Boolean): String {
        if (!snapshot) {
            return ""
        }
        var commitHash = ""
        if (includeHash && file(".git").isDirectory) {
            val stdout = ByteArrayOutputStream()
            exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
                standardOutput = stdout
            }
            commitHash = "+${stdout.toString().trim()}"
        }
        return "$commitHash-SNAPSHOT"
    }

    group = "me.caseload.knockbacksync"
    version = "$fullVersion${getVersionMeta(true)}"
    ext["versionNoHash"] = "$fullVersion${getVersionMeta(false)}"
    ext["githubRepo"] = githubRepo

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.codemc.io/repository/maven-releases/")
        maven("https://repo.opencollab.dev/maven-snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven(url = "https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://libraries.minecraft.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://repo.codemc.io/repository/maven-snapshots/")
    }
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
