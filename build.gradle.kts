import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.lang.Closure

plugins {
    java
    `maven-publish`

    alias(catalog.plugins.shadow)
    alias(catalog.plugins.git.version)
}

apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/gradle_issue_15754.gradle.kts")

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withSourcesJar()

        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
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
//            "fabric_loader_version" to rootProject.catalog.versions.fabric.loader.get(),
//            "quilt_loader_version" to rootProject.catalog.versions.quilt.loader.get(),
//            "forge_version" to rootProject.catalog.versions.forge.get(),
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
    shadow(project(":fabric:fabric-loader")) { isTransitive = false }
    shadow(project(":fabric:quilt-loader")) { isTransitive = false }

    shadow(project(":neoforge:fancy-mod-loader")) { isTransitive = false }

    shadow(project(":lexforge:forge-mod-loader")) { isTransitive = false }
    shadow(project(":lexforge:forge-mod-loader-40")) { isTransitive = false }
}

tasks {
    val shadowSourcesJar by creating(ShadowJar::class) {
        mergeServiceFiles()
        archiveClassifier.set("sources")
        from(subprojects.map { it.sourceSets.main.get().allSource })

        doFirst {
            manifest {
                from(source.filter { it.name.equals("MANIFEST.MF") }.toList())
            }
        }
    }

    shadowJar {
        dependsOn(":lexforge:forge-mod-loader:shadowJar", ":lexforge:forge-mod-loader-40:shadowJar")

        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier.set("")
        mergeServiceFiles()

        doFirst {
            manifest {
                from(configurations.flatMap { it.files }.filter { it.exists() }.map { zipTree(it) }
                    .map { zip -> zip.find { it.name.equals("MANIFEST.MF") } })
            }
        }
        finalizedBy(shadowSourcesJar)
    }

    build {
        dependsOn(shadowJar, shadowSourcesJar)
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

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}
