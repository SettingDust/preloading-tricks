import net.minecraftforge.gradle.userdev.tasks.RenameJarInPlace

plugins {
    alias(catalog.plugins.forge.gradle)
    alias(catalog.plugins.librarian.forgegradle)
    alias(catalog.plugins.mixin)
    alias(catalog.plugins.shadow)
}

val mod_id: String by rootProject

minecraft {
    mappings(
        "parchment", "${catalog.versions.parchmentmc.asProvider().get()}-${catalog.versions.minecraft.get()}"
    )

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

    runs {
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
    minecraft(catalog.minecraft.forge)
    annotationProcessor(variantOf(catalog.mixin) { classifier("processor") })

    implementation(project(":services")) {
        isTransitive = false
    }

    implementation(project(":lexforge:language-provider"))
    jarJar(project(":lexforge:language-provider")) {
        jarJar.ranged(this, "[$version, )")
        isTransitive = false
    }
    shadow(implementation(project(":lexforge:api")) {
        isTransitive = false
    })
}

reobf {
    create("shadowJar") {}
}

tasks {
    shadowJar {
        dependsOn(sourcesJar, this@tasks.jarJar)
        from(this@tasks.jarJar)
        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier = ""

        manifest.attributes(
            "FMLModType" to "LIBRARY"
        )
    }

    afterEvaluate {
        named<RenameJarInPlace>("reobfJar") {
            enabled = false
        }
    }

    jar {
        enabled = false
    }
}
