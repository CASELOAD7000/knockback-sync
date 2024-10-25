plugins {
    id("fabric-loom")
    id("com.github.gmazzo.buildconfig") version "3.1.0"
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
    implementation("org.yaml:snakeyaml:2.0")
//    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
//    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")
    implementation("org.kohsuke:github-api:1.326") {
        exclude(group = "commons-io", module = "commons-io")
        exclude(group = "org.apache.commons", module = "commons-lang3")
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

buildConfig {
    buildConfigField("String", "GITHUB_REPO", "\"${project.rootProject.ext["githubRepo"]}\"")
}