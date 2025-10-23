package settingdust.preloading_tricks.fabric;

import de.florianmichael.asmfabricloader.api.event.PrePrePreLaunchEntrypoint;
import net.fabricmc.loader.impl.util.log.LogCategory;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;

public class PreloadingTricksLanguageAdapterEntrypoint implements PrePrePreLaunchEntrypoint {
    public static final LogCategory LOG_CATEGORY = LogCategory.createCustom("PreloadingTricks");

    @Override
    public void onLanguageAdapterLaunch() {
        PreloadingTricks.LOGGER.info("PreloadingTricks installed.");
        PreloadingTricksCallback.invoker.onSetupLanguageAdapter();
    }
}
