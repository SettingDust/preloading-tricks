@file:Suppress("UnstableApiUsage", "INVISIBLE_REFERENCE")

import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin.Companion.shadowRuntimeElements
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PreserveFirstFoundResourceTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ResourceTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import earth.terrarium.cloche.ClocheExtension
import earth.terrarium.cloche.INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE
import earth.terrarium.cloche.REMAPPED_ATTRIBUTE
import earth.terrarium.cloche.api.attributes.IncludeTransformationStateAttribute
import earth.terrarium.cloche.api.attributes.MinecraftModLoader
import earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute
import earth.terrarium.cloche.api.attributes.TargetAttributes
import earth.terrarium.cloche.api.metadata.CommonMetadata
import earth.terrarium.cloche.api.metadata.FabricMetadata
import earth.terrarium.cloche.api.target.FabricTarget
import earth.terrarium.cloche.api.target.ForgeLikeTarget
import earth.terrarium.cloche.api.target.ForgeTarget
import earth.terrarium.cloche.api.target.MinecraftTarget
import earth.terrarium.cloche.api.target.NeoforgeTarget
import earth.terrarium.cloche.api.target.compilation.ClocheDependencyHandler
import earth.terrarium.cloche.target.LazyConfigurableInternal
import earth.terrarium.cloche.tasks.GenerateFabricModJson
import earth.terrarium.cloche.util.fromJars
import earth.terrarium.cloche.util.target
import groovy.lang.Closure
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.fabric.task.JarInJar
import net.msrandom.minecraftcodev.forge.task.JarJar
import net.msrandom.minecraftcodev.includes.IncludesJar
import net.msrandom.minecraftcodev.runs.MinecraftRunConfiguration
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.support.serviceOf
import java.nio.charset.StandardCharsets

plugins {
    java
    idea
    id("com.palantir.git-version") version "5.0.0"
    id("com.gradleup.shadow") version "9.4.1"
    id("earth.terrarium.cloche") version "0.18.11-dust.17"
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

// region Repositories

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

// endregion

// region Container DSL

private fun MinecraftModLoader.containerFeatureName(): String =
    lowerCamelCaseGradleName("container", toString().lowercase())

class ContainerScope(
    private val project: Project,
    val loader: MinecraftModLoader,
) {
    val featureName: String = loader.containerFeatureName()
    val capabilitySuffix: String = loader.toString().lowercase()

    val intermediateOutputsDirectory = project.layout.buildDirectory.dir("libs/intermediates")

    private val includeConfigurationProvider =
        project.configurations.register(lowerCamelCaseGradleName(featureName, "include")) {
            isCanBeResolved = true
            isCanBeConsumed = false
            isTransitive = false

            attributes {
                attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                attribute(REMAPPED_ATTRIBUTE, false)
                attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
                attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
            }
        }

    private val includeDevConfigurationProvider =
        project.configurations.register(lowerCamelCaseGradleName(featureName, "includeDev")) {
            isCanBeResolved = true
            isCanBeConsumed = false
            isTransitive = false

            attributes {
                attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                attribute(REMAPPED_ATTRIBUTE, true)
                attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
                attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
                attribute(RemapNamespaceAttribute.ATTRIBUTE, RemapNamespaceAttribute.INITIAL)
            }
        }

    private val embedConfigurations = mutableMapOf<String, NamedDomainObjectProvider<Configuration>>()

    val jarTask = project.tasks.register<ShadowJar>(lowerCamelCaseGradleName(featureName, "jar")) {
        group = "build"
        archiveClassifier = loader.toString().lowercase()
        destinationDirectory = intermediateOutputsDirectory
    }

    val includeJarTask: TaskProvider<out IncludesJar> =
        createPackageTask("includeJar", includeConfigurationProvider)
    val includeDevJarTask: TaskProvider<out IncludesJar> =
        createPackageTask(
            "includesDevJar",
            includeDevConfigurationProvider,
            archiveClassifier = "${loader.toString().lowercase()}-dev",
            toIntermediateOutputs = true,
        )

    init {
        project.tasks.build {
            dependsOn(includeJarTask, includeDevJarTask)
        }

        val containerCapability = "${project.group}:${project.name}-$capabilitySuffix:${project.version}"

        project.configurations.register(lowerCamelCaseGradleName(featureName, "runtimeElements")) {
            isCanBeResolved = false
            isCanBeConsumed = true
            attributes {
                applyRuntimeVariantAttributes(remapped = false)
            }
            outgoing.artifact(includeJarTask)
            outgoing.capability(containerCapability)
        }

        project.configurations.register(lowerCamelCaseGradleName(featureName, "devRuntimeElements")) {
            isCanBeResolved = false
            isCanBeConsumed = true
            attributes {
                applyRuntimeVariantAttributes(remapped = true)
            }
            outgoing.artifact(includeDevJarTask)
            outgoing.capability(containerCapability)
        }
    }

    private fun AttributeContainer.applyRuntimeVariantAttributes(remapped: Boolean) {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
        attribute(TargetAttributes.MOD_LOADER, loader)
        attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
        attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
        attribute(REMAPPED_ATTRIBUTE, remapped)
        if (remapped) {
            attribute(RemapNamespaceAttribute.ATTRIBUTE, RemapNamespaceAttribute.INITIAL)
        }
    }

    private fun createPackageTask(
        name: String,
        configuration: NamedDomainObjectProvider<Configuration>,
        archiveClassifier: String = loader.toString().lowercase(),
        toIntermediateOutputs: Boolean = false,
    ): TaskProvider<out IncludesJar> = when (loader) {
        MinecraftModLoader.fabric -> project.tasks.register<JarInJar>(lowerCamelCaseGradleName(featureName, name)) {
            group = "build"
            this.archiveClassifier = archiveClassifier
            if (toIntermediateOutputs) {
                destinationDirectory = intermediateOutputsDirectory
            }
            input = jarTask.flatMap { it.archiveFile }
            manifest.fromJars(serviceOf(), input)
            fromResolutionResults(configuration)
        }

        else -> project.tasks.register<JarJar>(lowerCamelCaseGradleName(featureName, name)) {
            group = "build"
            this.archiveClassifier = archiveClassifier
            if (toIntermediateOutputs) {
                destinationDirectory = intermediateOutputsDirectory
            }
            input = jarTask.flatMap { it.archiveFile }
            manifest.fromJars(serviceOf(), input)
            fromResolutionResults(configuration)
        }
    }

    private fun ModuleDependency.withIncludeAttributes() {
        attributes {
            attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
            attribute(REMAPPED_ATTRIBUTE, false)
            attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
            attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
        }
    }

    private fun ModuleDependency.withIncludeDevAttributes() {
        attributes {
            attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
            attribute(REMAPPED_ATTRIBUTE, true)
            attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
            attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
            attribute(RemapNamespaceAttribute.ATTRIBUTE, RemapNamespaceAttribute.INITIAL)
        }
    }

    private fun embedConfigurationName(name: String): String =
        if (name.isBlank()) {
            lowerCamelCaseGradleName(featureName, "embed")
        } else {
            lowerCamelCaseGradleName(featureName, "embed", name)
        }

    private fun Configuration.applyDefaultEmbedAttributes() {
        attributes {
            attribute(
                LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                project.objects.named(LibraryElements.CLASSES_AND_RESOURCES)
            )
            attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
        }
    }

    private fun Configuration.applyTransformedJarAttributes() {
        attributes {
            attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
            attribute(REMAPPED_ATTRIBUTE, false)
            attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, true)
            attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
        }
    }

    inner class DependenciesScope(private val handler: DependencyHandler) : DependencyHandler by handler {
        private fun addTo(
            configuration: NamedDomainObjectProvider<Configuration>,
            dependencyNotation: Any,
            configure: ModuleDependency.() -> Unit = {},
        ): Dependency? {
            val dependency = handler.add(configuration.get().name, dependencyNotation)
            if (dependency is ModuleDependency) {
                dependency.configure()
            }
            return dependency
        }

        fun include(dependencyNotation: Any, configure: ModuleDependency.() -> Unit = {}): Dependency? =
            addTo(includeConfigurationProvider, dependencyNotation, configure)

        fun includeDev(dependencyNotation: Any, configure: ModuleDependency.() -> Unit = {}): Dependency? =
            addTo(includeDevConfigurationProvider, dependencyNotation, configure)

        fun embed(dependencyNotation: Any, configure: ModuleDependency.() -> Unit = {}): Dependency? =
            embed("", dependencyNotation, configure)

        fun embed(name: String, dependencyNotation: Any, configure: ModuleDependency.() -> Unit = {}): Dependency? {
            val configuration = embedConfigurations[name]
                ?: throw IllegalArgumentException("embed('$name') is not registered for $featureName")
            return addTo(configuration, dependencyNotation, configure)
        }

        fun includeTarget(target: MinecraftTarget) {
            includeJarTask.configure {
                dependsOn(target.includeJarTaskName)
            }
            includeDevJarTask.configure {
                dependsOn(target.jarTaskName)
            }

            include(target(target)) {
                withIncludeAttributes()
            }
            includeDev(target(target)) {
                withIncludeDevAttributes()
            }
        }

        override fun variantOf(
            dependencyProviderConvertible: ProviderConvertible<MinimalExternalModuleDependency>,
            variantSpec: Action<in ExternalModuleDependencyVariantSpec>
        ): Provider<MinimalExternalModuleDependency> {
            return handler.variantOf(dependencyProviderConvertible, variantSpec)
        }

        override fun platform(dependencyProvider: Provider<MinimalExternalModuleDependency>): Provider<MinimalExternalModuleDependency> {
            return handler.platform(dependencyProvider)
        }

        override fun platform(dependencyProviderConvertible: ProviderConvertible<MinimalExternalModuleDependency>): Provider<MinimalExternalModuleDependency> {
            return handler.platform(dependencyProviderConvertible)
        }

        override fun enforcedPlatform(dependencyProviderConvertible: ProviderConvertible<MinimalExternalModuleDependency>): Provider<MinimalExternalModuleDependency> {
            return handler.enforcedPlatform(dependencyProviderConvertible)
        }

        override fun testFixtures(dependencyProvider: Provider<MinimalExternalModuleDependency>): Provider<MinimalExternalModuleDependency> {
            return handler.testFixtures(dependencyProvider)
        }

        override fun testFixtures(dependencyProviderConvertible: ProviderConvertible<MinimalExternalModuleDependency>): Provider<MinimalExternalModuleDependency> {
            return handler.testFixtures(dependencyProviderConvertible)
        }
    }

    fun embed(
        name: String = "",
        configureConfiguration: Configuration.() -> Unit = { applyDefaultEmbedAttributes() },
        configure: CopySpec.() -> Unit = {},
    ) {
        require(name !in embedConfigurations) { "embed('$name') is already registered for $featureName" }

        val configuration = project.configurations.register(embedConfigurationName(name)) {
            isCanBeResolved = true
            isTransitive = false
            configureConfiguration()
        }
        embedConfigurations[name] = configuration

        jarTask.configure {
            from(configuration) {
                configure()
            }
        }
    }

    fun dependencies(block: DependenciesScope.() -> Unit) {
        DependenciesScope(project.dependencies).block()
    }

    fun jar(block: ShadowJar.() -> Unit) {
        jarTask.configure(block)
    }
}

fun ClocheExtension.container(
    loader: MinecraftModLoader,
    block: ContainerScope.() -> Unit,
): ContainerScope = ContainerScope(project, loader).apply(block)

fun ClocheDependencyHandler.container(container: ContainerScope): Dependency =
    project.dependencies.project(":").apply {
        capabilities {
            requireFeature(container.capabilitySuffix)
        }

        attributes {
            attribute(REMAPPED_ATTRIBUTE, true)
            attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
        }
    }

// endregion

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

    val common201 = common("common:20.1") {
        dependsOn(commonMain)
        // mixins.from("src/common/20.1/main/resources/$id.20_1.mixins.json")
    }
    val common211 = common("common:21.1") {
        dependsOn(commonMain)
        // mixins.from("src/common/21.1/main/resources/$id.21_1.mixins.json")
    }
    val common261 = common("common:26.1") {
        dependsOn(commonMain)
        // mixins.from("src/common/26.1/main/resources/$id.26_1.mixins.json")
    }

    // endregion

    // region Game Targets
    val game = common("game") {
        // mixins.from(files("src/game/main/resources/$id.game.mixins.json"))
        // accessWideners.from(file("src/game/main/resources/$id.game.accessWidener"))

        project.dependencies {
            val implementation = lowerCamelCaseGradleName(name, JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)

            implementation(target(commonMain))
        }
    }

    val game201 = common("game:20.1") {
        dependsOn(game)
    }
    val game211 = common("game:21.1") {
        dependsOn(game)
    }
    val game261 = common("game:26.1") {
        dependsOn(game)
    }
    // endregion

    // region Shared Target Defaults

    targets.withType<FabricTarget> {
        loaderVersion = "0.19.2"

        includedClient()

        dependencies {
            fabricApi(minecraftVersion.map(String::fabricApiVersion))
        }

        if (isVersionTarget()) return@withType

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
    }

    targets.withType<ForgeTarget> {
        loaderVersion.set(minecraftVersion.map(String::forgeLoaderVersion))
    }

    targets.withType<NeoforgeTarget> {
        loaderVersion.set(minecraftVersion.map(String::neoForgeLoaderVersion))

        if (isVersionTarget()) return@withType

        metadata {
            modLoader = "javafml"
            loaderVersion {
                start = "4"
            }

            dependency {
                modId = "preloading_tricks"
                type = CommonMetadata.Dependency.Type.Recommended
            }
        }
    }

    targets.all {
        if (isVersionTarget()) {
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

    val fabricCommon = common("fabric:common") {
        dependsOn(commonMain)
        // mixins.from(file("src/fabric/common/main/resources/$id.fabric.mixins.json"))
    }

    val fabric201 = fabric("fabric:20.1") {
        dependsOn(common201, game201, fabricCommon)
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

    }

    val fabric211 = fabric("fabric:21.1") {
        dependsOn(common211, game211, fabricCommon)
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

    }

    val fabric261 = fabric("fabric:26.1") {
        dependsOn(common261, game261, fabricCommon)
        minecraftVersion = "26.1.2"

        metadata {
            dependency {
                modId = "minecraft"
                type = CommonMetadata.Dependency.Type.Required
                version {
                    start = "26.1"
                }
            }
        }
    }

    // endregion

    // region Main Targets - Forge

    val forgeService = forge("forge:service") {
        dependsOn(common201)

        minecraftVersion = "1.20.1"

        tasks {
            named(generateModsTomlTaskName) { enabled = false }
        }
    }
    val forgeGame = forge("forge:game") {
        dependsOn(game201)
        minecraftVersion = "1.20.1"

        metadata {
            modLoader = "javafml"
            loaderVersion {
                start = "47"
            }
            dependency {
                modId = "minecraft"
                type = CommonMetadata.Dependency.Type.Required
                version {
                    start = "1.20.1"
                    end = "1.21"
                }
            }

            dependency {
                modId = "preloading_tricks"
                type = CommonMetadata.Dependency.Type.Recommended
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
            implementation(target(forgeService))
        }

        tasks {
            named<Jar>(lowerCamelCaseGradleName(featureName, "jar")) {
                manifest {
                    attributes(
                        "ForgeVariant" to "LexForge"
                    )
                }
            }
        }
    }

    // endregion

    // region Main Targets - NeoForge

    val neoforgeService = neoforge("neoforge:service") {
        dependsOn(common211)

        minecraftVersion = "1.21.1"

        tasks {
            named(generateModsTomlTaskName) { enabled = false }
        }
    }

    val neoforgeService261 = neoforge("neoforge:service:26.1") {
        dependsOn(common261)

        minecraftVersion = "26.1.2"

        tasks {
            named(generateModsTomlTaskName) { enabled = false }
        }
    }

    val neoforgeGameCommon = common("neoforge:game:common") {
        dependsOn(commonMain)

        mixins.from(file("src/neoforge/game/common/main/resources/$id.neoforge.mixins.json"))
    }

    val neoforgeGame211 = neoforge("neoforge:game:21.1") {
        dependsOn(game211, neoforgeGameCommon)
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
            implementation(target(neoforgeService))
        }

        tasks {
            named<Jar>(lowerCamelCaseGradleName(featureName, "jar")) {
                manifest {
                    attributes(
                        "ForgeVariant" to "NeoForge"
                    )
                }
            }
        }
    }

    val neoforgeGame261 = neoforge("neoforge:game:26.1") {
        dependsOn(game261, neoforgeGameCommon)
        minecraftVersion = "26.1.2"

        metadata {
            dependency {
                modId = "minecraft"
                type = CommonMetadata.Dependency.Type.Required
                version {
                    start = "26.1"
                }
            }
        }

        dependencies {
            implementation(target(neoforgeService261))
        }

        tasks {
            named<Jar>(lowerCamelCaseGradleName(featureName, "jar")) {
                manifest {
                    attributes(
                        "ForgeVariant" to "NeoForge"
                    )
                }
            }
        }
    }

    // endregion

    // region Containers

    // region Fabric Container

    val fabricContainer = container(loader = MinecraftModLoader.fabric) {
        val metadataDirectory = project.layout.buildDirectory.dir("generated")
            .map { it.dir("metadata").dir(featureName) }
        val generateModJson =
            tasks.register<GenerateFabricModJson>(lowerCamelCaseGradleName(featureName, "generateModJson")) {
                modId = "${id}_container"
                metadata = objects.newInstance(FabricMetadata::class.java, fabric201).apply {
                    license.value(cloche.metadata.license)
                    dependencies.value(cloche.metadata.dependencies)
                }
                loaderDependencyVersion = "0.18"
                output.set(metadataDirectory.map { it.file("fabric.mod.json") })
            }

        dependencies {
            includeTarget(fabric261)
            includeTarget(fabric211)
            includeTarget(fabric201)
        }

        jar {
            dependsOn(generateModJson)
            from(metadataDirectory)
        }
    }

    // endregion

    // region Forge Container

    val forgeContainer = container(loader = MinecraftModLoader.forge) {
        embed()
        dependencies {
            includeTarget(forgeGame)
            embed(target(forgeService))
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

    val neoforgeContainer = container(loader = MinecraftModLoader.neoforge) {
        embed()

        val neoforgeMergedJar = tasks.register<ShadowJar>(lowerCamelCaseGradleName(featureName, "mergedJar")) {
            group = "build"
            archiveClassifier = "${loader.toString().lowercase()}-merged"
            destinationDirectory = intermediateOutputsDirectory
            configurations = emptyList()

            for (target in listOf(neoforgeGame211, neoforgeGame261)) {
                val output = tasks.named<Jar>(target.includeJarTaskName).flatMap { it.archiveFile }
                from(project.zipTree(output))

                manifest.fromJars(serviceOf(), output)
            }

            mergeServiceFiles()
            append("META-INF/accesstransformer.cfg")
            transform<PreserveFirstFoundResourceTransformer>()
            filesMatching("**/*.class") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
        }

        val neoforgeMergedDevJar = tasks.register<ShadowJar>(lowerCamelCaseGradleName(featureName, "mergedDevJar")) {
            group = "build"
            archiveClassifier = "${loader.toString().lowercase()}-merged-dev"
            destinationDirectory = intermediateOutputsDirectory
            configurations = emptyList()

            for (target in listOf(neoforgeGame211, neoforgeGame261)) {
                val output = tasks.named<Jar>(target.jarTaskName).flatMap { it.archiveFile }
                from(project.zipTree(output))

                manifest.fromJars(serviceOf(), output)
            }

            mergeServiceFiles()
            append("META-INF/accesstransformer.cfg")
            transform<PreserveFirstFoundResourceTransformer>()
            filesMatching("**/*.class") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
        }

        val mergedRuntimeElements =
            configurations.register(lowerCamelCaseGradleName(featureName, "mergedRuntimeElements")) {
                isCanBeResolved = false
                isCanBeConsumed = true

                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
                    attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                    attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
                    attribute(TargetAttributes.MOD_LOADER, loader)
                    attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
                    attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
                    attribute(REMAPPED_ATTRIBUTE, false)
                }

                outgoing.artifact(neoforgeMergedJar)
            }

        val mergedDevRuntimeElements =
            configurations.register(lowerCamelCaseGradleName(featureName, "mergedDevRuntimeElements")) {
                isCanBeResolved = false
                isCanBeConsumed = true

                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
                    attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                    attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
                    attribute(TargetAttributes.MOD_LOADER, loader)
                    attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
                    attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
                    attribute(REMAPPED_ATTRIBUTE, true)
                    attribute(RemapNamespaceAttribute.ATTRIBUTE, RemapNamespaceAttribute.INITIAL)
                }

                outgoing.artifact(neoforgeMergedDevJar)
            }

        dependencies {
            include(project(":", mergedRuntimeElements.name))
            includeDev(project(":", mergedDevRuntimeElements.name))
            embed(target(neoforgeService))
            embed(target(neoforgeService261))
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
        minecraftVersion = "1.20.1"

        runs { client() }

        dependencies {
            modRuntimeOnly(skipIncludeTransformation(project(":"))) {
                isTransitive = false
            }
        }
    }

    fabric("version:fabric:21.1") {
        minecraftVersion = "1.21.1"

        runs { client() }

        dependencies {
            modRuntimeOnly(skipIncludeTransformation(project(":"))) {
                isTransitive = false
            }
        }
    }

    fabric("version:fabric:26.1") {
        minecraftVersion = "26.1.2"

        runs { client() }

        dependencies {
            modRuntimeOnly(skipIncludeTransformation(project(":"))) {
                isTransitive = false
            }
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

        configurations.named(lowerCamelCaseGradleName(featureName, "legacyClasspath")) {
            exclude("org.jetbrains.kotlin")
        }

        dependencies {
            modRuntimeOnly(project(":")) {
                isTransitive = false
            }

            legacyClasspath(catalog.preloadingTricks) {
                isTransitive = false
            }

            legacyClasspath(catalog.klf.mc20.forge)
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
            modRuntimeOnly(project(":")) {
                isTransitive = false
            }

            legacyClasspath(catalog.preloadingTricks) {
                isTransitive = false
            }

            legacyClasspath(catalog.klf.mc21.neoforge)
        }
    }

    neoforge("version:neoforge:26.1") {
        minecraftVersion = "26.1.2"

        runs {
            client {
                env("MOD_CLASSES", "")
            }
        }

        dependencies {
            modRuntimeOnly(project(":")) {
                isTransitive = false
            }

            legacyClasspath(catalog.preloadingTricks) {
                isTransitive = false
            }

            legacyClasspath(catalog.klf.mc26.neoforge)
        }
    }

    // endregion

    // endregion

    // region Final Jar
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
                    jars?.forEach { collected.add(it) }
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

        val shadowMergedDevJar by registering(ShadowJar::class) {
            archiveClassifier = "dev"
            configurations = emptyList()

            for (container in listOf(fabricContainer, forgeContainer, neoforgeContainer)) {
                val output = container.includeDevJarTask.flatMap { it.archiveFile }
                from(project.zipTree(output))

                manifest.fromJars(serviceOf(), output)
            }

            mergeServiceFiles()
            append("META-INF/accesstransformer.cfg")

            transform<ForgeMetadataTransformer>()
            transform<PreserveFirstFoundResourceTransformer>()
            filesMatching("**/*.class") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
        }

        val shadowMergedJar by registering(ShadowJar::class) {
            archiveClassifier = ""
            configurations = emptyList()

            for (container in listOf(fabricContainer, forgeContainer, neoforgeContainer)) {
                val output = container.includeJarTask.flatMap { it.archiveFile }
                from(project.zipTree(output))

                manifest.fromJars(serviceOf(), output)
            }

            mergeServiceFiles()
            append("META-INF/accesstransformer.cfg")

            transform<ForgeMetadataTransformer>()
            transform<PreserveFirstFoundResourceTransformer>()
            filesMatching("**/*.class") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
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
            dependsOn(shadowMergedDevJar, shadowMergedJar, shadowSourcesJar)
        }

        jar {
            enabled = false
        }

        afterEvaluate {
            (components["java"] as AdhocComponentWithVariants).apply {
                configurations {
                    shadowRuntimeElements {
                        // Shadow plugin registers an extra shadowRuntimeElements variant.
                        // Keep it out of published metadata to avoid a duplicate runtime slot.
                        withVariantsFromConfiguration(this) {
                            skip()
                        }
                    }

                    runtimeElements {
                        // Replace the default jar artifact with shadowMergedJar so that
                        // run configs that resolve runtimeElements properly depend on shadowMergedJar.
                        outgoing.artifacts.clear()
                        outgoing.artifact(shadowMergedJar)

                        // Re-add the dev (remapped=true) variant pointing to shadowMergedDevJar.
                        outgoing.variants.create("remapped") {
                            attributes {
                                attribute(REMAPPED_ATTRIBUTE, true)
                                attribute(RemapNamespaceAttribute.ATTRIBUTE, RemapNamespaceAttribute.INITIAL)
                            }
                            artifact(shadowMergedDevJar)
                        }

                        addVariantsFromConfiguration(this) {
                            if (configurationVariant.name in listOf("classes", "resources")) {
                                skip()
                            }
                            mapToMavenScope("runtime")
                        }
                    }
                }

                val testTargets = cloche.targets.filter { it.isVersionTarget() }

                testTargets.forEach { target ->
                    for (variant in listOf(
                        "${target.featureName}ApiElements",
                        "${target.featureName}RuntimeElements"
                    )) {
                        configurations.named(variant) {
                            withVariantsFromConfiguration(this) {
                                skip()
                            }
                        }
                    }
                }
            }
        }
    }
    // endregion
}

// region Extension Properties

fun String.fabricApiVersion(): String? = when (this) {
    "1.20.1" -> "0.92.7"
    "1.21.1" -> "0.116.10"
    "26.1.2" -> "0.145.4"
    else -> null
}

fun String.parchmentVersion(): String? = when (this) {
    "1.20.1" -> "2023.09.03"
    "1.21.1" -> "2024.11.17"
    else -> null
}

fun String.forgeLoaderVersion(): String? = when (this) {
    "1.20.1" -> "47.4.20"
    else -> null
}

fun String.neoForgeLoaderVersion(): String? = when (this) {
    "1.21.1" -> "21.1.228"
    "26.1.2" -> "26.1.2.30-beta"
    else -> null
}

fun MinecraftTarget.isVersionTarget(): Boolean = name.startsWith("version:")

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
