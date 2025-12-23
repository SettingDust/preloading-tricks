@file:Suppress("UnstableApiUsage", "INVISIBLE_REFERENCE")
@file:OptIn(ExperimentalPathApi::class)

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import earth.terrarium.cloche.INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE
import earth.terrarium.cloche.REMAPPED_ATTRIBUTE
import earth.terrarium.cloche.api.attributes.IncludeTransformationStateAttribute
import earth.terrarium.cloche.api.attributes.TargetAttributes
import earth.terrarium.cloche.api.metadata.CommonMetadata
import earth.terrarium.cloche.api.target.FabricTarget
import earth.terrarium.cloche.api.target.ForgeLikeTarget
import earth.terrarium.cloche.api.target.MinecraftTarget
import earth.terrarium.cloche.target.LazyConfigurableInternal
import groovy.lang.Closure
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.core.utils.toPath
import net.msrandom.minecraftcodev.core.utils.zipFileSystem
import net.msrandom.minecraftcodev.fabric.MinecraftCodevFabricPlugin
import net.msrandom.minecraftcodev.runs.MinecraftRunConfiguration
import org.gradle.jvm.tasks.Jar
import java.nio.file.StandardCopyOption
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyTo
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.name

plugins {
    java
    idea

    id("com.palantir.git-version") version "4.2.0"

    id("com.gradleup.shadow") version "9.2.2"

    id("earth.terrarium.cloche") version "0.16.21-dust"
}

val archive_name: String by rootProject.properties
val id: String by rootProject.properties
val source: String by rootProject.properties

group = "settingdust.preloading_tricks"

val gitVersion: Closure<String> by extra
version = gitVersion()

base { archivesName = archive_name }
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven")
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    exclusiveContent {
        forRepository {
            maven("https://mvn.devos.one/snapshots")
        }
        filter {
            includeGroup("xyz.bluspring")
        }
    }

    maven("https://maven.lenni0451.net/snapshots/") {
        content {
            includeGroupAndSubgroups("net.lenni0451")
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

    val common = common("common:common")

    run fabric@{
        val fabricCommon = common("fabric:common") {
            dependsOn(common)
        }

        fabric("version:fabric:1.20.1") {
            minecraftVersion = "1.20.1"

            runs { client() }

            dependencies {
                fabricApi("0.92.6")

                catalog.asmFabricLoader.let {
                    implementation(it)
                    include(it)
                }
            }
        }

        fabric("version:fabric:1.21") {
            minecraftVersion = "1.21.1"

            runs { client() }

            dependencies {
                fabricApi("0.116.6")

                implementation(catalog.asmFabricLoader)
            }

            tasks {
                named(jarTaskName) {
                    enabled = false
                }

                named(remapJarTaskName) {
                    enabled = false
                }

                named(includeJarTaskName) {
                    enabled = false
                }
            }
        }

        targets.withType<FabricTarget> {
            dependsOn(fabricCommon)

            loaderVersion = "0.18.1"

            includedClient()

            metadata {
                entrypoint("afl:prePrePreLaunch", "$group.fabric.PreloadingTricksLanguageAdapterEntrypoint")
                custom("afl:classtransform", "$id.fabric.classtransform.json")

                dependency {
                    modId = "fabricloader"
                    version {
                        start = "0.18"
                    }
                }

                dependency {
                    modId = "asmfabricloader"
                }
            }
        }
    }

    val commonForgeLike = common("common:forgelike") {
        dependsOn(common)
    }
    val commonModLauncher = common("common:forgelike:modlauncher") {
        dependsOn(common, commonForgeLike)
    }
    val commonNeoForge = common("common:forgelike:neoforge") {
        dependsOn(common, commonForgeLike)
    }

    run forge@{
        val forge = forge("forge:service") {
            minecraftVersion = "1.20.1"
            loaderVersion = "47.4.4"

            metadata {
                modLoader = "lowcodefml"
                loaderVersion {
                    start = "0"
                }
            }

            dependsOn(common, commonForgeLike, commonModLauncher)

            dependencies {
                implementation(catalog.reflect)

                implementation(catalog.classTransform)
                implementation(catalog.classTransform.additionalClassProvider)

                implementation(catalog.bytebuddy.agent)
            }

            val noNewerJavaAttribute = Attribute.of("noNewerJava", Boolean::class.javaObjectType)

            abstract class RemoveNewerJavaTransform : TransformAction<TransformParameters.None> {
                @get:InputArtifact
                abstract val inputArtifact: Provider<FileSystemLocation>

                override fun transform(outputs: TransformOutputs) {
                    val input = inputArtifact.get().toPath()

                    val newerJavaClasses = zipFileSystem(input).use {
                        it.getPath("META-INF/versions/24").exists()
                    }

                    if (!newerJavaClasses) {
                        outputs.file(input)
                        return
                    }

                    val output = outputs.file(input.name.replace(".jar", "-noNewerJava.jar")).toPath()

                    input.copyTo(output, StandardCopyOption.COPY_ATTRIBUTES)

                    zipFileSystem(output).use { fs ->
                        fs.getPath("META-INF/versions/24").deleteRecursively()
                    }
                }
            }

            val embedBoot by configurations.register(lowerCamelCaseGradleName(featureName, "embedBoot")) {
                isTransitive = false

                attributes
                    .attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                    .attribute(REMAPPED_ATTRIBUTE, false)
                    .attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, true)
                    .attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
                    .attribute(noNewerJavaAttribute, true)
            }

            project.dependencies {
                attributesSchema {
                    attribute(noNewerJavaAttribute)
                }

                artifactTypes.named(ArtifactTypeDefinition.JAR_TYPE) {
                    attributes.attribute(noNewerJavaAttribute, false)
                }

                registerTransform(RemoveNewerJavaTransform::class) {
                    from.attribute(noNewerJavaAttribute, false)
                    to.attribute(noNewerJavaAttribute, true)
                }

                embedBoot(catalog.reflect)
                embedBoot(catalog.classTransform)
                embedBoot(catalog.classTransform.additionalClassProvider)
                embedBoot(catalog.bytebuddy.agent)
            }

            tasks {
                named(generateModsTomlTaskName) {
                    enabled = false
                }

                named<Jar>(jarTaskName) {
                    from(embedBoot) {
                        into("libs/boot")
                    }
                }
            }
        }

        forge("version:forge:1.20.1") {
            minecraftVersion = "1.20.1"
            loaderVersion = "47.4.4"

            runs {
                client {
                    env("MOD_CLASSES", "")
                }
            }

            dependencies {
                implementation(project(":")) {
                    capabilities {
                        requireFeature(forge.capabilitySuffix!!)
                    }
                }
            }

            tasks {
                named(jarTaskName) {
                    enabled = false
                }

                named(remapJarTaskName) {
                    enabled = false
                }

                named(includeJarTaskName) {
                    enabled = false
                }
            }
        }
    }

    run neoforge@{
        val neoforgeModlauncherLegacy = neoforge("neoforge:modlauncher:legacy") {
            dependsOn(common, commonForgeLike, commonModLauncher)

            minecraftVersion = "1.20.6"
            loaderVersion = "20.6.139"

            dependencies {
                catalog.reflect.let {
                    implementation(it)
                }
                catalog.classTransform.let {
                    implementation(it) {
                        exclude(group = "org.ow2.asm")
                    }
                }
                catalog.classTransform.additionalClassProvider.let {
                    implementation(it) {
                        exclude(group = "com.google.guava")
                        exclude(group = "org.ow2.asm")
                    }
                }
                catalog.bytebuddy.agent.let {
                    implementation(it)
                }
            }

            tasks {
                named(generateModsTomlTaskName) {
                    enabled = false
                }
            }
        }

        val neoforgeModlauncher = neoforge("neoforge:modlauncher") {
            dependsOn(common, commonForgeLike, commonModLauncher, commonNeoForge)

            minecraftVersion = "1.21.1"
            loaderVersion = "21.1.213"

            metadata {
                modLoader = "lowcodefml"
                loaderVersion {
                    start = "0"
                }
            }

            dependencies {
                catalog.reflect.let {
                    implementation(it)
                    legacyClasspath(it)
                }
                catalog.classTransform.let {
                    implementation(it) {
                        exclude(group = "org.ow2.asm")
                    }
                    legacyClasspath(it)
                }
                catalog.classTransform.additionalClassProvider.let {
                    implementation(it) {
                        exclude(group = "com.google.guava")
                        exclude(group = "org.ow2.asm")
                    }
                    legacyClasspath(it)
                }
                catalog.bytebuddy.agent.let {
                    implementation(it)
                    legacyClasspath(it)
                }
            }

            val embedBoot by configurations.register(lowerCamelCaseGradleName(featureName, "embedBoot")) {
                isTransitive = false

                attributes
                    .attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                    .attribute(REMAPPED_ATTRIBUTE, false)
                    .attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, true)
                    .attribute(
                        IncludeTransformationStateAttribute.ATTRIBUTE,
                        IncludeTransformationStateAttribute.None
                    )
            }

            project.dependencies {
                embedBoot(catalog.reflect)
                embedBoot(catalog.classTransform)
                embedBoot(catalog.classTransform.additionalClassProvider)
                embedBoot(catalog.bytebuddy.agent)
            }

            tasks {
                named(generateModsTomlTaskName) {
                    enabled = false
                }

                named<Jar>(jarTaskName) {
                    from(embedBoot) {
                        into("libs/boot")
                    }
                }
            }
        }

        val neoforgeFancyModLoader = neoforge("neoforge:fancy-mod-loader") {
            dependsOn(common, commonForgeLike, commonNeoForge)

            minecraftVersion = "1.21.10"
            loaderVersion = "21.10.64"

            dependencies {
                catalog.reflect.let {
                    implementation(it)
                    legacyClasspath(it)
                }
                catalog.classTransform.let {
                    implementation(it) {
                        exclude(group = "org.ow2.asm")
                    }
                    legacyClasspath(it)
                }
                catalog.classTransform.additionalClassProvider.let {
                    implementation(it) {
                        exclude(group = "com.google.guava")
                        exclude(group = "org.ow2.asm")
                    }
                    legacyClasspath(it)
                }
                catalog.bytebuddy.agent.let {
                    implementation(it)
                    legacyClasspath(it)
                }
            }
        }

        neoforge("version:neoforge:1.20.6") {
            minecraftVersion = "1.20.6"
            loaderVersion = "20.6.139"

            runs {
                client {
                    env("MOD_CLASSES", "")
                }
            }

            dependencies {
                legacyClasspath(project(":")) {
                    capabilities {
                        requireFeature(neoforgeModlauncherLegacy.capabilitySuffix!!)
                    }

                    attributes {
                        attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                        attribute(REMAPPED_ATTRIBUTE, false)
                        attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, true)
                        attribute(
                            IncludeTransformationStateAttribute.ATTRIBUTE,
                            IncludeTransformationStateAttribute.None
                        )
                    }

                    isTransitive = false
                }
            }

            tasks {
                named(generateModsTomlTaskName) {
                    enabled = false
                }

                named(jarTaskName) {
                    enabled = false
                }

                named(remapJarTaskName) {
                    enabled = false
                }

                named(includeJarTaskName) {
                    enabled = false
                }
            }
        }

        neoforge("version:neoforge:1.21.1") {
            minecraftVersion = "1.21.1"
            loaderVersion = "21.1.213"

            runs {
                client {
                    env("MOD_CLASSES", "")
                }
            }

            dependencies {
                legacyClasspath(project(":")) {
                    capabilities {
                        requireFeature(neoforgeModlauncher.capabilitySuffix!!)
                    }

                    attributes {
                        attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                        attribute(REMAPPED_ATTRIBUTE, false)
                        attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, true)
                        attribute(
                            IncludeTransformationStateAttribute.ATTRIBUTE,
                            IncludeTransformationStateAttribute.None
                        )
                    }

                    isTransitive = false
                }
            }

            tasks {
                named(generateModsTomlTaskName) {
                    enabled = false
                }

                named(jarTaskName) {
                    enabled = false
                }

                named(remapJarTaskName) {
                    enabled = false
                }

                named(includeJarTaskName) {
                    enabled = false
                }
            }
        }

        neoforge("version:neoforge:1.21.10") {
            minecraftVersion = "1.21.10"
            loaderVersion = "21.10.38-beta"

            runs {
                client {
                    env("MOD_CLASSES", "")
                }
            }

            dependencies {
                implementation(project(":")) {
                    capabilities {
                        requireFeature(neoforgeFancyModLoader.capabilitySuffix!!)
                    }
                }
            }

            tasks {
                named(generateModsTomlTaskName) {
                    enabled = false
                }

                named(jarTaskName) {
                    enabled = false
                }

                named(remapJarTaskName) {
                    enabled = false
                }

                named(includeJarTaskName) {
                    enabled = false
                }
            }
        }
    }

    targets.all {
        runs {
            (client as LazyConfigurableInternal<MinecraftRunConfiguration>).onConfigured {
                it.jvmArguments(
                    "-Dmixin.debug.verbose=true",
                    "-Dmixin.debug.export=true",
                    "-Dclasstransform.dumpClasses=true"
                )
            }
        }

        mappings {
            minecraftVersion.map {
                when (it) {
                    "1.20.1" -> "2023.09.03"
                    "1.21.1" -> "2024.11.17"
                    "1.21.10" -> "2025.10.12"
                    else -> null
                }
            }.orNull?.let {
                parchment(it)
            }
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

val MinecraftTarget.jarTaskName: String
    get() = lowerCamelCaseGradleName(featureName, "jar")

val MinecraftTarget.remapJarTaskName: String
    get() = lowerCamelCaseGradleName(featureName, "remapJar")

tasks {
    withType<ProcessResources> {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.WARN

        manifest {
            attributes("Implementation-Version" to project.version)
        }
    }

    shadowJar {
        enabled = false
    }

    val shadowContainersJar by registering(ShadowJar::class) {
        archiveClassifier = ""

        val fabricJar = project.tasks.named<Jar>(cloche.targets.getByName("version:fabric:1.20.1").includeJarTaskName)
        from(fabricJar.map { zipTree(it.archiveFile) })
        manifest.from(fabricJar.get().manifest)

        val forgeServiceJar = project.tasks.named<Jar>(cloche.targets.getByName("forge:service").includeJarTaskName)
        from(forgeServiceJar.map { zipTree(it.archiveFile) })
        manifest.from(forgeServiceJar.get().manifest)

        val neoforgeModlauncherJar =
            project.tasks.named<Jar>(cloche.targets.getByName("neoforge:modlauncher").includeJarTaskName)
        from(neoforgeModlauncherJar.map { zipTree(it.archiveFile) }) {
            include("settingdust/preloading_tricks/forgelike/neoforge/**/*")
            include("settingdust/preloading_tricks/neoforge/**/*")
            include("META-INF/services/*")
            include("$id.neoforge.modlauncher.classtransform.json")
        }

        val neoforgeFancyModLoaderJar =
            project.tasks.named<Jar>(cloche.targets.getByName("neoforge:fancy-mod-loader").includeJarTaskName)
        from(neoforgeFancyModLoaderJar.map { zipTree(it.archiveFile) }) {
            include("settingdust/preloading_tricks/forgelike/neoforge/**/*")
            include("settingdust/preloading_tricks/neoforge/**/*")
            include("META-INF/services/*")
            include("$id.neoforge.fml.classtransform.json")
        }

        append("META-INF/accesstransformer.cfg")

        mergeServiceFiles()
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

    for (target in cloche.targets.filterIsInstance<FabricTarget>()) {
        named(lowerCamelCaseGradleName("accessWiden", target.featureName, "commonMinecraft")) {
            dependsOn(
                lowerCamelCaseGradleName(
                    "remap",
                    target.featureName,
                    "commonMinecraft",
                    MinecraftCodevFabricPlugin.INTERMEDIARY_MAPPINGS_NAMESPACE,
                ), lowerCamelCaseGradleName(
                    "remap",
                    target.featureName,
                    "clientMinecraft",
                    MinecraftCodevFabricPlugin.INTERMEDIARY_MAPPINGS_NAMESPACE,
                ), lowerCamelCaseGradleName("generate", target.featureName, "MappingsArtifact")
            )
        }

        named(lowerCamelCaseGradleName("accessWiden", target.featureName, "Minecraft")) {
            dependsOn(
                lowerCamelCaseGradleName(
                    "remap",
                    target.featureName,
                    "clientMinecraft",
                    MinecraftCodevFabricPlugin.INTERMEDIARY_MAPPINGS_NAMESPACE,
                ), lowerCamelCaseGradleName("generate", target.featureName, "MappingsArtifact")
            )
        }
    }
}