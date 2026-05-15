package settingdust.cloche_template.buildsrc

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PreserveFirstFoundResourceTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ResourceTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import earth.terrarium.cloche.REMAPPED_ATTRIBUTE
import earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute
import earth.terrarium.cloche.api.target.MinecraftTarget
import earth.terrarium.cloche.util.fromJars
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
import org.gradle.api.Project
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.language.jvm.tasks.ProcessResources
import java.nio.charset.StandardCharsets

class ForgeMetadataTransformer : ResourceTransformer {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val collected = JsonArray()
    private val path = "META-INF/jarjar/metadata.json"
    private var transformed = false

    override fun canTransformResource(element: FileTreeElement): Boolean {
        return element.path == path
    }

    override fun transform(context: TransformerContext) {
        context.inputStream.use { input ->
            val json = gson.fromJson(input.reader(Charsets.UTF_8), JsonObject::class.java)
            val jars = json.getAsJsonArray("jars")
            if (jars != null) {
                for (jar in jars) {
                    collected.add(jar)
                }
            }
            transformed = true
        }
    }

    override fun hasTransformedResource(): Boolean = transformed

    override fun modifyOutputStream(os: ZipOutputStream, preserveFileTimestamps: Boolean) {
        if (collected.size() == 0) return

        val merged = JsonObject().apply {
            add("jars", collected)
        }

        os.putNextEntry(ZipEntry(path))
        os.write(gson.toJson(merged).toByteArray(StandardCharsets.UTF_8))
        os.closeEntry()
    }
}

fun Project.configureFinalJar(
    containers: List<ContainerScope>,
    allTargets: Iterable<MinecraftTarget>,
) {
    val sourceSets = extensions.getByType<SourceSetContainer>()

    tasks.withType<ProcessResources> {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    tasks.withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    tasks.named<ShadowJar>("shadowJar") {
        enabled = false
    }

    val shadowMergedDevJar = tasks.register<ShadowJar>("shadowMergedDevJar") {
        archiveClassifier.set("dev")
        configurations.set(emptyList())

        for (container in containers) {
            val output = container.includeDevJarTask.flatMap { it.archiveFile }
            from(zipTree(output))

            manifest.fromJars(serviceOf(), output)
        }

        mergeServiceFiles()
        append("META-INF/accesstransformer.cfg")

        transform<ForgeMetadataTransformer>()
        transform<PreserveFirstFoundResourceTransformer>()
        filesMatching("**/*.class") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
    }

    val shadowMergedJar = tasks.register<ShadowJar>("shadowMergedJar") {
        archiveClassifier.set("")
        configurations.set(emptyList())

        for (container in containers) {
            val output = container.includeJarTask.flatMap { it.archiveFile }
            from(zipTree(output))

            manifest.fromJars(serviceOf(), output)
        }

        mergeServiceFiles()
        append("META-INF/accesstransformer.cfg")

        transform<ForgeMetadataTransformer>()
        transform<PreserveFirstFoundResourceTransformer>()
        filesMatching("**/*.class") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
    }

    val shadowSourcesJar = tasks.register<ShadowJar>("shadowSourcesJar") {
        dependsOn(allTargets.map { it.generateModsManifestTaskName })

        mergeServiceFiles()
        archiveClassifier.set("sources")
        from(sourceSets.map { it.allSource })

        doFirst {
            manifest {
                from(source.filter { it.name.equals("MANIFEST.MF") }.toList())
            }
        }
    }

    tasks.named(LifecycleBasePlugin.BUILD_TASK_NAME) {
        dependsOn(shadowMergedDevJar, shadowMergedJar, shadowSourcesJar)
    }

    tasks.named<Jar>("jar") {
        enabled = false
    }

    afterEvaluate {
        val component = components.named("java").get() as AdhocComponentWithVariants
        val shadowRuntimeElementsConfiguration = configurations.getByName("shadowRuntimeElements")
        val runtimeElementsConfiguration = configurations.getByName("runtimeElements")

        component.withVariantsFromConfiguration(shadowRuntimeElementsConfiguration) { skip() }

        runtimeElementsConfiguration.apply {
            outgoing.artifacts.clear()
            outgoing.artifact(shadowMergedJar)

            outgoing.variants.create("remapped") {
                attributes {
                    attribute(REMAPPED_ATTRIBUTE, true)
                    attribute(RemapNamespaceAttribute.ATTRIBUTE, RemapNamespaceAttribute.INITIAL)
                }
                artifact(shadowMergedDevJar)
            }
        }

        component.addVariantsFromConfiguration(runtimeElementsConfiguration) {
            if (configurationVariant.name in listOf("classes", "resources")) {
                skip()
            }
            mapToMavenScope("runtime")
        }

        allTargets
            .filter(MinecraftTarget::isVersionTarget)
            .forEach { target ->
                for (variant in listOf(
                    "${target.featureName}ApiElements",
                    "${target.featureName}RuntimeElements"
                )) {
                    component.withVariantsFromConfiguration(configurations.getByName(variant)) { skip() }
                }
            }
    }
}