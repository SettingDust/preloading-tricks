package settingdust.cloche_template.buildsrc

import earth.terrarium.cloche.ClocheExtension
import earth.terrarium.cloche.api.metadata.CommonMetadata
import org.gradle.api.Project

fun ClocheExtension.clocheTemplateMetadata(project: Project) {
    val rootProject = project.rootProject
    val id: String by rootProject.properties
    val source: String by rootProject.properties

    metadata {
        modId.set(id)
        name.set(rootProject.property("name").toString())
        description.set(rootProject.property("description").toString())
        license.set("Apache License 2.0")
        icon.set("assets/$id/icon.png")
        sources.set(source)
        issues.set("$source/issues")
        author(rootProject.property("author").toString())

        dependency {
            modId.set("minecraft")
            type.set(CommonMetadata.Dependency.Type.Required)
            version {
                start.set("1.20.1")
            }
        }
    }
}