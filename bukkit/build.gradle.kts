plugins {
    id("com.gradleup.shadow")
    id("net.neoforged.moddev") version "1.0.11"
}

base {
    archivesName.set("${rootProject.property("archives_base_name")}-bukkit")
}

val shadeThisThing: Configuration by configurations.creating {
    isCanBeConsumed = true
    isTransitive = true
}

sourceSets {
    main {
        java {
            srcDir(project(":common").file("src/main/java"))
        }
        resources {
            srcDir(project(":common").file("src/main/resources"))
        }
    }
}

dependencies {
//    implementation(project(":common"))

    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    compileOnly("dev.folia:folia-api:1.20.4-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    shadeThisThing(implementation("org.kohsuke:github-api:1.326") {
        exclude(group = "commons-io", module = "commons-io")
        exclude(group = "org.apache.commons", module = "commons-lang3")
    })

    shadeThisThing(implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")!!)
    shadeThisThing(implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")!!)
    shadeThisThing(implementation("com.github.retrooper:packetevents-spigot:2.5.0")!!)
    shadeThisThing(implementation("dev.jorel:commandapi-bukkit-shade:9.5.3")!!)
}

tasks.shadowJar {
//    archiveClassifier.set("dev")
    configurations = listOf(shadeThisThing)
    isEnableRelocation = true
    relocationPrefix = "${project.property("maven_group")}.${project.property("archives_base_name")}.shaded"
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"

    filesMatching("plugin.yml") {
        expand(
            "version" to project.version,
        )
    }
}

neoForge {
    // Look for versions on https://projects.neoforged.net/neoforged/neoform
    neoFormVersion.set("1.21-20240613.152323")
}