package settingdust.cloche_template.buildsrc

import earth.terrarium.cloche.api.attributes.MinecraftModLoader

fun loaderArtifactOverrides(vararg artifacts: Pair<MinecraftModLoader, String>): MultiversionResolver = { loader, _ ->
    artifacts.firstOrNull { it.first == loader }
        ?.second
}

fun versionLoaderMcVersionPattern(version: String): MultiversionResolver = { loader, mcVersion ->
    "$version-${loader.name.lowercase()},${mcVersion.value}"
}