plugins {
    java
    id("io.github.goooler.shadow") version "8.1.7"
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
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
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/groups/public/") }
}

dependencies {
    implementation("de.tr7zw:item-nbt-api:2.14.1") // Check if on latest https://github.com/tr7zw/Item-NBT-API
    implementation(files("lib/NMSLib_Plugin.jar"))
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    // cannot compile on 1.21 + or inventory issues will occur... but will still work on 1.21+
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT") // 1.8.8-R0.1-SNAPSHOT 1.19.4-R0.1-SNAPSHOT 1.20.1-R0.1-SNAPSHOT 1.21-R0.1-SNAPSHOT
    compileOnly(files("lib/InfiniteKothAPI-1.0.jar"))
    compileOnly(files("lib/ManticHoes-2.0.5-strippedforapi.jar"))
    compileOnly(files("lib/ManticSwords-stripped.jar"))
    compileOnly(files("lib/ManticRods-stripped.jar"))
    compileOnly(files("lib/MiningEconomy-API.jar"))
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.jetbrains:annotations:20.1.0")
    annotationProcessor("org.jetbrains:annotations:20.1.0")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("com.github.cryptomorin:XSeries:13.2.0")
}

group = "me.fullpage"
version = "1.0.49.2"
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
    relocate("com.cryptomorin", "me.fullpage.manticlib.cryptomorin")
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