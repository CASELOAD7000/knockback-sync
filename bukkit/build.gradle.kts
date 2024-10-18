import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow")
//    id("io.papermc.paperweight.userdev") version "1.7.3"
//    id("net.neoforged.moddev") version "1.0.11"
}

//paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION

base {
    archivesName.set("${rootProject.property("archives_base_name")}-bukkit")
}

val shadeThisThing: Configuration by configurations.creating {
    isCanBeConsumed = true
    isTransitive = true
}

//sourceSets {
//    main {
//        java {
//            srcDir(project(":common").sourceSets.main.get().java.srcDirs)
//        }
//        resources {
//            srcDir(project(":common").sourceSets.main.get().resources.srcDirs)
//        }
//    }
//}

// TODO migrate to only including sourceset for compile, test and javadoc tasks
// Currently must build with gradle build -x test to skip test
tasks.withType<JavaCompile>().configureEach {
    source(project(":common").sourceSets.main.get().allSource)
    options.annotationProcessorPath = configurations["annotationProcessor"] + configurations["compileClasspath"]
}

tasks.withType<Javadoc>().configureEach {
    source(project(":common").sourceSets.main.get().allJava)
}

// Dirty hack exists so the build process will finish running
//tasks.withType<Test>().configureEach {
//    classpath += project(":common").sourceSets["main"].output + configurations["compileClasspath"] + configurations["runtimeClasspath"]
//}


dependencies {
//    shadeThisThing(implementation(project(":common"))!!)
    implementation(project(":common"))

//    paperweight.paperDevBundle("1.16.5-R0.1-SNAPSHOT")
//    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
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

tasks.withType<ShadowJar> {
    // Exclude Java 21 specific classes
    exclude("META-INF/**")

//    archiveClassifier.set("-all")
    configurations = listOf(shadeThisThing)
    isEnableRelocation = false
    relocationPrefix = "${project.property("maven_group")}.${project.property("archives_base_name")}.shaded"

    // Exclude Java 21 specific classes
    exclude("META-INF/**")
}

//tasks.named("reobfJar") {
//    dependsOn(tasks.named("shadowJar"))
//}

tasks.build {
//    dependsOn(tasks.named("reobfJar"))
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

//neoForge {
// Look for versions on https://projects.neoforged.net/neoforged/neoform
//    neoFormVersion.set("1.21-20240613.152323")
//}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
    toolchain.languageVersion.set(JavaLanguageVersion.of(16))
}