package settingdust.preloading_tricks.fabric

import settingdust.preloading_tricks.PreloadingTricks
import settingdust.preloading_tricks.util.Entrypoint

object PreloadingTricksFabric {
    init {
        requireNotNull(PreloadingTricks)
        Entrypoint.construct()
    }

    fun init() {
        Entrypoint.init()
    }

    fun clientInit() {
        Entrypoint.clientInit()
    }
}
