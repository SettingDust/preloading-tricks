import net.fabricmc.loom.task.RemapJarTask

plugins {
    java
    `maven-publish`

    alias(libs.plugins.minotaur)
    alias(libs.plugins.architectury)
    alias(libs.plugins.architectury.loom) apply false
}

val archives_name: String by project
val mod_id: String by rootProject
val mod_name: String by rootProject

group = project.property("group").toString()
version = project.property("version").toString()

architectury {
    compileOnly()
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

tasks {
    jar {
        val fabricLoader014Jar = project(":fabric-loader-0.14").tasks.named<RemapJarTask>("remapJar")
        val quiltLoader020Jar = project(":quilt-loader-0.20").tasks.named<RemapJarTask>("remapJar")
        val fml45 = project(":fml-45").tasks.named<RemapJarTask>("remapJar")
        dependsOn(fabricLoader014Jar, quiltLoader020Jar, fml45)
        from(
            zipTree(fabricLoader014Jar.get().archiveFile),
            zipTree(quiltLoader020Jar.get().archiveFile),
            zipTree(fml45.get().archiveFile)
        )

            manifest {
                from(setOf(
                    zipTree(fabricLoader014Jar.get().outputs.files.singleFile),
                    zipTree(quiltLoader020Jar.get().outputs.files.singleFile),
                    zipTree(fml45.get().outputs.files.singleFile)
                ).map { zip ->
                    zip.find { it.name.equals("MANIFEST.MF") }
                })
            }

        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}
