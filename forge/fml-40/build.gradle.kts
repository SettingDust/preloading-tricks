plugins {
    alias(catalog.plugins.forge.gradle)
    alias(catalog.plugins.librarian.forgegradle)
    alias(catalog.plugins.mixin)
    alias(catalog.plugins.shadow)
}

val mod_id: String by rootProject

minecraft {
    mappings("official", "1.18.2")

    runs.all {
        mods {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            property("forge.enabledGameTestNamespaces", mod_id)
            property("terminal.jline", "true")
            mods { create(mod_id) { source(sourceSets.main.get()) } }
        }
    }

    runs.run {
        create("client") {
            property("log4j.configurationFile", "log4j2.xml")
            jvmArg("-XX:+AllowEnhancedClassRedefinition")
            args("--username", "Player")
        }

        create("server") {}
        create("gameTestServer") {}
        create("data") {
            workingDirectory(project.file("run"))
            args(
                "--mod",
                mod_id,
                "--all",
                "--output",
                file("src/generated/resources/"),
                "--existing",
                file("src/main/resources")
            )
        }
    }
}

dependencies {
    minecraft(catalog.minecraft.forge.get1().get18().get2())
    annotationProcessor(variantOf(catalog.mixin) { classifier("processor") })

    implementation(project(":services")) {
        isTransitive = false
    }

    jarJar(implementation(project(":forge:language-provider")) {
        isTransitive = false
    })
    shadow(implementation(project(":forge:api")) {
        isTransitive = false
    })
}

tasks {
    shadowJar {
        dependsOn(sourcesJar)
        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier = "dev"
        destinationDirectory = layout.buildDirectory.dir("devlibs")
    }

    classes {
        finalizedBy(jar)
    }

    jar {
        manifest.attributes(
            "FMLModType" to "LIBRARY"
        )
    }
}
