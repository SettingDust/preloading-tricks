@file:OptIn(ExperimentalPathApi::class)

import earth.terrarium.cloche.api.attributes.MinecraftModLoader
import earth.terrarium.cloche.api.metadata.FabricMetadata
import earth.terrarium.cloche.tasks.GenerateFabricModJson
import earth.terrarium.cloche.util.target
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.core.utils.toPath
import net.msrandom.minecraftcodev.core.utils.zipFileSystem
import settingdust.cloche_template.buildsrc.*
import java.nio.file.StandardCopyOption
import kotlin.io.path.*

plugins {
    id("clocheTemplate.base")
    id("clocheTemplate.language.java")
    id("clocheTemplate.maven-publish")
}

// region Project Properties

val id: String by rootProject.properties

project.group = "settingdust.preloading_tricks"

// endregion

cloche {
    // region Common Targets

    val apiCommon = common("api")

    val coreCommon = common("core") {
        dependsOn(apiCommon)

        dependencies {
            compileOnly("org.spongepowered:mixin:0.8.7")
        }
    }

    // endregion

    // region ForgeLike Common Targets
    val sharedForgeLike = common("shared:forgelike") {
        dependsOn(coreCommon)
    }
    val sharedModLauncher = common("shared:modlauncher") {
        dependsOn(coreCommon, sharedForgeLike)
    }
    val sharedNeoForge = common("shared:neoforge") {
        dependsOn(coreCommon, sharedForgeLike)
    }

    val excludeAsm: ExternalModuleDependency.() -> Unit = {
        exclude(group = "org.ow2.asm")
    }
    val excludeGuavaAndAsm: ExternalModuleDependency.() -> Unit = {
        exclude(group = "com.google.guava")
        exclude(group = "org.ow2.asm")
    }
    // endregion

    // region Main Targets - Fabric

    val fabric = fabric("platform:fabric") {
        dependsOn(coreCommon)
        minecraftVersion = MinecraftVersion.`20_1`.value

        dependencies {
            multiversionDependencies.reflect.resolve(project).let {
                api(it)
                include(it)
            }
            include(multiversionDependencies.commonsUnchecked.resolve(project))
            multiversionDependencies.classTransform.resolve(project).let {
                api(it)
                include(it)
            }
            multiversionDependencies.classTransformAdditionalClassProvider.resolve(project).let {
                implementation(it)
                include(it)
            }
            multiversionDependencies.classTransformMixinsTranslator.resolve(project).let {
                implementation(it)
                include(it)
            }
            multiversionDependencies.byteBuddyAgent.resolve(project).let {
                api(it)
                include(it)
            }
        }

        metadata {
            languageAdapters.put(id, "$group.fabric.PreloadingTricksLanguageAdapter")
        }
    }

    // endregion

    // region Main Targets - Forge

    val forgeModLauncher = forge("platform:forge:modlauncher") {
        dependsOn(coreCommon, sharedModLauncher)
        minecraftVersion = MinecraftVersion.`20_1`.value

        dependencies {
            implementation(
                multiversionDependencies.mixinextras.resolve(
                    MinecraftModLoader.common,
                    MinecraftVersion.`20_1`,
                    project
                )
            )
            api(multiversionDependencies.reflect.resolve(project))
            api(multiversionDependencies.classTransform.resolve(project))
            implementation(multiversionDependencies.classTransformAdditionalClassProvider.resolve(project))
            implementation(multiversionDependencies.classTransformMixinsTranslator.resolve(project))
            implementation(multiversionDependencies.byteBuddyAgent.resolve(project))
        }

        val noNewerJavaAttribute = Attribute.of("noNewerJava", Boolean::class.javaObjectType)

        abstract class RemoveNewerJavaTransform : TransformAction<TransformParameters.None> {
            @get:InputArtifact
            abstract val inputArtifact: Provider<FileSystemLocation>

            override fun transform(outputs: TransformOutputs) {
                val input = inputArtifact.get().toPath()
                val newerJavaClasses = zipFileSystem(input).use { it.getPath("META-INF/versions/24").exists() }
                if (!newerJavaClasses) {
                    outputs.file(input)
                    return
                }
                val output = outputs.file(input.name.replace(".jar", "-noNewerJava.jar")).toPath()
                input.copyTo(output, StandardCopyOption.COPY_ATTRIBUTES)
                zipFileSystem(output).use { fs -> fs.getPath("META-INF/versions/24").deleteRecursively() }
            }
        }

        project.dependencies {
            attributesSchema { attribute(noNewerJavaAttribute) }
            artifactTypes.named(ArtifactTypeDefinition.JAR_TYPE) {
                attributes.attribute(noNewerJavaAttribute, false)
            }
            registerTransform(RemoveNewerJavaTransform::class) {
                from.attribute(noNewerJavaAttribute, false)
                to.attribute(noNewerJavaAttribute, true)
            }
        }

        tasks {
            named(generateModsTomlTaskName) { enabled = false }
        }
    }

    // endregion

    // region Main Targets - NeoForge

    val neoforgeModlauncher = neoforge("platform:neoforge:modlauncher") {
        dependsOn(coreCommon, sharedForgeLike, sharedModLauncher, sharedNeoForge)
        minecraftVersion = MinecraftVersion.`21_1`.value

        metadata {
            modLoader = "lowcodefml"
            loaderVersion { start = "0" }
        }

        dependencies {
            multiversionDependencies.reflect.resolve(project).let {
                api(it)
                legacyClasspath(it)
            }
            multiversionDependencies.classTransform.resolve(project).let {
                api(it, excludeAsm)
                legacyClasspath(it)
            }
            multiversionDependencies.classTransformAdditionalClassProvider.resolve(project).let {
                implementation(it, excludeGuavaAndAsm)
                legacyClasspath(it)
            }
            multiversionDependencies.classTransformMixinsTranslator.resolve(project).let {
                implementation(it, excludeGuavaAndAsm)
                legacyClasspath(it)
            }
            multiversionDependencies.byteBuddyAgent.resolve(project).let {
                implementation(it)
                legacyClasspath(it)
            }
        }

        tasks {
            named(generateModsTomlTaskName) { enabled = false }
        }
    }

    val neoforgeFancyModLoader = neoforge("platform:neoforge:fancy-mod-loader") {
        dependsOn(coreCommon, sharedForgeLike, sharedNeoForge)
        minecraftVersion = MinecraftVersion.`26_1`.value

        dependencies {
            multiversionDependencies.reflect.resolve(project).let {
                api(it)
                legacyClasspath(it)
            }
            multiversionDependencies.classTransform.resolve(project).let {
                api(it, excludeAsm)
                legacyClasspath(it)
            }
            multiversionDependencies.classTransformAdditionalClassProvider.resolve(project).let {
                implementation(it, excludeGuavaAndAsm)
                legacyClasspath(it)
            }
            multiversionDependencies.classTransformMixinsTranslator.resolve(project).let {
                implementation(it, excludeGuavaAndAsm)
                legacyClasspath(it)
            }
            multiversionDependencies.byteBuddyAgent.resolve(project).let {
                implementation(it)
                legacyClasspath(it)
            }
        }

    }

    // region Containers

    // region Fabric Container

    val fabricContainer = project.container(loader = MinecraftModLoader.fabric) {
        val metadataDirectory = project.layout.buildDirectory.dir("generated")
            .map { it.dir("metadata").dir(featureName) }
        val generateModJson =
            tasks.register<GenerateFabricModJson>(lowerCamelCaseGradleName(featureName, "generateModJson")) {
                modId = "${id}_container"
                metadata = objects.newInstance(FabricMetadata::class.java, fabric).apply {
                    license.value(cloche.metadata.license)
                    dependencies.value(cloche.metadata.dependencies)
                }
                loaderDependencyVersion = "0.18"
                output.set(metadataDirectory.map { it.file("fabric.mod.json") })
            }

        embed()
        dependencies {
            embed(target(fabric))
        }

        jar {
            dependsOn(generateModJson)
            from(metadataDirectory)
        }
    }

    // endregion

    // region Forge Container

    val forgeContainer = project.container(loader = MinecraftModLoader.forge) {
        embed()
        embed("boot") { into("libs/boot") }
        dependencies {
            embed(target(forgeModLauncher))
            embed("boot", multiversionDependencies.commonsUnchecked.resolve(project))
            embed("boot", multiversionDependencies.reflect.resolve(project))
            embed("boot", multiversionDependencies.classTransform.resolve(project))
            embed("boot", multiversionDependencies.classTransformAdditionalClassProvider.resolve(project))
            embed("boot", multiversionDependencies.classTransformMixinsTranslator.resolve(project))
            embed("boot", multiversionDependencies.byteBuddyAgent.resolve(project))
        }

        jar {
            manifest {
                attributes(
                    "FMLModType" to "GAMELIBRARY"
                )
            }
        }
    }

    // endregion

    // region NeoForge Container

    val neoforgeContainer = project.container(loader = MinecraftModLoader.neoforge) {
        embed()
        embed("boot") { into("libs/boot") }

        dependencies {
            embed(target(neoforgeModlauncher))
            embed(target(neoforgeFancyModLoader))
            embed("boot", multiversionDependencies.commonsUnchecked.resolve(project))
            embed("boot", multiversionDependencies.reflect.resolve(project))
            embed("boot", multiversionDependencies.classTransform.resolve(project))
            embed("boot", multiversionDependencies.classTransformAdditionalClassProvider.resolve(project))
            embed("boot", multiversionDependencies.classTransformMixinsTranslator.resolve(project))
            embed("boot", multiversionDependencies.byteBuddyAgent.resolve(project))
        }

        jar {
            manifest {
                attributes(
                    "FMLModType" to "GAMELIBRARY"
                )
            }
        }
    }

    // endregion

    // endregion

    // region Run Targets

    fabric("version:fabric:20.1") {
        minecraftVersion = MinecraftVersion.`20_1`.value
        dependencies {
            modRuntimeOnly(project(":")) {
                isTransitive = false
            }
        }
    }

    fabric("version:fabric:21.1") {
        minecraftVersion = MinecraftVersion.`21_1`.value
        dependencies {
            modRuntimeOnly(project(":")) {
                isTransitive = false
            }
        }
    }

    fabric("version:fabric:26.1") {
        minecraftVersion = MinecraftVersion.`26_1`.value
        dependencies {
            modRuntimeOnly(project(":")) {
                isTransitive = false
            }
        }
    }

    forge("version:forge:20.1") {
        minecraftVersion = MinecraftVersion.`20_1`.value
        dependencies {
            modRuntimeOnly(project(":")) {
                isTransitive = false
            }
        }
    }

    neoforge("version:neoforge:21.1") {
        minecraftVersion = MinecraftVersion.`21_1`.value
        dependencies {
            legacyClasspath(project(":")) {
                isTransitive = false
            }
        }
    }

    neoforge("version:neoforge:26.1") {
        minecraftVersion = MinecraftVersion.`26_1`.value
        dependencies {
            modRuntimeOnly(project(":")) {
                isTransitive = false
            }
        }
    }

    // endregion

    // endregion

    // endregion

    project.configureFinalJar(
        containers = listOf(fabricContainer, forgeContainer, neoforgeContainer),
        allTargets = targets,
    )
}

