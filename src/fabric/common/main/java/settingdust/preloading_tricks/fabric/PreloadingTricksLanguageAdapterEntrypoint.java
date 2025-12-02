package settingdust.preloading_tricks.fabric;

import de.florianmichael.asmfabricloader.api.event.PrePrePreLaunchEntrypoint;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallbacks;
import settingdust.preloading_tricks.fabric.mod_candidate.ExtraModsLoader;

public class PreloadingTricksLanguageAdapterEntrypoint implements PrePrePreLaunchEntrypoint {
    @Override
    public void onLanguageAdapterLaunch() {
        PreloadingTricks.LOGGER.info("[{}] installed.", PreloadingTricks.NAME);

        PreloadingTricksCallbacksInvoker.onSetupLanguageAdapter();

        PreloadingTricksCallbacks.SETUP_MODS.register(_manager -> {
            if (!(_manager instanceof FabricModManager manager)) return;

            ExtraModsLoader.onSetupMods(manager);
        });
    }
}
