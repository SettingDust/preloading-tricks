@file:Suppress("UnstableApiUsage", "INVISIBLE_REFERENCE")
@file:OptIn(ExperimentalPathApi::class)

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.DeduplicatingResourceTransformer
import earth.terrarium.cloche.INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE
import earth.terrarium.cloche.REMAPPED_ATTRIBUTE
import earth.terrarium.cloche.api.attributes.IncludeTransformationStateAttribute
import earth.terrarium.cloche.api.attributes.MinecraftModLoader
import earth.terrarium.cloche.api.attributes.TargetAttributes
import earth.terrarium.cloche.api.metadata.CommonMetadata
import earth.terrarium.cloche.api.target.*
import earth.terrarium.cloche.target.LazyConfigurableInternal
import earth.terrarium.cloche.util.target
import groovy.lang.Closure
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.core.utils.toPath
import net.msrandom.minecraftcodev.core.utils.zipFileSystem
import net.msrandom.minecraftcodev.runs.MinecraftRunConfiguration
import org.gradle.jvm.tasks.Jar
import java.nio.file.StandardCopyOption
import kotlin.io.path.*

plugins {
    java
    idea
    `maven-publish`

    id("com.palantir.git-version") version "5.0.0"
    id("com.gradleup.shadow") version "9.4.1"
    id("earth.terrarium.cloche") version "0.18.11-dust.3"
}

// region Project Properties

val archive_name: String by rootProject.properties
val id: String by rootProject.properties
val source: String by rootProject.properties

group = "settingdust.preloading_tricks"

val gitVersion: Closure<String> by extra
version = gitVersion()

base { archivesName = archive_name }

// endregion

repositories {
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven")
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    maven("https://repo.nyon.dev/releases") {
        content {
            includeGroup("dev.nyon")
        }
    }

    maven("https://maven.lenni0451.net/snapshots/") {
        content {
            includeGroupAndSubgroups("net.lenni0451")
        }
    }

    maven("https://maven.su5ed.dev/releases") {
        content {
            includeGroupAndSubgroups("dev.su5ed.sinytra")
            includeGroupAndSubgroups("org.sinytra")
        }
    }

    maven("https://maven.sinytra.org/") {
        content {
            includeGroupAndSubgroups("org.sinytra")
        }
    }

    maven("https://raw.githubusercontent.com/settingdust/maven/main/repository/") {
        name = "SettingDust's Maven"
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

// region Attribute Compatibility Rules

class MinecraftVersionCompatibilityRule : AttributeCompatibilityRule<String> {
    override fun execute(details: CompatibilityCheckDetails<String>) {
        details.compatible()
    }
}

class MinecraftModLoaderCompatibilityRule : AttributeCompatibilityRule<MinecraftModLoader> {
    override fun execute(details: CompatibilityCheckDetails<MinecraftModLoader>) {
        if (details.producerValue == MinecraftModLoader.common) {
            details.compatible()
        }
    }
}

dependencies {
    attributesSchema {
        attribute(TargetAttributes.MINECRAFT_VERSION) {
            compatibilityRules.add(MinecraftVersionCompatibilityRule::class)
        }
        attribute(TargetAttributes.MOD_LOADER) {
            compatibilityRules.add(MinecraftModLoaderCompatibilityRule::class)
        }
        attribute(TargetAttributes.CLOCHE_MINECRAFT_VERSION) {
            compatibilityRules.add(MinecraftVersionCompatibilityRule::class)
        }
        attribute(TargetAttributes.CLOCHE_MOD_LOADER) {
            compatibilityRules.add(MinecraftModLoaderCompatibilityRule::class)
        }
    }
}

// endregion

cloche {
    // region Metadata & Mappings

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

    // endregion

    // region Common Targets

    common()

    val commonMain = common("common:common") {
        // mixins.from(file("src/common/common/main/resources/$id.mixins.json"))
        // accessWideners.from(file("src/common/common/main/resources/$id.accessWidener"))

        dependencies {
            compileOnly("org.spongepowered:mixin:0.8.7")
        }
    }

    // endregion

    // region ForgeLike Common Targets
    val commonForgeLike = common("common:forgelike") {
        dependsOn(commonMain)
    }
    val commonModLauncher = common("common:modlauncher") {
        dependsOn(commonMain, commonForgeLike)
    }
    val commonNeoForge = common("common:neoforge") {
        dependsOn(commonMain, commonForgeLike)
    }
    // endregion

    // region Shared Target Defaults

    targets.withType<FabricTarget> {
        loaderVersion = "0.18.6"

        includedClient()

        metadata {

            entrypoint("main") {
                value = "$group.fabric.PreloadingTricksFabric"
            }

            entrypoint("client") {
                value = "$group.fabric.PreloadingTricksFabricClient"
            }


            dependency {
                modId = "fabric-api"
                type = CommonMetadata.Dependency.Type.Required
            }

        }

        dependencies {
            fabricApi(minecraftVersion.map(String::fabricApiVersion))
        }
    }

    targets.withType<ForgeTarget> {
        loaderVersion.set(minecraftVersion.map(String::forgeLoaderVersion))
    }

    targets.withType<NeoforgeTarget> {
        loaderVersion.set(minecraftVersion.map(String::neoForgeLoaderVersion))
    }

    targets.all {
        if (name.startsWith("version:")) {
            disableVersionTemplateTasks()
        }

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
            minecraftVersion.orNull
                ?.let(String::parchmentVersion)
                ?.let(::parchment)
        }
    }
    // endregion

    // region Main Targets - Fabric

    val fabric = fabric {
        dependsOn(commonMain)

        minecraftVersion = "1.20.1"

        dependencies {
            fabricApi("0.92.6")

            catalog.reflect.let {
                api(it)
                include(it)
            }

            catalog.classTransform.let {
                api(it)
                include(it)
            }

            catalog.classTransform.additionalClassProvider.let {
                implementation(it)
                include(it)
            }

            catalog.classTransform.mixinsTranslator.let {
                implementation(it)
                include(it)
            }

            catalog.bytebuddy.agent.let {
                api(it)
                include(it)
            }
        }

        metadata {
            languageAdapters.put(id, "$group.fabric.PreloadingTricksLanguageAdapter")

            dependency {
                modId = "fabricloader"
                version {
                    start = "0.18"
                }
            }
        }
    }

    // endregion

    // region Main Targets - Forge

    val forgeService = forge("forge:service") {
        dependsOn(commonMain, commonModLauncher)

        minecraftVersion = "1.20.1"

        dependencies {
            implementation(catalog.mixinextras.common)

            api(catalog.reflect)

            api(catalog.classTransform)
            implementation(catalog.classTransform.additionalClassProvider)
            implementation(catalog.classTransform.mixinsTranslator)

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

            embedBoot(catalog.lenni0451.commons.unchecked)
            embedBoot(catalog.reflect)
            embedBoot(catalog.classTransform)
            embedBoot(catalog.classTransform.mixinsTranslator)
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

    // endregion

    // region Main Targets - NeoForge

    val neoforgeModlauncher = neoforge("neoforge:modlauncher") {
        dependsOn(commonMain, commonForgeLike, commonModLauncher, commonNeoForge)

        minecraftVersion = "1.21.1"

        metadata {
            modLoader = "lowcodefml"
            loaderVersion {
                start = "0"
            }
        }

        dependencies {
            catalog.reflect.let {
                api(it)
                legacyClasspath(it)
            }
            catalog.classTransform.let {
                api(it) {
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
            catalog.classTransform.mixinsTranslator.let {
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
            embedBoot(catalog.lenni0451.commons.unchecked)
            embedBoot(catalog.reflect)
            embedBoot(catalog.classTransform)
            embedBoot(catalog.classTransform.additionalClassProvider)
            embedBoot(catalog.classTransform.mixinsTranslator)
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
        dependsOn(commonMain, commonForgeLike, commonNeoForge)

        minecraftVersion = "1.21.10"
        loaderVersion = "21.10.64"

        dependencies {
            catalog.reflect.let {
                api(it)
                legacyClasspath(it)
            }
            catalog.classTransform.let {
                api(it) {
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
            catalog.classTransform.mixinsTranslator.let {
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

    // endregion

    // region Version Targets

    // region Fabric Version Targets

    fabric("version:fabric:20.1") {
        minecraftVersion = "1.20.1"

        runs { client() }

        dependencies {
            modRuntimeOnly(target(fabric))
        }
    }

    fabric("version:fabric:21.1") {
        minecraftVersion = "1.21.1"

        runs { client() }

        dependencies {
            modRuntimeOnly(target(fabric))
        }
    }

    // endregion

    // region Forge Version Targets

    forge("version:forge:20.1") {
        minecraftVersion = "1.20.1"

        runs {
            client {
                env("MOD_CLASSES", "")
            }
        }

        dependencies {
            modRuntimeOnly(target(forgeService))
        }
    }

    // endregion

    // region NeoForge Version Targets

    neoforge("version:neoforge:21.1") {
        minecraftVersion = "1.21.1"

        runs {
            client {
                env("MOD_CLASSES", "")
            }
        }

        dependencies {
            legacyClasspath(target(neoforgeModlauncher)) {
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
    }

    neoforge("version:neoforge:21.10") {
        minecraftVersion = "1.21.10"

        runs {
            client {
                env("MOD_CLASSES", "")
            }
        }

        dependencies {
            runtimeOnly(target(neoforgeFancyModLoader))
        }
    }

    // endregion
}

// region Extension Properties

fun String.fabricApiVersion(): String? = when (this) {
    "1.20.1" -> "0.92.7"
    "1.21.1" -> "0.116.10"
    else -> null
}

fun String.parchmentVersion(): String? = when (this) {
    "1.20.1" -> "2023.09.03"
    "1.21.1" -> "2024.11.17"
    else -> null
}

fun String.forgeLoaderVersion(): String? = when (this) {
    "1.20.1" -> "47.4.4"
    else -> null
}

fun String.neoForgeLoaderVersion(): String? = when (this) {
    "1.21.1" -> "21.1.213"
    "1.21.10" -> "21.10.64"
    else -> null
}

fun MinecraftTarget.disableVersionTemplateTasks() {
    tasks {
        named(generateModsManifestTaskName) { enabled = false }
        named(jarTaskName) { enabled = false }
        named(remapJarTaskName) { enabled = false }
        named(includeJarTaskName) { enabled = false }
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

val MinecraftTarget.accessWidenTaskName: String
    get() = lowerCamelCaseGradleName("accessWiden", featureName, "minecraft")

val MinecraftTarget.decompileMinecraftTaskName: String
    get() = lowerCamelCaseGradleName("decompile", featureName, "minecraft")

// endregion

// region Tasks

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

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        val fabricJar = project.tasks.named<Jar>(cloche.targets.getByName("fabric").includeJarTaskName)
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

        transform<DeduplicatingResourceTransformer>()
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

        transform<DeduplicatingResourceTransformer>()
    }

    build {
        dependsOn(shadowContainersJar, shadowSourcesJar)
    }

    afterEvaluate {
        (components["java"] as AdhocComponentWithVariants).apply {
            val testTargets = cloche.targets.filter { it.name.startsWith("version:") }

            testTargets.forEach { target ->
                listOf(
                    "${target.featureName}ApiElements",
                    "${target.featureName}RuntimeElements"
                ).forEach { variantName ->
                    configurations.findByName(variantName)?.let { config ->
                        withVariantsFromConfiguration(config) {
                            skip()
                        }
                    }
                }
            }
        }
    }
}

// endregion

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])

            artifact(tasks.named("shadowSourcesJar")) {
                classifier = "sources"
            }

            pom {
                name = cloche.metadata.modId
                description = cloche.metadata.description
                url = cloche.metadata.sources

                licenses {
                    license {
                        name = cloche.metadata.license
                    }
                }

                developers {
                    developer {
                        name = cloche.metadata.authors.get().first().name
                        email = cloche.metadata.authors.get().first().contact
                    }
                }
            }
        }
    }
}