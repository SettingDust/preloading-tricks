package settingdust.cloche_template.buildsrc

import earth.terrarium.cloche.api.attributes.MinecraftModLoader
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private class RegisteredSpecDelegate(
    private val configure: MultiversionDependencySpec.() -> Unit,
) : ReadOnlyProperty<MultiversionDependencies, MultiversionDependencySpec> {
    private var cached: MultiversionDependencySpec? = null

    override fun getValue(
        thisRef: MultiversionDependencies,
        property: KProperty<*>,
    ): MultiversionDependencySpec {
        return cached ?: thisRef.maybeCreate(property.name).apply(configure).also { cached = it }
    }
}

fun maven(
    group: String,
    artifact: String,
    version: String,
    action: MultiversionDependencySpec.() -> Unit = {},
): ReadOnlyProperty<MultiversionDependencies, MultiversionDependencySpec> =
    RegisteredSpecDelegate {
        this.group = group
        this.artifact = artifact
        this.version = version
        action()
    }

fun modrinth(
    artifact: String,
    version: String,
    action: MultiversionDependencySpec.() -> Unit = {},
): ReadOnlyProperty<MultiversionDependencies, MultiversionDependencySpec> =
    RegisteredSpecDelegate {
        this.group = "maven.modrinth"
        this.artifact = artifact
        this.version = version
        action()
    }

// Preset specs — infrastructure-level, not modified by users.
// These are regular delegated extension properties (no custom getter).

val MultiversionDependencies.mixinextras: MultiversionDependencySpec by maven(
    group = "io.github.llamalad7",
    artifact = "mixinextras",
    version = "0.5.4",
) {
    resolve(
        artifact = loaderArtifactOverrides(
            MinecraftModLoader.fabric to "mixinextras-fabric",
            MinecraftModLoader.forge to "mixinextras-forge",
            MinecraftModLoader.common to "mixinextras-common",
        )
    )
}

val MultiversionDependencies.reflect: MultiversionDependencySpec by maven(
    group = "net.lenni0451",
    artifact = "Reflect",
    version = "1.6.3",
)

val MultiversionDependencies.commonsUnchecked: MultiversionDependencySpec by maven(
    group = "net.lenni0451.commons",
    artifact = "unchecked",
    version = "1.9.2",
)

val MultiversionDependencies.classTransform: MultiversionDependencySpec by maven(
    group = "net.lenni0451.classtransform",
    artifact = "core",
    version = "1.15.0-SNAPSHOT",
)

val MultiversionDependencies.classTransformAdditionalClassProvider: MultiversionDependencySpec by maven(
    group = "net.lenni0451.classtransform",
    artifact = "additionalclassprovider",
    version = "1.15.0-SNAPSHOT",
)

val MultiversionDependencies.classTransformMixinsTranslator: MultiversionDependencySpec by maven(
    group = "net.lenni0451.classtransform",
    artifact = "mixinstranslator",
    version = "1.15.0-SNAPSHOT",
)

val MultiversionDependencies.byteBuddyAgent: MultiversionDependencySpec by maven(
    group = "net.bytebuddy",
    artifact = "byte-buddy-agent",
    version = "1.18.8",
)

val MultiversionDependencies.preloadingTricks: MultiversionDependencySpec by maven(
    group = "settingdust.preloading_tricks",
    artifact = "PreloadingTricks",
    version = "3.6.3",
)

val MultiversionDependencies.fabricLanguageKotlin: MultiversionDependencySpec by maven(
    group = "net.fabricmc",
    artifact = "fabric-language-kotlin",
    version = "1.13.11+kotlin.2.3.21",
)

val MultiversionDependencies.klf: MultiversionDependencySpec by maven(
    group = "dev.nyon",
    artifact = "KotlinLangForge",
    version = "2.12.0-k2.3.21",
) {
    resolve(version = { loader, mcVersion ->
        when (loader to mcVersion) {
            MinecraftModLoader.forge to MinecraftVersion.`20_1` ->
                "$version-2.0+forge"

            MinecraftModLoader.neoforge to MinecraftVersion.`21_1` ->
                "$version-3.0+neoforge"

            MinecraftModLoader.neoforge to MinecraftVersion.`26_1` ->
                "$version-3.1+neoforge"

            else -> error("No KLF variant for $loader / $mcVersion; choose a dependency manually")
        }
    })
}
