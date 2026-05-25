import earth.terrarium.cloche.REMAPPED_ATTRIBUTE
import earth.terrarium.cloche.api.attributes.MinecraftModLoader
import earth.terrarium.cloche.api.metadata.FabricMetadata
import earth.terrarium.cloche.api.target.FabricTarget
import earth.terrarium.cloche.api.target.ForgeLikeTarget
import earth.terrarium.cloche.api.target.NeoforgeTarget
import earth.terrarium.cloche.tasks.GenerateFabricModJson
import earth.terrarium.cloche.util.target
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import settingdust.cloche_template.buildsrc.*

plugins {
    id("clocheTemplate.base")
    id("clocheTemplate.language.java")
    id("clocheTemplate.maven-publish")
}

// region Project Properties

val id: String by rootProject.properties

group = "settingdust.preloading_tricks"

// endregion

clocheTemplate {
    // Generated-project switch for dev/remapped publication & runtime exposure.
    // Keep default true to preserve current compatibility with existing consumers.
    remappedDevVariants.set(false)
}

cloche {
    // region Common Targets

    val apiCommon = common("api")

    val coreCommon = common("core") {
        dependsOn(apiCommon)
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

        metadata {
            languageAdapters.put(id, "$group.fabric.PreloadingTricksLanguageAdapter")
        }
    }

    // endregion

    // region Main Targets - Forge

    val forgeModLauncher = forge("platform:forge:modlauncher") {
        dependsOn(coreCommon, sharedModLauncher)
        minecraftVersion = MinecraftVersion.`20_1`.value

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

        tasks {
            named(generateModsTomlTaskName) { enabled = false }
        }
    }

    val neoforgeFancyModLoader = neoforge("platform:neoforge:fancy-mod-loader") {
        dependsOn(coreCommon, sharedForgeLike, sharedNeoForge)
        minecraftVersion = MinecraftVersion.`26_1`.value
    }

    // endregion

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
        dependencies {
            embed(target(forgeModLauncher))
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

        dependencies {
            embed(target(neoforgeFancyModLoader))
            embed(target(neoforgeModlauncher))
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

    // region Version Targets

    // region Fabric Version Targets

    fabric("version:fabric:20.1") {
        minecraftVersion = MinecraftVersion.`20_1`.value
    }

    fabric("version:fabric:21.1") {
        minecraftVersion = MinecraftVersion.`21_1`.value
    }

    fabric("version:fabric:26.1") {
        minecraftVersion = MinecraftVersion.`26_1`.value
    }

    // endregion

    // region Forge Version Targets

    forge("version:forge:20.1") {
        minecraftVersion = MinecraftVersion.`20_1`.value
    }

    neoforge("version:neoforge:21.1") {
        minecraftVersion = MinecraftVersion.`21_1`.value
    }

    neoforge("version:neoforge:26.1") {
        minecraftVersion = MinecraftVersion.`26_1`.value
    }

    // endregion

    // endregion

    targets.all {
        val bootClasspath = if (this@all is ForgeLikeTarget && !isVersionTarget()) {
            project.configurations.register(lowerCamelCaseGradleName(featureName, "bootClasspath")) {
                isCanBeResolved = true
                isCanBeConsumed = false
                isTransitive = false
            }.also {
                tasks.named<ProcessResources>(sourceSet.processResourcesTaskName) {
                    from(it) { into("libs/boot") }
                }
            }
        } else {
            null
        }

        dependencies {
            operator fun DependencyCollector.invoke(
                spec: MultiversionDependencySpec,
                configure: ExternalModuleDependency.() -> Unit = {},
            ) {
                add(spec.resolve(this@all, project).apply(configure))
            }

            when {
                name == "core" -> compileOnly("org.spongepowered:mixin:0.8.7")

                isVersionTarget() && this@all is FabricTarget -> {
                    modRuntimeOnly(project(":")) { isTransitive = false }
                }

                isVersionTarget() && this@all is NeoforgeTarget && minecraftVersionEnum() == MinecraftVersion.`21_1` -> {
                    legacyClasspath(project(":")) {
                        isTransitive = false

                        attributes {
                            attribute(REMAPPED_ATTRIBUTE, false)
                        }
                    }
                }

                isVersionTarget() && this@all is ForgeLikeTarget -> {
                    modRuntimeOnly(project(":")) { isTransitive = false }
                }

                this@all is FabricTarget -> {
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

                this@all is NeoforgeTarget -> {
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

                this@all is ForgeLikeTarget -> {
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
            }
        }

        bootClasspath?.let {
            project.dependencies {
                it(multiversionDependencies.commonsUnchecked.resolve(project))
                it(multiversionDependencies.reflect.resolve(project))
                it(multiversionDependencies.classTransform.resolve(project))
                project.dependencies.add(
                    it.name,
                    multiversionDependencies.classTransformAdditionalClassProvider.resolve(project)
                )
                it(multiversionDependencies.classTransformMixinsTranslator.resolve(project))
                it(multiversionDependencies.byteBuddyAgent.resolve(project))
            }
        }
    }

    project.configureFinalJar(
        containers = listOf(fabricContainer, forgeContainer, neoforgeContainer),
        allTargets = targets,
    )
}
