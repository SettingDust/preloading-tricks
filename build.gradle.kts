plugins {
    java
    `maven-publish`

    alias(libs.plugins.minotaur)
}

val archives_name: String by project

group = project.property("group").toString()
version = project.property("version").toString()

subprojects {
    apply(plugin = "java")

    group = rootProject.group
    version = rootProject.version

    base {
        archivesName.set("$archives_name-${name}")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withSourcesJar()
    }

    tasks {
        jar {
            from("LICENSE") {
                rename { "${it}_${base.archivesName}" }
            }
        }
    }
}
