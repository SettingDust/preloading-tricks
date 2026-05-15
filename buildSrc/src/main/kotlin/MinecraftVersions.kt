package settingdust.cloche_template.buildsrc

enum class MinecraftVersion(val value: String) {
    `20_1`("1.20.1"),
    `21_1`("1.21.1"),
    `26_1`("26.1.2");

    companion object {
        fun fromValue(value: String): MinecraftVersion =
            entries.firstOrNull { it.value == value }
                ?: error("Unsupported Minecraft version: $value")
    }
}