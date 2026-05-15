package settingdust.cloche_template.buildsrc

import earth.terrarium.cloche.api.attributes.MinecraftModLoader
import earth.terrarium.cloche.api.target.FabricTarget
import earth.terrarium.cloche.api.target.ForgeTarget
import earth.terrarium.cloche.api.target.MinecraftTarget
import earth.terrarium.cloche.api.target.NeoforgeTarget
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyCollector

fun MinecraftTarget.multiversionLoader(): MinecraftModLoader = when (this) {
    is FabricTarget -> MinecraftModLoader.fabric
    is ForgeTarget -> MinecraftModLoader.forge
    is NeoforgeTarget -> MinecraftModLoader.neoforge
    else -> MinecraftModLoader.common
}

fun MultiversionDependencySpec.resolve(target: MinecraftTarget, project: Project): ExternalModuleDependency =
    resolve(target.multiversionLoader(), target.minecraftVersionEnum(), project)

operator fun DependencyCollector.invoke(target: MinecraftTarget, project: Project, spec: MultiversionDependencySpec) {
    add(spec.resolve(target, project))
}