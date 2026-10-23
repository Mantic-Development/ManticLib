import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.jar.Manifest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
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

val legacyImplementation = configurations.create("legacyImplementation")

dependencies {
    // full build
    implementation("de.tr7zw:item-nbt-api:2.16.1")
    implementation(files("lib/NMSLib_Plugin.jar"))
    implementation("com.github.cryptomorin:XSeries:13.7.1")

    // legacy build
    legacyImplementation("de.tr7zw:item-nbt-api:2.16.1")
    legacyImplementation(files("lib/NMSLib_Plugin-1.0-legacy.jar"))
    legacyImplementation("com.github.cryptomorin:XSeries:13.7.1")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly(files("lib/InfiniteKothAPI-1.0.jar"))
    compileOnly(files("lib/ManticHoes-2.0.5-strippedforapi.jar"))
    compileOnly(files("lib/ManticSwords-stripped.jar"))
    compileOnly(files("lib/ManticRods-stripped.jar"))
    compileOnly(files("lib/MiningEconomy-API.jar"))
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.jetbrains:annotations:20.1.0")
    annotationProcessor("org.jetbrains:annotations:20.1.0")
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
}

group = "me.fullpage"
version = "1.0.54.0"
description = "ManticLib"
java.sourceCompatibility = JavaVersion.VERSION_1_8

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.processResources {
    val props = mapOf("project" to mapOf("version" to version))
    inputs.properties(props)
    filesMatching("**/plugin.yml") {
        expand(props)
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

tasks.register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("legacyShadowJar") {
    group = "shadow"
    archiveFileName.set("${project.description}-$version-legacy.jar")
    from(sourceSets.main.get().output)
    configurations = setOf(legacyImplementation)

    relocate("me.fullpage.nmslib", "me.fullpage.manticlib.nmslib")
    relocate("de.tr7zw.annotations", "me.fullpage.manticlib.nbtapi.annotations")
    relocate("de.tr7zw.changeme.nbtapi", "me.fullpage.manticlib.nbtapi")
    relocate("com.cryptomorin", "me.fullpage.manticlib.cryptomorin")
}

tasks.build {
    dependsOn("shadowJar", "legacyShadowJar")
}

// NMSLib's full reactor needs BuildTools jars, so only NMSLib_API and NMSLib_V26_2 are built (mvn -o -pl NMSLib_API,NMSLib_V26_2 -am install) and swapped in.
tasks.register("refreshNmsLib") {
    group = "build"
    description = "Swaps the built NMSLib API and v26_2 classes into lib/NMSLib_Plugin.jar and records the NMSLib commit in its manifest (-PnmsLib=<checkout>, default ../NMSLib)"
    val nmsLib = file(findProperty("nmsLib") ?: "../NMSLib")
    val jar = file("lib/NMSLib_Plugin.jar")
    doLast {
        val prefix = "me/fullpage/nmslib/"
        val v26 = prefix + "v26_2/"
        val api = nmsLib.resolve("NMSLib_API/target/classes")
        val impl = nmsLib.resolve("NMSLib_V26_2/target/classes")
        if (!api.isDirectory || !impl.isDirectory) throw GradleException("Build NMSLib first: mvn -o -pl NMSLib_API,NMSLib_V26_2 -am install in $nmsLib")
        fun classes(root: File, keep: (String) -> Boolean) = root.walkTopDown().filter { it.isFile }
            .associateBy { it.relativeTo(root).invariantSeparatorsPath }.filterKeys { it.endsWith(".class") && keep(it) }
        val fresh = classes(api) { it.startsWith(prefix) && it.removePrefix(prefix).let { rest -> '/' !in rest || rest.startsWith("packet/") || rest.startsWith("chunk/") } } +
                classes(impl) { it.startsWith(v26) }
        val git = ProcessBuilder("git", "-C", nmsLib.path, "describe", "--always", "--dirty").redirectErrorStream(true).start()
        val commit = git.inputStream.bufferedReader().readText().trim()
        if (git.waitFor() != 0) throw GradleException("Could not read the NMSLib commit in $nmsLib: $commit")
        val added = 1767225600000L
        val tmp = File(jar.path + ".tmp")
        var replaced = 0
        var dropped = 0
        ZipFile(jar).use { src ->
            ZipOutputStream(tmp.outputStream()).use { out ->
                val manifest = src.getEntry("META-INF/MANIFEST.MF")?.let { entry -> src.getInputStream(entry).use { Manifest(it) } }
                        ?: Manifest().apply { mainAttributes.putValue("Manifest-Version", "1.0") }
                manifest.mainAttributes.putValue("NMSLib-Commit", commit)
                out.putNextEntry(ZipEntry("META-INF/MANIFEST.MF").apply { time = added })
                manifest.write(out)
                val existing = HashSet<String>()
                for (entry in src.entries()) {
                    existing += entry.name
                    if (entry.name == "META-INF/MANIFEST.MF") continue
                    val file = fresh[entry.name]
                    if (file == null && entry.name.startsWith(v26) && !entry.name.endsWith("/")) {
                        dropped++
                        continue
                    }
                    out.putNextEntry(ZipEntry(entry.name).apply { time = entry.time })
                    if (file != null) replaced++
                    (file?.inputStream() ?: src.getInputStream(entry)).use { it.copyTo(out) }
                }
                for (name in (fresh.keys - existing).sorted()) {
                    out.putNextEntry(ZipEntry(name).apply { time = added })
                    fresh.getValue(name).inputStream().use { it.copyTo(out) }
                }
                logger.lifecycle("NMSLib $commit: replaced $replaced, dropped $dropped, added ${(fresh.keys - existing).size}")
            }
        }
        Files.move(tmp.toPath(), jar.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}
