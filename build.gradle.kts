@file:Suppress("UnstableApiUsage")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import earth.terrarium.cloche.INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE
import earth.terrarium.cloche.api.attributes.CompilationAttributes
import earth.terrarium.cloche.api.attributes.TargetAttributes
import earth.terrarium.cloche.api.metadata.CommonMetadata
import earth.terrarium.cloche.api.metadata.FabricMetadata
import earth.terrarium.cloche.api.target.FabricTarget
import earth.terrarium.cloche.api.target.ForgeLikeTarget
import earth.terrarium.cloche.api.target.MinecraftTarget
import earth.terrarium.cloche.api.target.NeoforgeTarget
import earth.terrarium.cloche.tasks.GenerateFabricModJson
import groovy.lang.Closure
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.fabric.MinecraftCodevFabricPlugin
import net.msrandom.minecraftcodev.fabric.task.JarInJar
import net.msrandom.minecraftcodev.forge.task.JarJar
import org.gradle.jvm.tasks.Jar


plugins {
    java
    idea

    id("com.palantir.git-version") version "4.1.0"

    id("com.gradleup.shadow") version "9.0.2"

    id("earth.terrarium.cloche") version "0.15.1-dust"
}

val archive_name: String by rootProject.properties
val id: String by rootProject.properties
val source: String by rootProject.properties

group = "settingdust.preloading_tricks"

val gitVersion: Closure<String> by extra
version = gitVersion()

base { archivesName = archive_name }

repositories {
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven")
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    mavenCentral()

    cloche {
        librariesMinecraft()
        main()
        mavenFabric()
        mavenForge()
        mavenNeoforged()
        mavenNeoforgedMeta()
        mavenParchment()
    }

    mavenLocal()
}

class MinecraftVersionCompatibilityRule : AttributeCompatibilityRule<String> {
    override fun execute(details: CompatibilityCheckDetails<String>) {
        details.compatible()
    }
}

dependencies {
    attributesSchema {
        attribute(TargetAttributes.MINECRAFT_VERSION) {
            compatibilityRules.add(MinecraftVersionCompatibilityRule::class)
        }
    }
}

val containerTasks = mutableSetOf<TaskProvider<out Jar>>()

cloche {
    metadata {
        modId = id
        name = rootProject.property("name").toString()
        description = rootProject.property("description").toString()
        license = "Apache License 2.0"
        icon = "assets/$id/icon.png"
        sources = source
        issues = "$source/issues"
        author("SettingDust")

        dependency {
            modId = "minecraft"
            type = CommonMetadata.Dependency.Type.Required
            version {
                start = "1.20.1"
            }
        }
    }

    mappings {
        official()
    }

    common {
        // mixins.from(file("src/common/main/resources/$id.mixins.json"))
        // accessWideners.from(file("src/common/main/resources/$id.accessWidener"))

        dependencies {
            compileOnly("org.spongepowered:mixin:0.8.7")
        }
    }

    val commons = mapOf(
        "1.20.1" to common("common:1.20.1") {
            // mixins.from("src/common/1.20.1/main/resources/$id.1_20.mixins.json")
        },
        "1.21.1" to common("common:1.21.1") {
            // mixins.from("src/common/1.21.1/main/resources/$id.1_21.mixins.json")
        },
    )

    run fabric@{
        val fabricCommon = common("fabric:common") {
            // mixins.from(file("src/fabric/common/main/resources/$id.fabric.mixins.json"))
        }

        val fabric1201 = fabric("fabric:1.20.1") {
            minecraftVersion = "1.20.1"

            metadata {
                dependency {
                    modId = "minecraft"
                    type = CommonMetadata.Dependency.Type.Required
                    version {
                        start = "1.20.1"
                        end = "1.21"
                    }
                }
            }

            dependencies {
                fabricApi("0.92.6")

                catalog.asmFabricLoader.get17().let {
                    implementation(it)
                    include(it)
                }
            }

            tasks.named<GenerateFabricModJson>(generateModsManifestTaskName) {
                modId = "${id}_1_20"
            }
        }

        val fabric121 = fabric("fabric:1.21") {
            minecraftVersion = "1.21.1"

            metadata {
                dependency {
                    modId = "minecraft"
                    type = CommonMetadata.Dependency.Type.Required
                    version {
                        start = "1.21"
                    }
                }
            }

            dependencies {
                fabricApi("0.116.6")

                catalog.asmFabricLoader.get21().let {
                    implementation(it)
                    include(it)
                }
            }

            tasks.named<GenerateFabricModJson>(generateModsManifestTaskName) {
                modId = "${id}_1_21"
            }
        }

        run container@{
            val featureName = "containerFabric"
            val metadataDirectory = project.layout.buildDirectory.dir("generated")
                .map { it.dir("metadata").dir(featureName) }
            val include = configurations.register(lowerCamelCaseGradleName(featureName, "include")) {
                isCanBeResolved = true
                isTransitive = false

                attributes {
                    attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, true)
                    attribute(CompilationAttributes.DATA, false)
                }
            }
            val targets = setOf(fabric1201, fabric121)

            dependencies {
                for (target in targets) {
                    include(project(":")) {
                        capabilities {
                            requireFeature(target.capabilitySuffix!!)
                        }
                    }
                }
            }

            tasks {
                val generateModJson =
                    register<GenerateFabricModJson>(lowerCamelCaseGradleName(featureName, "generateModJson")) {
                        modId = id
                        targetMetadata = objects.newInstance(FabricMetadata::class.java, fabric1201).apply {
                            license.value(cloche.metadata.license)
                            dependencies.value(cloche.metadata.dependencies)
                        }
                        loaderDependencyVersion = "0.17"
                        output.set(metadataDirectory.map { it.file("fabric.mod.json") })
                    }

                val jar = register<Jar>(lowerCamelCaseGradleName(featureName, "jar")) {
                    group = "build"
                    archiveClassifier = "fabric"
                    destinationDirectory = intermediateOutputsDirectory
                    dependsOn(generateModJson)
                    from(metadataDirectory)
                }

                val includesJar = register<JarInJar>(lowerCamelCaseGradleName(featureName, "includeJar")) {
                    dependsOn(targets.map { it.includeJarTaskName })

                    archiveClassifier = "fabric"
                    input = jar.flatMap { it.archiveFile }
                    fromResolutionResults(include)
                }

                containerTasks += includesJar

                build {
                    dependsOn(includesJar)
                }
            }
        }

        targets.withType<FabricTarget> {
            loaderVersion = "0.17.2"

            includedClient()

            dependsOn(fabricCommon)

            metadata {
                entrypoint("afl:prePrePreLaunch", "$group.fabric.PreloadingTricksLanguageAdapterEntrypoint")
                custom("afl:classtransform", "$id.fabric.classtransform.json")
            }

            dependencies {
            }
        }
    }

    val commonForgeLike = common("common:forge") {

    }

    run forge@{
        val forge1201 = forge("forge:1.20.1") {
            minecraftVersion = "1.20.1"
            loaderVersion = "47.4.4"

            runs {
                client {
                    env("MOD_CLASSES", "")
                }
            }

            metadata {
                dependency {
                    modId = "minecraft"
                    type = CommonMetadata.Dependency.Type.Required
                    version {
                        start = "1.20.1"
                        end = "1.21"
                    }
                }
            }

            repositories {
                maven("https://repo.spongepowered.org/maven") {
                    content {
                        includeGroup("org.spongepowered")
                    }
                }
            }

            dependencies {
                implementation("org.spongepowered:mixin:0.8.7")
                compileOnly(catalog.mixinextras.common)
                implementation(catalog.mixinextras.forge)

                implementation(catalog.reflect)

                implementation(catalog.classTransform)
                implementation(catalog.classTransform.additionalClassProvider)
            }

            val embed by configurations.register(lowerCamelCaseGradleName(featureName, "embed")) {
                isTransitive = false
            }

            project.dependencies {
                embed(catalog.reflect)
                embed(catalog.classTransform)
                embed(catalog.classTransform.additionalClassProvider)
            }

            tasks {
                named(generateModsTomlTaskName) {
                    enabled = false
                }

                val jar = named<Jar>(lowerCamelCaseGradleName(featureName, "jar")) {
                    from(embed) {
                        into("libs")
                    }

                    manifest {
                        attributes("FMLModType" to "LIBRARY")
                    }
                }

                val includeJar = named<JarJar>(lowerCamelCaseGradleName(featureName, "includeJar"))

                val deleteJarInModFolder = register<Delete>(
                    lowerCamelCaseGradleName(featureName, "deleteJarInModFolder")
                ) {
                    delete(fileTree(layout.projectDirectory.dir("run/mods")) {
                        include("$archive_name*.jar")
                    })
                }

                val copyToModFolder = register<Copy>(
                    lowerCamelCaseGradleName(featureName, "copyToModFolder")
                ) {
                    from(deleteJarInModFolder, includeJar.flatMap { it.archiveFile })
                    into(layout.projectDirectory.dir("run/mods"))
                }

                afterEvaluate {
                    named(lowerCamelCaseGradleName("prepare", featureName, "clientRun")) {
                        dependsOn(copyToModFolder)
                    }
                }
            }
        }
    }

    run neoforge@{
        val neoforge121 = neoforge("neoforge:1.21") {
            minecraftVersion = "1.21.1"

            metadata {
                dependency {
                    modId = "minecraft"
                    type = CommonMetadata.Dependency.Type.Required
                    version {
                        start = "1.21"
                    }
                }
            }

            dependencies {
                catalog.reflect.let {
                    implementation(it)
                    legacyClasspath(it)
                }
                catalog.classTransform.let {
                    implementation(it)
                    legacyClasspath(it)
                }
                catalog.classTransform.additionalClassProvider.let {
                    implementation(it) {
                        exclude(group = "com.google.guava")
                    }
                    legacyClasspath(it)
                }
            }

            val legacyClasspath by configurations.named(lowerCamelCaseGradleName(featureName, "legacyClasspath"))

            project.dependencies {
                legacyClasspath(catalog.reflect)
                legacyClasspath(catalog.classTransform)
                legacyClasspath(catalog.classTransform.additionalClassProvider)
            }

            val embed by configurations.register(lowerCamelCaseGradleName(featureName, "embed")) {
                isTransitive = false
            }

            project.dependencies {
                embed(catalog.reflect)
                embed(catalog.classTransform)
                embed(catalog.classTransform.additionalClassProvider)
            }

            tasks {
                val jar = named<Jar>(lowerCamelCaseGradleName(featureName, "jar")) {
                    from(embed) {
                        into("libs")
                    }

                    manifest {
                        attributes("FMLModType" to "LIBRARY")
                    }
                }
            }
        }

        run container@{
            val featureName = "containerNeoforge"
            val include = configurations.register(lowerCamelCaseGradleName(featureName, "include")) {
                isCanBeResolved = true
                isTransitive = false

                attributes {
                    attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, true)
                    attribute(CompilationAttributes.DATA, false)
                }
            }
            val targets = setOf(neoforge121)

            dependencies {
                for (target in targets) {
                    include(project(":")) {
                        capabilities {
                            requireFeature(target.capabilitySuffix!!)
                        }
                    }
                }

                include(catalog.reflect)
                include(catalog.classTransform)
                include(catalog.classTransform.additionalClassProvider)
            }

            tasks {
                val jar = register<Jar>(lowerCamelCaseGradleName(featureName, "jar")) {
                    group = "build"

                    archiveClassifier = "neoforge"
                    destinationDirectory = intermediateOutputsDirectory
                }

                val includesJar = register<JarJar>(lowerCamelCaseGradleName(featureName, "includeJar")) {
                    group = "build"
                    dependsOn(targets.map { it.includeJarTaskName })

                    archiveClassifier = "neoforge"

                    input = jar.flatMap { it.archiveFile }
                    fromResolutionResults(include)
                }

                containerTasks += includesJar

                build {
                    dependsOn(includesJar)
                }
            }
        }

        targets.withType<NeoforgeTarget> {
            loaderVersion = "21.1.192"

            metadata {
            }
        }
    }

    targets.withType<ForgeLikeTarget> {
        dependsOn(commonForgeLike)
    }

    targets.all {
        dependsOn(commons.getValue(minecraftVersion.get()))

        runs {
            client {
                jvmArguments(
                    "-Dmixin.debug.verbose=true",
                    "-Dmixin.debug.export=true",
                    "-Dclasstransform.dumpClasses=true"
                )
            }
        }

        mappings {
            parchment(minecraftVersion.map {
                when (it) {
                    "1.20.1" -> "2023.09.03"
                    "1.21.1" -> "2024.11.17"
                    else -> throw IllegalArgumentException("Unsupported minecraft version $it")
                }
            })
        }
    }
}

val SourceSet.includeJarTaskName: String
    get() = lowerCamelCaseGradleName(takeUnless(SourceSet::isMain)?.name, "includeJar")

val MinecraftTarget.includeJarTaskName: String
    get() = when (this) {
        is FabricTarget -> sourceSet.includeJarTaskName
        is ForgeLikeTarget -> sourceSet.includeJarTaskName
        else -> throw IllegalArgumentException("Unsupported target $this")
    }

val FabricTarget.generateModsJsonTaskName: String
    get() = lowerCamelCaseGradleName("generate", featureName, "ModJson")

val ForgeLikeTarget.generateModsTomlTaskName: String
    get() = lowerCamelCaseGradleName("generate", featureName, "modsToml")

val MinecraftTarget.generateModsManifestTaskName: String
    get() = when (this) {
        is FabricTarget -> generateModsJsonTaskName
        is ForgeLikeTarget -> generateModsTomlTaskName
        else -> throw IllegalArgumentException("Unsupported target $this")
    }

fun String.camelToKebabCase(): String {
    val pattern = "(?<=.)[A-Z]".toRegex()
    return this.replace(pattern, "-$0").lowercase()
}

tasks {
    withType<ProcessResources> {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    shadowJar {
        enabled = false
    }

    val shadowContainersJar by registering(ShadowJar::class) {
        archiveClassifier = ""

        for (task in containerTasks) {
            from(task.map { zipTree(it.archiveFile) })
            manifest.inheritFrom(task.get().manifest)
        }

        manifest {
            attributes(
                "FMLModType" to "GAMELIBRARY"
            )
        }

        append("META-INF/accesstransformer.cfg")
    }

    val shadowSourcesJar by registering(ShadowJar::class) {
        dependsOn(cloche.targets.map { it.generateModsManifestTaskName })

        mergeServiceFiles()
        archiveClassifier.set("sources")
        from(sourceSets.map { it.allSource })

        doFirst {
            manifest {
                from(source.filter { it.name.equals("MANIFEST.MF") }.toList())
            }
        }
    }

    build {
        dependsOn(shadowContainersJar, shadowSourcesJar)
    }

    // https://github.com/terrarium-earth/cloche/issues/115
    val remapFabricMinecraftIntermediary by registering {
        dependsOn(cloche.targets.filterIsInstance<FabricTarget>().flatMap {
            listOf(
                lowerCamelCaseGradleName(
                    "remap",
                    it.name,
                    "commonMinecraft",
                    MinecraftCodevFabricPlugin.INTERMEDIARY_MAPPINGS_NAMESPACE,
                ), lowerCamelCaseGradleName(
                    "remap",
                    it.name,
                    "clientMinecraft",
                    MinecraftCodevFabricPlugin.INTERMEDIARY_MAPPINGS_NAMESPACE,
                )
            )
        })
    }
}