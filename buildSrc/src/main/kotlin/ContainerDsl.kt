package settingdust.cloche_template.buildsrc

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import earth.terrarium.cloche.INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE
import earth.terrarium.cloche.REMAPPED_ATTRIBUTE
import earth.terrarium.cloche.api.attributes.IncludeTransformationStateAttribute
import earth.terrarium.cloche.api.attributes.MinecraftModLoader
import earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute
import earth.terrarium.cloche.api.attributes.TargetAttributes
import earth.terrarium.cloche.api.target.MinecraftTarget
import earth.terrarium.cloche.api.target.compilation.ClocheDependencyHandler
import earth.terrarium.cloche.util.fromJars
import earth.terrarium.cloche.util.target
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.fabric.task.JarInJar
import net.msrandom.minecraftcodev.forge.task.JarJar
import net.msrandom.minecraftcodev.includes.IncludesJar
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.ExternalModuleDependencyVariantSpec
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.file.CopySpec
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf

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
                attribute(REMAPPED_ATTRIBUTE, false)
                attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
                attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
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
        project.tasks.named(LifecycleBasePlugin.BUILD_TASK_NAME) {
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
            manifest.fromJars(project.serviceOf(), input)
            fromResolutionResults(configuration)
        }

        else -> project.tasks.register<JarJar>(lowerCamelCaseGradleName(featureName, name)) {
            group = "build"
            this.archiveClassifier = archiveClassifier
            if (toIntermediateOutputs) {
                destinationDirectory = intermediateOutputsDirectory
            }
            input = jarTask.flatMap { it.archiveFile }
            manifest.fromJars(project.serviceOf(), input)
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
        
        fun includeTask(task: TaskProvider<out AbstractArchiveTask>) {
            includeJarTask.configure {
                dependsOn(task)
            }
            addTo(includeConfigurationProvider, project.files(task.flatMap { it.archiveFile }))
        }
        
        fun includeDevTask(task: TaskProvider<out AbstractArchiveTask>) {
            includeDevJarTask.configure {
                dependsOn(task)
            }
            addTo(includeDevConfigurationProvider, project.files(task.flatMap { it.archiveFile }))
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

fun Project.container(
    loader: MinecraftModLoader,
    block: ContainerScope.() -> Unit,
): ContainerScope = ContainerScope(this, loader).apply(block)

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