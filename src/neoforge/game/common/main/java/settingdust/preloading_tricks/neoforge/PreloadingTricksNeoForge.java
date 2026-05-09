package settingdust.preloading_tricks.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.util.Entrypoint;

@Mod(PreloadingTricks.ID)
public class PreloadingTricksNeoForge {
    public PreloadingTricksNeoForge(IEventBus modEventBus) {
        @SuppressWarnings("unused")
        String id = PreloadingTricks.ID;
        Entrypoint.construct();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Entrypoint.init();
    }

    private void clientSetup(FMLClientSetupEvent event) {
        Entrypoint.clientInit();
    }
}
