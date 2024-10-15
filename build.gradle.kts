plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.3" apply false
    id("fabric-loom") version "1.7.4" apply false
}


allprojects {
    group = "your.group.id"
    version = "1.0.0"

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
    }
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
