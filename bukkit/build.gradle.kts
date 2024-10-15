plugins {
    id("com.gradleup.shadow")
    id("net.neoforged.moddev") version "1.0.11"
}

base {
    archivesName.set("${rootProject.property("archives_base_name")}-bukkit")
}

dependencies {
    implementation(project(":common"))

    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    compileOnly("dev.folia:folia-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    implementation("com.github.retrooper:packetevents-spigot:2.5.0")
    implementation("dev.jorel:commandapi-bukkit-shade:9.5.3")
}

tasks.shadowJar {
    archiveClassifier.set("")
    configurations = listOf(project.configurations.runtimeClasspath.get())

    relocate("io.github.retrooper.packetevents", "${project.group}.shaded.packetevents")
    relocate("dev.jorel.commandapi", "${project.group}.shaded.commandapi")

    minimize()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

neoForge {
    // Look for versions on https://projects.neoforged.net/neoforged/neoform
    neoFormVersion.set("1.21-20240613.152323")
}