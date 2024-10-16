plugins {
    id("net.neoforged.moddev") version "1.0.11"
}

dependencies {
    // True compileOnly deps
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
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
}

repositories {
    maven("https://maven.neoforged.net/releases")
}

// Using neoforge in vanilla mode so common code compiles
neoForge {
    // Look for versions on https://projects.neoforged.net/neoforged/neoform
    neoFormVersion.set("1.21-20240613.152323")

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
}