plugins {
    alias(catalog.plugins.neoforge.gradle)
}

val mod_id: String by rootProject

minecraft {
    modIdentifier("${mod_id}_language_loader")

    runs {
        afterEvaluate {
            clear()
        }
    }
}

jarJar.enable()

dependencies {
    implementation(catalog.neoforge)

    implementation(project(":services")) {
        isTransitive = false
    }
}
