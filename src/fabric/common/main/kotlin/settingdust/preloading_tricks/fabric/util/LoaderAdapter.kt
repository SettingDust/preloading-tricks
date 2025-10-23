package settingdust.preloading_tricks.fabric.util

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import settingdust.preloading_tricks.util.LoaderAdapter

class LoaderAdapter : LoaderAdapter {
    override val isClient = FabricLoader.getInstance().environmentType === EnvType.CLIENT

    override fun isModLoaded(modId: String) = FabricLoader.getInstance().isModLoaded(modId)
}