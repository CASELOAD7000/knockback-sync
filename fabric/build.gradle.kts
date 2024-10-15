plugins {
    id("fabric-loom")
}

base {
    archivesName.set("${rootProject.property("archives_base_name")}-fabric")
}


dependencies {
    implementation(project(":common"))

    minecraft("com.mojang:minecraft:${rootProject.property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21:${rootProject.property("parchment_mappings")}")
    })
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.property("fabric_version")}")

    include(modImplementation("me.lucko:fabric-permissions-api:0.3.1")!!)
    include(modImplementation("com.github.retrooper:packetevents-fabric:2.5.6-SNAPSHOT")!!)

    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", rootProject.property("minecraft_version"))
    inputs.property("loader_version", rootProject.property("loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to rootProject.property("minecraft_version"),
            "loader_version" to rootProject.property("loader_version")
        )
    }
}