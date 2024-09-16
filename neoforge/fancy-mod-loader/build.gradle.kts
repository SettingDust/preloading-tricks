plugins {
    alias(catalog.plugins.neoforge.moddev)
    alias(catalog.plugins.shadow)
}

val mod_id: String by rootProject

neoForge {
    version = catalog.neoforge.get().version!!

    runs {
        create("client") {
            client()
        }
    }

    mods {
        create(mod_id) {
            sourceSet(sourceSets.main.get())
            sourceSet(project(":neoforge:language-loader").sourceSets.main.get())
        }
    }
}

dependencies {
    implementation(project(":services")) {
        isTransitive = false
    }

    jarJar(implementation(project(":neoforge:language-loader"))!!)
    shadow(implementation(project(":neoforge:api")) {
        isTransitive = false
    })
}

tasks {
    jar {
        finalizedBy(shadowJar)
    }

    shadowJar {
        dependsOn(jar)
        from(jar)
        configurations = listOf(project.configurations.shadow.get())
    }
}

artifacts {
    shadow(tasks.shadowJar)
}
