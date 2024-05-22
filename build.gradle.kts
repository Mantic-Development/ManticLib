plugins {
    java
    id("io.github.goooler.shadow") version "8.1.7"
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
    implementation("de.tr7zw:item-nbt-api:2.12.4") // Check if on latest
    implementation(files("lib/NMSLib_Plugin.jar"))
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT") // 1.8.8-R0.1-SNAPSHOT 1.19.4-R0.1-SNAPSHOT 1.20.1-R0.1-SNAPSHOT
    compileOnly(files("lib/InfiniteKothAPI-1.0.jar"))
    compileOnly(files("lib/ManticHoes-stripped.jar"))
    compileOnly(files("lib/ManticSwords-stripped.jar"))
    compileOnly(files("lib/ManticRods-stripped.jar"))
    compileOnly(files("lib/MiningEconomy-API.jar"))
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.jetbrains:annotations:20.1.0")
    annotationProcessor("org.jetbrains:annotations:20.1.0")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

group = "me.fullpage"
version = "1.0.45.0"
description = "ManticLib"
java.sourceCompatibility = JavaVersion.VERSION_1_8

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.processResources {

    filesMatching("**/plugin.yml") {
        expand(project.properties)
    }

}

tasks.shadowJar {
    //"${project.name}-${project.version}.jar"
    archiveFileName.set("${project.description}-$version.jar")
    //  destinationDirectory.set(file("out"))

    relocate("me.fullpage.nmslib", "me.fullpage.manticlib.nmslib")
    relocate("de.tr7zw.annotations", "me.fullpage.manticlib.nbtapi.annotations")
    relocate("de.tr7zw.changeme.nbtapi", "me.fullpage.manticlib.nbtapi")
}

//tasks.withType<Jar> {
//  exclude("META-INF", "META-INF/**")
//}

tasks.clean {
    delete(file("out"))
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.build {
    dependsOn("shadowJar")
}

artifacts {
    archives(tasks.shadowJar)
}