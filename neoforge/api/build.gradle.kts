plugins {
    alias(catalog.plugins.neoforge.gradle)
}

minecraft {
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
