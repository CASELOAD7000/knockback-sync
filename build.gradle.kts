import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.3" apply false
    id("fabric-loom") version "1.11.8" apply false
}

fun getGitCommitHash(project: Project): String? {
    // Only try to get the hash if the .git directory exists
    if (!project.file(".git").isDirectory) {
        return null
    }
    return try {
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        process.waitFor(5, TimeUnit.SECONDS)

        if (process.exitValue() == 0) {
            process.inputStream.bufferedReader().readText().trim().takeIf { it.isNotEmpty() }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

val fullVersion = "1.3.5"
val snapshot = true
val githubRepo = System.getenv("GITHUB_REPOSITORY") ?: project.findProperty("githubRepo").toString()

extra["shadePE"] = project.findProperty("shadePE")?.toString()?.toBoolean()
    ?: System.getenv("SHADE_PE")?.toBoolean()
    ?: true

extra["relocate"] = project.findProperty("relocate")?.toString()?.toBoolean()
    ?: System.getenv("RELOCATE_JAR")?.toBoolean()
    ?: true

allprojects {
    fun getVersionMeta(includeHash: Boolean): String {
        if (!snapshot) {
            return ""
        }
        val commitHash = if (includeHash) {
            getGitCommitHash(project)?.let { "+$it" } ?: ""
        } else {
            ""
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
