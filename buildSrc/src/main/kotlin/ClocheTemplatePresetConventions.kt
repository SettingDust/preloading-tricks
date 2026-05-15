package settingdust.cloche_template.buildsrc

import earth.terrarium.cloche.api.target.FabricTarget
import earth.terrarium.cloche.api.target.ForgeTarget
import earth.terrarium.cloche.api.target.NeoforgeTarget
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.the

open class ClocheTemplatePresetConventions {
    private val fabricConfigurators = mutableListOf<FabricTarget.() -> Unit>()
    private val forgeConfigurators = mutableListOf<ForgeTarget.() -> Unit>()
    private val neoforgeConfigurators = mutableListOf<NeoforgeTarget.() -> Unit>()

    fun fabric(configure: FabricTarget.() -> Unit) {
        fabricConfigurators += configure
    }

    fun forge(configure: ForgeTarget.() -> Unit) {
        forgeConfigurators += configure
    }

    fun neoforge(configure: NeoforgeTarget.() -> Unit) {
        neoforgeConfigurators += configure
    }

    internal fun applyFabric(target: FabricTarget) {
        fabricConfigurators.forEach { target.it() }
    }

    internal fun applyForge(target: ForgeTarget) {
        forgeConfigurators.forEach { target.it() }
    }

    internal fun applyNeoforge(target: NeoforgeTarget) {
        neoforgeConfigurators.forEach { target.it() }
    }
}

private const val CLOCHETEMPLATE_PRESET_CONVENTIONS_EXTENSION = "clocheTemplatePresetConventions"

val Project.clocheTemplatePresetConventions: ClocheTemplatePresetConventions
    get() = the()

fun Project.createClocheTemplatePresetConventions() {
    if (extensions.findByName(CLOCHETEMPLATE_PRESET_CONVENTIONS_EXTENSION) == null) {
        extensions.create<ClocheTemplatePresetConventions>(CLOCHETEMPLATE_PRESET_CONVENTIONS_EXTENSION)
    }
}

fun Project.clocheTemplatePresetConventions(configure: ClocheTemplatePresetConventions.() -> Unit) {
    clocheTemplatePresetConventions.configure()
}