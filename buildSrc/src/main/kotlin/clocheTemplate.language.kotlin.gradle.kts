import earth.terrarium.cloche.api.metadata.CommonMetadata
import settingdust.cloche_template.buildsrc.*

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

val modName = providers.gradleProperty("name").get()

clocheTemplatePresetConventions {
    fabric {
        dependencies {
            modImplementation(project.multiversionDependencies.fabricLanguageKotlin.resolve(project))
        }

        if (!isVersionTarget()) {
            metadata {
                entrypoint("main") {
                    adapter.set("kotlin")
                    value.set("${project.group}.fabric.${modName}Fabric::init")
                }

                entrypoint("client") {
                    adapter.set("kotlin")
                    value.set("${project.group}.fabric.${modName}Fabric::clientInit")
                }

                dependency {
                    modId.set("fabric-language-kotlin")
                    type.set(CommonMetadata.Dependency.Type.Required)
                }
            }
        }
    }

    forge {
        if (!isVersionTarget()) {
            metadata {
                modLoader.set("klf")
                loaderVersion("1")
            }
        }
    }

    neoforge {
        if (!isVersionTarget()) {
            metadata {
                modLoader.set("klf")
                loaderVersion("1")
            }
        }
    }
}