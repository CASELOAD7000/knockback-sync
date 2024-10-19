plugins {
    id("fabric-loom")
//    id("net.neoforged.moddev") version "1.0.11"
}

dependencies {
    minecraft("com.mojang:minecraft:1.16.5")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.16.5:2022.03.06")
    })

    // True compileOnly deps
    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    // Shaded in or bundled by platform-specific code
    implementation("com.github.retrooper:packetevents-api:2.5.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")
    implementation("org.kohsuke:github-api:1.326") {
        exclude(group = "commons-io", module = "commons-io")
        exclude(group = "org.apache.commons", module = "commons-lang3")
    }

    api("net.kyori:adventure-api:4.11.0") {
        exclude(module = "adventure-bom")
        exclude(module = "checker-qual")
        exclude(module = "annotations")
    }

    api("net.kyori:adventure-text-serializer-gson:4.11.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
        exclude(module = "gson")
    }

    api("net.kyori:adventure-text-serializer-legacy:4.11.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    api("net.kyori:adventure-text-serializer-plain:4.11.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    api("net.kyori:adventure-text-minimessage:4.11.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    api("net.kyori:event-api:3.0.0") {
        exclude(module = "checker-qual")
        exclude(module = "guava")
    }

    implementation("org.incendo:cloud-core:2.0.0")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.10")
}

repositories {
    maven("https://maven.neoforged.net/releases")
}

tasks.named("remapJar").configure {
    enabled = false
}