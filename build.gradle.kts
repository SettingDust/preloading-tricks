import groovy.lang.Closure

plugins {
    java
    `maven-publish`

    alias(libs.plugins.shadow)
    alias(libs.plugins.git.version)
}

val archives_name: String by project
val mod_id: String by rootProject
val mod_name: String by rootProject

group = project.property("group").toString()

val gitVersion: Closure<String> by extra
version = gitVersion()

base {
    archivesName.set(properties["archives_name"].toString())
}

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
    shadow(project(":fabric:fabric-loader")) { isTransitive = false }
    shadow(project(":fabric:quilt-loader")) { isTransitive = false }
    shadow(project(":neoforge:fml")) { isTransitive = false }
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier.set("")
        mergeServiceFiles()

        doFirst {
            manifest {
                from(configurations.flatMap { it.files }.map { zipTree(it) }
                    .map { zip -> zip.find { it.name.equals("MANIFEST.MF") } })
            }
        }
    }

    build {
        dependsOn(shadowJar)
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
