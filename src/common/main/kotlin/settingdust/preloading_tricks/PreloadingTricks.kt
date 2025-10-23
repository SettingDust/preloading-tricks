package settingdust.preloading_tricks

import org.apache.logging.log4j.LogManager
import settingdust.preloading_tricks.util.MinecraftAdapter
import settingdust.preloading_tricks.util.ServiceLoaderUtil

object PreloadingTricks {
    const val ID = "preloading_tricks"

    val LOGGER = LogManager.getLogger()

    init {
        ServiceLoaderUtil.defaultLogger = LOGGER
    }

    fun id(path: String) = MinecraftAdapter.id(ID, path)
}