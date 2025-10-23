package settingdust.preloading_tricks.forge

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import settingdust.preloading_tricks.PreloadingTricks
import settingdust.preloading_tricks.util.Entrypoint
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(PreloadingTricks.ID)
object PreloadingTricksForge {
    init {
        requireNotNull(PreloadingTricks)
        Entrypoint.construct()
        MOD_BUS.apply {
            addListener<FMLCommonSetupEvent> {
                Entrypoint.init()
            }
            addListener<FMLClientSetupEvent> { Entrypoint.clientInit() }
        }
    }
}