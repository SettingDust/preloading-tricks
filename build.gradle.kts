plugins {
    java
    `maven-publish`

    alias(libs.plugins.dotenv)

    alias(libs.plugins.minotaur)
    alias(libs.plugins.architectury)
    alias(libs.plugins.architectury.loom) apply false
    alias(libs.plugins.shadow)
}

val archives_name: String by project
val mod_id: String by rootProject
val mod_name: String by rootProject

group = project.property("group").toString()
version = project.property("version").toString()

architectury {
    compileOnly()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withSourcesJar()
}

subprojects {
    apply(plugin = "java")

    group = rootProject.group
    version = rootProject.version

    base {
        archivesName.set("$archives_name-${name}")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withSourcesJar()
    }

    tasks {
        jar {
            from("LICENSE") {
                rename { "${it}_${base.archivesName}" }
            }
        }

        val properties = mapOf(
            "id" to mod_id,
            "version" to rootProject.version,
            "group" to rootProject.group,
            "name" to mod_name,
            "description" to rootProject.property("mod_description").toString(),
            "author" to rootProject.property("mod_author").toString(),
            "source" to rootProject.property("mod_source").toString(),
//            "fabric_loader_version" to rootProject.libs.versions.fabric.loader.get(),
//            "quilt_loader_version" to rootProject.libs.versions.quilt.loader.get(),
//            "forge_version" to rootProject.libs.versions.forge.get(),
            "schema" to "\$schema",
        )

        withType<ProcessResources> {
            inputs.properties(properties)
            filesMatching(listOf("fabric.mod.json", "quilt.mod.json", "META-INF/mods.toml", "*.mixins.json")) {
                expand(properties)
            }
        }
    }
}

dependencies {
    shadow(project(":fabric-like:fabric-loader")) { isTransitive = false }
    shadow(project(":fabric-like:quilt-loader")) { isTransitive = false }
    shadow(project(":forge:fml")) { isTransitive = false }
    shadow(project(":forge:fml-40")) { isTransitive = false }
    shadow(project(":preloading-callbacks")) { isTransitive = false }
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier.set("")
        mergeServiceFiles()

        manifest {
            from(configurations.flatMap { it.files }.map { zipTree(it) }
                .map { zip -> zip.find { it.name.equals("MANIFEST.MF") } })
        }
    }
//    shadowJar {
//        val fabricLoaderJar = project(":fabric-like:fabric-loader").tasks.named<RemapJarTask>("remapJar")
//        val quiltLoaderJar = project(":fabric-like:quilt-loader").tasks.named<RemapJarTask>("remapJar")
//        val fml = project(":forge:fml").tasks.named<RemapJarTask>("remapJar")
//        val preloadingCallbacks = project(":preloading-callbacks").tasks.jar
//
//        dependsOn(fabricLoaderJar, quiltLoaderJar, fml, preloadingCallbacks)
//
//        from(
//            zipTree(preloadingCallbacks.get().archiveFile),
//            fabricLoaderJar.get().archiveFile,
//            quiltLoaderJar.get().archiveFile,
//            fml.get().archiveFile
//        )
//
//        manifest {
//            from(setOf(
//                zipTree(fabricLoaderJar.get().outputs.files.singleFile),
//                zipTree(quiltLoaderJar.get().outputs.files.singleFile),
//                zipTree(fml.get().outputs.files.singleFile)
//            ).map { zip ->
//                zip.find { it.name.equals("MANIFEST.MF") }
//            })
//        }
//
//        archiveClassifier.set("")
//        mergeServiceFiles()
//    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }
}

modrinth {
    token.set(env.MODRINTH_TOKEN.value) // This is the default. Remember to have the MODRINTH_TOKEN environment variable set or else this will fail, or set it to whatever you want - just make sure it stays private!
    projectId.set("preloading-tricks") // This can be the project ID or the slug. Either will work!
    syncBodyFrom.set(rootProject.file("README.md").readText())
    versionType.set("release") // This is the default -- can also be `beta` or `alpha`
    uploadFile.set(tasks.shadowJar) // With Loom, this MUST be set to `remapJar` instead of `jar`!
    changelog.set(rootProject.file("CHANGELOG.md").readText())
    gameVersions.addAll(
        "1.18",
        "1.18.1",
        "1.18.2",
        "1.18.2",
        "1.19",
        "1.19.1",
        "1.19.2",
        "1.19.3",
        "1.19.4",
        "1.20",
        "1.20.1",
        "1.20.2",
    ) // Must be an array, even with only one version
    loaders.addAll(
        "fabric",
        "quilt",
        "forge"
    ) // Must also be an array - no need to specify this if you're using Loom or ForgeGradle
}

publishing {
    publications {
        create<MavenPublication>("preloading-tricks") {
            groupId = "${rootProject.group}"
            artifactId = base.archivesName.get()
            version = "${rootProject.version}"
            artifact(tasks.shadowJar)
        }
    }
}
