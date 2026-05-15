package settingdust.cloche_template.buildsrc

import earth.terrarium.cloche.api.attributes.MinecraftModLoader
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.kotlin.dsl.the
import javax.inject.Inject

typealias MultiversionResolver = (MinecraftModLoader, MinecraftVersion) -> String?

open class MultiversionDependencySpec @Inject constructor(private val dependencyName: String) : Named {
    var group: String? = null
    var artifact: String = dependencyName
    var version: String = ""

    private var groupResolver: MultiversionResolver? = null
    private var artifactResolver: MultiversionResolver? = null
    private var versionResolver: MultiversionResolver? = null

    override fun getName(): String = dependencyName

    fun resolve(
        group: MultiversionResolver? = null,
        artifact: MultiversionResolver? = null,
        version: MultiversionResolver? = null,
    ) {
        groupResolver = group
        artifactResolver = artifact
        versionResolver = version
    }

    fun resolve(project: Project): ExternalModuleDependency {
        require(groupResolver == null && artifactResolver == null && versionResolver == null) {
            "Dependency $name requires target context; use resolve(loader, mcVersion, project)"
        }

        return resolveDependency(group, artifact, version, project)
    }

    fun resolve(
        loader: MinecraftModLoader,
        mcVersion: MinecraftVersion,
        project: Project,
    ): ExternalModuleDependency {
        return resolveDependency(
            groupResolver?.invoke(loader, mcVersion) ?: group,
            artifactResolver?.invoke(loader, mcVersion) ?: artifact,
            versionResolver?.invoke(loader, mcVersion) ?: version,
            project,
        )
    }

    private fun resolveDependency(
        resolvedGroup: String?,
        resolvedArtifact: String?,
        resolvedVersion: String?,
        project: Project,
    ): ExternalModuleDependency {
        require(!resolvedArtifact.isNullOrBlank()) {
            "Dependency $name resolved an empty artifact"
        }
        require(!resolvedVersion.isNullOrBlank()) {
            "Dependency $name resolved an empty version"
        }
        require(!resolvedGroup.isNullOrBlank()) {
            "Dependency $name requires a group"
        }

        return project.dependencies.create("$resolvedGroup:$resolvedArtifact:$resolvedVersion") as ExternalModuleDependency
    }
}

open class MultiversionDependencies @Inject constructor(
    val project: Project,
    private val entries: NamedDomainObjectContainer<MultiversionDependencySpec>,
) : NamedDomainObjectContainer<MultiversionDependencySpec> by entries

private const val MULTIVERSION_DEPENDENCIES_EXTENSION = "multiversionDependencies"

val Project.multiversionDependencies: MultiversionDependencies
    get() = the()

fun Project.createMultiversionDependencies() {
    if (extensions.findByName(MULTIVERSION_DEPENDENCIES_EXTENSION) == null) {
        val instance = objects.newInstance(
            MultiversionDependencies::class.java,
            this,
            objects.domainObjectContainer(MultiversionDependencySpec::class.java),
        )
        extensions.add(MultiversionDependencies::class.java, MULTIVERSION_DEPENDENCIES_EXTENSION, instance)
    }
}

fun Project.multiversionDependencies(configure: MultiversionDependencies.() -> Unit) {
    multiversionDependencies.configure()
}
