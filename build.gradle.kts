plugins {
    java
    `maven-publish`

    alias(libs.plugins.shadow)
    alias(libs.plugins.semver)
}

val archives_name: String by project
val mod_id: String by rootProject
val mod_name: String by rootProject

group = project.property("group").toString()
project.version = "${semver.semVersion}"

allprojects {
    apply(plugin = "java")
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21

        withSourcesJar()

        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    base { archivesName.set("${rootProject.base.archivesName.get()}${project.path.replace(":", "-")}") }

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
    shadow(project(":services")) { isTransitive = false }
    //    shadow(project(":fabric-like:fabric-loader")) { isTransitive = false }
//    shadow(project(":fabric-like:quilt-loader")) { isTransitive = false }
//    shadow(project(":forge:fml")) { isTransitive = false }
//    shadow(project(":forge:fml-40")) { isTransitive = false }
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
