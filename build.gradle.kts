import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import groovy.lang.Closure
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream

plugins {
    java
    `maven-publish`

    alias(catalog.plugins.shadow)
    alias(catalog.plugins.git.version)

    alias(catalog.plugins.neoforge.moddev) apply false
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
    shadow(project(":services")) { isTransitive = false }

    shadow(project(":fabric:fabric-loader")) { isTransitive = false }
    shadow(project(":fabric:quilt-loader")) { isTransitive = false }

    shadow(project(":neoforge:fancy-mod-loader", configuration = "shadow")) {
        isTransitive = false
    }

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
        dependsOn(
            ":lexforge:forge-mod-loader:shadowJar",
            ":lexforge:forge-mod-loader-40:shadowJar",
            ":neoforge:fancy-mod-loader:shadowJar"
        )

        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier.set("")
        mergeServiceFiles()

        transform(object : Transformer {
            private val GSON = GsonBuilder().setPrettyPrinting().create()
            private var json = JsonObject()
            private val PATH = "META-INF/jarjar/metadata.json"

            override fun getName() = "JarJar Metadata"

            override fun canTransformResource(element: FileTreeElement) = element.relativePath.pathString == PATH

            override fun transform(context: TransformerContext) {
                val jsonElement = JsonParser.parseReader(context.`is`.reader()).asJsonObject
                val existJars = json.asMap()["jars"]?.asJsonArray?.asList()?.toMutableSet() ?: mutableSetOf()
                existJars.addAll(jsonElement.getAsJsonArray("jars").asList())
                json.add("jars", JsonArray(existJars.size).also { it.asList().addAll(existJars) })
            }

            override fun hasTransformedResource() = !json.isEmpty

            override fun modifyOutputStream(os: ZipOutputStream, preserveFileTimestamps: Boolean) {
                val entry = ZipEntry(PATH)
                entry.time = TransformerContext.getEntryTimestamp(preserveFileTimestamps, entry.time)
                os.putNextEntry(entry)
                os.write(GSON.toJson(json).encodeToByteArray())

                json = JsonObject()
            }
        })

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

