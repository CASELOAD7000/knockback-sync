plugins {
    id("fabric-loom") version "1.7.4"
    id("maven-publish")
    id("com.gradleup.shadow") version "8.3.3"
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val shadeThisThing: Configuration by configurations.creating {
    isCanBeConsumed = true
    isTransitive = true
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
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
}

loom {
    // Since this is a plugin, client classes in the classpath aren't needed
//    serverOnlyMinecraftJar()
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
//    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    // We can use include() since we only ever use this dependency on fabric
    include(modImplementation("me.lucko:fabric-permissions-api:0.3.1")!!)
    include(modImplementation("com.github.retrooper:packetevents-fabric:2.5.0")!!)

//    compileOnly("org.spigotmc:spigot:1.18.2-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    compileOnly("dev.folia:folia-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

//    shadeThisThing(modImplementation("com.github.retrooper:packetevents-fabric:2.5.0")!!)
    shadeThisThing(implementation("com.github.retrooper:packetevents-spigot:2.5.0")!!)
    shadeThisThing(implementation("dev.jorel:commandapi-bukkit-shade:9.5.3")!!)
    shadeThisThing(implementation("org.kohsuke:github-api:1.326")!!)
    shadeThisThing(implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")!!) // Jackson 2.17.2 corresponds to org.kohsuke 1.326
    shadeThisThing(implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")!!)
    shadeThisThing(implementation("org.bstats:bstats-bukkit:3.0.2")!!)

//    include(implementation("me.lucko:fabric-permissions-api:0.3.1")!!)
//    implementation("me.lucko:commodore:2.2")
//    shadeThisThing("me.lucko:commodore:2.2") {
//        exclude(group = "com.mojang", module = "brigadier")
//    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "loader_version" to project.property("loader_version")
        )
    }
}

val targetJavaVersion = 21
tasks.withType<JavaCompile>().configureEach {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.property("archives_base_name")}" }
    }
    archiveClassifier.set("dev-slim")
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

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
