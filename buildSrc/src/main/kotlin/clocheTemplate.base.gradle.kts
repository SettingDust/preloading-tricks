import earth.terrarium.cloche.api.attributes.TargetAttributes
import groovy.lang.Closure
import settingdust.cloche_template.buildsrc.*

plugins {
    java
    idea
    id("com.palantir.git-version")
    id("com.gradleup.shadow")
    id("earth.terrarium.cloche")
}

val archive_name: String by rootProject.properties
val gitVersion: Closure<String> by extra

version = gitVersion()

createClocheTemplatePresetConventions()
createMultiversionDependencies()

cloche {
    clocheTemplateMetadata(project)

    mappings {
        official()
    }

    common {}

    targets.applySharedTargetDefaults(project)
}

base { archivesName = archive_name }

repositories {
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven")
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    maven("https://repo.nyon.dev/releases") {
        content {
            includeGroup("dev.nyon")
        }
    }

    maven("https://maven.lenni0451.net/snapshots/") {
        content {
            includeGroupAndSubgroups("net.lenni0451")
        }
    }

    maven("https://maven.su5ed.dev/releases") {
        content {
            includeGroupAndSubgroups("dev.su5ed.sinytra")
            includeGroupAndSubgroups("org.sinytra")
        }
    }

    maven("https://maven.sinytra.org/") {
        content {
            includeGroupAndSubgroups("org.sinytra")
        }
    }

    maven("https://raw.githubusercontent.com/settingdust/maven/main/repository/") {
        name = "SettingDust's Maven"
    }

    mavenCentral()

    cloche {
        librariesMinecraft()
        main()
        mavenFabric()
        mavenForge()
        mavenNeoforged()
        mavenNeoforgedMeta()
        mavenParchment()
    }

    mavenLocal()
}

dependencies {
    attributesSchema {
        attribute(TargetAttributes.MINECRAFT_VERSION) {
            compatibilityRules.add(MinecraftVersionCompatibilityRule::class)
        }
        attribute(TargetAttributes.MOD_LOADER) {
            compatibilityRules.add(MinecraftModLoaderCompatibilityRule::class)
        }
        attribute(TargetAttributes.CLOCHE_MINECRAFT_VERSION) {
            compatibilityRules.add(MinecraftVersionCompatibilityRule::class)
        }
        attribute(TargetAttributes.CLOCHE_MOD_LOADER) {
            compatibilityRules.add(MinecraftModLoaderCompatibilityRule::class)
        }
    }
}
