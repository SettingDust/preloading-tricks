plugins {
    alias(catalog.plugins.neoforge.gradle)
    alias(catalog.plugins.shadow)
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

dependencies {
    implementation(catalog.neoforge)

//    implementation(project(":services")) {
//        isTransitive = false
//    }
//
//    jarJar(implementation(project(":neoforge:language-loader"))!!)
//    shadow(implementation(project(":neoforge:api")) {
//        isTransitive = false
//    })
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier = ""
    }

    this.jarJar {
        from(zipTree(shadowJar.get().archiveFile))
    }
}
