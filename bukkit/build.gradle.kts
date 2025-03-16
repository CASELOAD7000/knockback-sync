import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow")
}

val shadePE: Boolean by rootProject.extra

base {
    archivesName.set("${rootProject.property("archives_base_name")}-bukkit${if (shadePE) "" else "-lite"}")
}

val shadeThisThing: Configuration by configurations.creating {
    isCanBeConsumed = true
    isTransitive = true
}

// TODO migrate to only including sources sets for compile, test and javadoc tasks
tasks.withType<JavaCompile>().configureEach {
    source(project(":common").sourceSets.main.get().allSource)
}

tasks.withType<Javadoc>().configureEach {
    source(project(":common").sourceSets.main.get().allJava)
}

tasks.named<JavaCompile>("compileTestJava") {
    exclude("**/*")
}

dependencies {
    implementation(project(":common"))

    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.1.72.Final")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    shadeThisThing(implementation("org.kohsuke:github-api:1.326")!!)
    if (shadePE) {
        shadeThisThing(implementation("com.github.retrooper:packetevents-spigot:2.7.1-SNAPSHOT")!!)
    } else {
        compileOnly("com.github.retrooper:packetevents-spigot:2.7.1-SNAPSHOT")
    }
    shadeThisThing(implementation("org.incendo:cloud-paper:2.0.0-beta.10")!!)
    shadeThisThing(implementation("org.incendo:cloud-core:2.0.0")!!)

    // Required for 1.14.4 support because gson is too old to have JosnParser.parseString()
    shadeThisThing(implementation("com.google.code.gson:gson:2.11.0")!!)
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }

    // Remove the -all suffix from the output JAR file name
    archiveClassifier.set("")

    configurations = listOf(shadeThisThing)
    isEnableRelocation = false
    relocationPrefix = "${project.property("maven_group")}.${project.property("archives_base_name")}.shaded"
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    from(project(":common").sourceSets.main.get().resources)
    inputs.property("version", project.version)
    inputs.property("shadePE", shadePE) // Add shadePE as an input to trigger reprocessing if it changes
    filteringCharset = "UTF-8"


    filesMatching("plugin.yml") {
        expand(
            "version" to project.version,
            "depends" to if (shadePE) "[]" else listOf("packetevents")
        )
    }
}