package settingdust.cloche_template.buildsrc

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.the

open class ClocheTemplateDsl(project: Project) {
    /**
     * Controls generated-project dev/remapped publication surface:
     * - includeDev jar wiring
     * - remapped outgoing runtime variant publication
     * - dev runtime variant exposure path.
     *
     * Default is true to preserve existing generated-project behavior.
     */
    val remappedDevVariants: Property<Boolean> = project.objects.property(Boolean::class.java).convention(true)
}

private const val CLOCHETEMPLATE_DSL_EXTENSION = "clocheTemplate"

val Project.clocheTemplate: ClocheTemplateDsl
    get() = the()

fun Project.createClocheTemplateDsl() {
    if (extensions.findByName(CLOCHETEMPLATE_DSL_EXTENSION) == null) {
        extensions.create<ClocheTemplateDsl>(CLOCHETEMPLATE_DSL_EXTENSION, this)
    }
}

fun Project.clocheTemplate(configure: ClocheTemplateDsl.() -> Unit) {
    clocheTemplate.configure()
}
