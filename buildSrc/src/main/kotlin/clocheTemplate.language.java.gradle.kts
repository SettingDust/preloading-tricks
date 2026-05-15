import settingdust.cloche_template.buildsrc.*

val modName = providers.gradleProperty("name").get()

clocheTemplatePresetConventions {
	fabric {
		if (!isVersionTarget()) {
			metadata {
				entrypoint("main", "${project.group}.fabric.${modName}Fabric")
				entrypoint("client", "${project.group}.fabric.${modName}FabricClient")
			}
		}
	}

	forge {
		if (!isVersionTarget()) {
			metadata {
				modLoader.set("javafml")
				loaderVersion("47")
			}
		}
	}

	neoforge {
		if (!isVersionTarget()) {
			metadata {
				modLoader.set("javafml")
				loaderVersion("4")
			}
		}
	}
}