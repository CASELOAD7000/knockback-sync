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
    implementation("dev.jorel:commandapi-bukkit-shade:9.5.3")
}

repositories {
    maven("https://maven.neoforged.net/releases")
}

tasks.named("remapJar").configure {
    enabled = false
}

// Using neoforge in vanilla mode so common code compiles
//neoForge {
//     Look for versions on https://projects.neoforged.net/neoforged/neoform
//    neoFormVersion.set("1.21-20240613.152323")

//    runs {
//        create("client") {
//            client()
//        }
//        create("server") {
//            server()
//        }
//        create("data") {
//            data()
//        }
//    }
//}