package settingdust.cloche_template.buildsrc

import earth.terrarium.cloche.api.target.FabricTarget
import earth.terrarium.cloche.api.target.ForgeLikeTarget
import earth.terrarium.cloche.api.target.MinecraftTarget
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

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

fun String.toMinecraftVersionEnum(): MinecraftVersion = MinecraftVersion.fromValue(this)

fun MinecraftTarget.minecraftVersionEnum(): MinecraftVersion =
    minecraftVersion.orNull?.toMinecraftVersionEnum()
        ?: error("Target $name does not declare a minecraftVersion")

fun MinecraftTarget.isVersionTarget(): Boolean = name.startsWith("version:") || name.startsWith("run:")

fun MinecraftTarget.disableVersionTemplateTasks(project: Project) {
    project.tasks.named(generateModsManifestTaskName) { enabled = false }
    project.tasks.named(jarTaskName) { enabled = false }
    project.tasks.named(remapJarTaskName) { enabled = false }
    project.tasks.named(includeJarTaskName) { enabled = false }
}

fun MinecraftTarget.configureClientModClassesEnv() {
    runs {
        client {
            env("MOD_CLASSES", "")
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

val MinecraftTarget.accessWidenTaskName: String
    get() = lowerCamelCaseGradleName("accessWiden", featureName, "minecraft")

val MinecraftTarget.decompileMinecraftTaskName: String
    get() = lowerCamelCaseGradleName("decompile", featureName, "minecraft")