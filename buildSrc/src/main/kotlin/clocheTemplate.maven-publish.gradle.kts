import earth.terrarium.cloche.ClocheExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getByType

plugins {
    `maven-publish`
}

val cloche = extensions.getByType<ClocheExtension>()

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("maven") {
                from(components["java"])

                artifact(tasks.named("shadowSourcesJar")) {
                    classifier = "sources"
                }

                pom {
                    name = cloche.metadata.modId
                    description = cloche.metadata.description
                    url = cloche.metadata.sources

                    licenses {
                        license {
                            name = cloche.metadata.license
                        }
                    }

                    developers {
                        developer {
                            name = cloche.metadata.authors.get().first().name
                            email = cloche.metadata.authors.get().first().contact
                        }
                    }
                }
            }
        }
    }
}
