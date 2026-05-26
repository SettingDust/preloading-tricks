import settingdust.cloche_template.buildsrc.*

val modName = providers.gradleProperty("name").get()

clocheTemplatePresetConventions {
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