plugins {
    alias(catalog.plugins.neoforge.gradle)
}

afterEvaluate {
    runs.clear()
}

jarJar.enable()

dependencies {
    implementation(catalog.neoforge)

    implementation(project(":services")) {
        isTransitive = false
    }
}
