plugins {
    alias(catalog.plugins.neoforge.moddev)
}

val mod_id: String by rootProject

neoForge {
    version = catalog.neoforge.get().version!!
}

dependencies {
    implementation(project(":services")) {
        isTransitive = false
    }
}
