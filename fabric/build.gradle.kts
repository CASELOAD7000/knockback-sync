plugins {
    id("fabric-loom")
    id("com.gradleup.shadow")
}

base {
    archivesName.set("${rootProject.property("archives_base_name")}-fabric")
}

val shadeThisThing: Configuration by configurations.creating {
    isCanBeConsumed = true
    isTransitive = true
}

tasks.jar {
    from(project(":common").sourceSets.named("main").get().output)
}

dependencies {
    shadeThisThing(implementation(project(":common"))!!)

    minecraft("com.mojang:minecraft:${rootProject.property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21:${rootProject.property("parchment_mappings")}")
    })
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.property("fabric_version")}")

    include(modImplementation("me.lucko:fabric-permissions-api:0.3.1")!!)
    include(modImplementation("com.github.retrooper:packetevents-fabric:2.5.8-SNAPSHOT")!!)

    shadeThisThing(implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")!!)
    shadeThisThing(implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")!!)
    shadeThisThing(implementation("org.kohsuke:github-api:1.326")!!)

//    include(implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")!!)
//    include(implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")!!)
//    include(implementation("org.kohsuke:github-api:1.326")!!)

//    include(implementation("org.kohsuke:github-api:1.326") {
//        exclude(group = "commons-io", module = "commons-io")
//        exclude(group = "org.apache.commons", module = "commons-lang3")
//    })

    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.shadowJar {
    archiveClassifier.set("dev")
    configurations = listOf(shadeThisThing)
    isEnableRelocation = false
    relocationPrefix = "${project.property("maven_group")}.${project.property("archives_base_name")}.shaded"
    finalizedBy(tasks.remapJar)
}
tasks.remapJar {
    archiveClassifier.set(null as String?)
    dependsOn(tasks.shadowJar)
    inputFile = tasks.shadowJar.get().archiveFile
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