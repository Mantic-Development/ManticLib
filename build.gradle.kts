import org.codehaus.plexus.util.StringUtils.clean

/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.0.0"
    `maven-publish`
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }

    maven {
        url = uri("https://repo1.maven.org/maven2/")
    }

    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation("de.tr7zw:item-nbt-api:2.11.0-SNAPSHOT")
    implementation(files("lib/NMSLib_Plugin.jar"))
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("org.spigotmc:spigot-api:1.13.2-R0.1-SNAPSHOT")
    compileOnly(files("lib/InfiniteKothAPI-1.0.jar"))
    compileOnly(files("lib/ManticHoes-1.0-API.jar"))
    compileOnly(files("lib/ManticSwords-1.0-stripped.jar"))
    compileOnly(files("lib/MiningEconomy-API.jar"))
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.jetbrains:annotations:20.1.0")
    annotationProcessor("org.jetbrains:annotations:20.1.0")
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")
}

group = "me.fullpage"
version = "1.0.5"
description = "ManticLib"
java.sourceCompatibility = JavaVersion.VERSION_1_8

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>() {
    archiveFileName.set("${project.name}-${project.version}.jar")
    destinationDirectory.set(file("out"))

    relocate("me.fullpage.nmslib", "me.fullpage.manticlib.nmslib")
    relocate("de.tr7zw.changeme.nbtapi", "me.fullpage.manticlib.nbtapi")


}

tasks.processResources {

    filesMatching("**/plugin.yml") {
        expand( project.properties )
    }

}

tasks.clean {
    delete(file("out"))
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}