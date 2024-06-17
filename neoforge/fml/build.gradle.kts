plugins {
    alias(libs.plugins.neoforge.gradle)
    alias(libs.plugins.shadow)
}

val mod_id: String by rootProject

minecraft {
    modIdentifier(mod_id)

    runs {
        configureEach {
            modSource(project(":neoforge:language-loader").sourceSets.main.get())
        }

        afterEvaluate {
            removeIf { !it.isClient.get() }
        }
    }
}

jarJar.enable()

repositories {
    maven("https://maven.neoforged.net/releases") { name = "NeoForge" }
}

dependencies {
    implementation(libs.neoforge)

    implementation(project(":services")) {
        isTransitive = false
    }

    jarJar(implementation(project(":neoforge:language-loader"))!!)
    shadow(implementation(project(":neoforge:api")) {
        isTransitive = false
    })
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier = ""
    }

    this.jarJar {
        from(shadowJar.get().archiveFile)
    }
}
