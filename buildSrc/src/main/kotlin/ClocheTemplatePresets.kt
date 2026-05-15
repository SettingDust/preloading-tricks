package settingdust.cloche_template.buildsrc

import earth.terrarium.cloche.api.metadata.CommonMetadata
import earth.terrarium.cloche.api.target.FabricTarget
import earth.terrarium.cloche.api.target.ForgeLikeTarget
import earth.terrarium.cloche.api.target.ForgeTarget
import earth.terrarium.cloche.api.target.MinecraftTarget
import earth.terrarium.cloche.api.target.NeoforgeTarget
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named

fun MinecraftTarget.commonDefaults(project: Project) {
    runs {
        client.onConfigured {
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

    if (isVersionTarget()) {
        disableVersionTemplateTasks(project)

        runs {
            client()
        }
    }
}

fun FabricTarget.fabricDefaults(project: Project) {
    loaderVersion.set("0.19.2")
    includedClient()

    dependencies {
        fabricApi(minecraftVersion.map(String::fabricApiVersion))
    }

    project.clocheTemplatePresetConventions.applyFabric(this)

    if (isVersionTarget()) return

    metadata {
        dependency {
            modId.set("fabric-api")
            type.set(CommonMetadata.Dependency.Type.Required)
        }
    }
}

fun ForgeTarget.forgeDefaults(project: Project) {
    loaderVersion.set(minecraftVersion.map(String::forgeLoaderVersion))

    if (isVersionTarget()) {
        runs {
            client {
                env("MOD_CLASSES", "")
            }
        }
    }

    project.clocheTemplatePresetConventions.applyForge(this)

    if (isVersionTarget()) return

    metadata {
        dependency {
            modId.set("preloading_tricks")
            type.set(CommonMetadata.Dependency.Type.Recommended)
        }
    }
}

fun NeoforgeTarget.neoforgeDefaults(project: Project) {
    loaderVersion.set(minecraftVersion.map(String::neoForgeLoaderVersion))

    if (isVersionTarget()) {
        runs {
            client {
                env("MOD_CLASSES", "")
            }
        }
    }

    project.clocheTemplatePresetConventions.applyNeoforge(this)

    if (isVersionTarget()) return

    metadata {
        dependency {
            modId.set("preloading_tricks")
            type.set(CommonMetadata.Dependency.Type.Recommended)
        }
    }
}

fun ForgeLikeTarget.bootstrapDefaults(project: Project) {
    project.tasks.named(generateModsTomlTaskName) { enabled = false }
}

fun NamedDomainObjectCollection<MinecraftTarget>.applySharedTargetDefaults(project: Project) {
    withType(FabricTarget::class.java).configureEach {
        fabricDefaults(project)
    }

    withType(ForgeTarget::class.java).configureEach {
        forgeDefaults(project)
    }

    withType(NeoforgeTarget::class.java).configureEach {
        neoforgeDefaults(project)
    }

    configureEach {
        commonDefaults(project)
    }
}