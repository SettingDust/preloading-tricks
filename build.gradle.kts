import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import net.fabricmc.loom.task.RemapJarTask
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
import java.io.ByteArrayOutputStream
import java.util.jar.JarFile.MANIFEST_NAME
import java.util.jar.Manifest


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

tasks {
    shadowJar {
        val fabricLoader014Jar = project(":fabric-loader-0.14").tasks.named<RemapJarTask>("remapJar")
        val quiltLoader020Jar = project(":quilt-loader-0.20").tasks.named<RemapJarTask>("remapJar")
        val fml45 = project(":fml-45").tasks.named<RemapJarTask>("remapJar")
        val preloadingCallbacks = project(":preloading-callbacks").tasks.jar

        dependsOn(fabricLoader014Jar, quiltLoader020Jar, fml45, preloadingCallbacks)

        from(
            zipTree(preloadingCallbacks.get().archiveFile),
            fabricLoader014Jar.get().archiveFile,
            quiltLoader020Jar.get().archiveFile,
            fml45.get().archiveFile
        )

        transform(ManifestMergeTransformer())

        archiveClassifier.set("")
        mergeServiceFiles()
    }

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
        "1.19",
        "1.19.1",
        "1.19.2",
        "1.19.3",
        "1.19.4",
        "1.20",
        "1.20.1",
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

// https://stackoverflow.com/questions/66333597/how-to-merge-manifest-sections-with-gradle-and-shadowjar
class ManifestMergeTransformer : Transformer {
    @Input
    var includePackages: String = "" // regular expression that must match a given package

    @Input
    var excludePackages: String = "" // regular expression that must not match a given package
    private var manifest: Manifest? = null
    override fun getName() = "ManifestMergeTransformer"

    override fun canTransformResource(element: FileTreeElement): Boolean {
        return MANIFEST_NAME.equals(element.relativePath.pathString, ignoreCase = true)
    }

    override fun transform(context: TransformerContext) {
        if (manifest == null) {
            manifest = Manifest(context.`is`)
        } else {
            val toMerge = Manifest(context.`is`)
            for ((key, value) in toMerge.entries) {
                if (mustInclude(key)) {
                    manifest!!.entries[key] = value
                }
            }
            for ((key, value) in toMerge.mainAttributes) {
                if (mustInclude(key.toString())) manifest!!.mainAttributes[key] = value
            }
        }
        context.`is`.close()
    }

    private fun mustInclude(packageName: String) =
        (includePackages.isBlank() || packageName.matches(includePackages.toRegex())) && (excludePackages.isBlank() || !packageName.matches(
            excludePackages.toRegex()
        ))

    override fun hasTransformedResource(): Boolean {
        return true
    }

    override fun modifyOutputStream(os: ZipOutputStream, preserveFileTimestamps: Boolean) {
        val entry = ZipEntry(MANIFEST_NAME)
        entry.time = TransformerContext.getEntryTimestamp(preserveFileTimestamps, entry.time)
        os.putNextEntry(entry)
        if (manifest != null) {
            val manifestContents = ByteArrayOutputStream()
            manifest!!.write(manifestContents)
            os.write(manifestContents.toByteArray())
        }
    }
}
