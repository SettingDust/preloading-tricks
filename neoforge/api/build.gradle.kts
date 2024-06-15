plugins {
    alias(libs.plugins.neoforge.gradle)
}

minecraft {
    runs {
        afterEvaluate {
            clear()
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
}
