plugins {
    alias(libs.plugins.neoforge.gradle)
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

repositories {
    maven("https://maven.neoforged.net/releases") { name = "NeoForge" }
}

dependencies {
    implementation(libs.neoforge)

    implementation(project(":services")) {
        isTransitive = false
    }
}
