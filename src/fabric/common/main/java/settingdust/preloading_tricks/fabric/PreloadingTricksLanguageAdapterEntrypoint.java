package settingdust.preloading_tricks.fabric;

import de.florianmichael.asmfabricloader.api.event.PrePrePreLaunchEntrypoint;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;

public class PreloadingTricksLanguageAdapterEntrypoint implements PrePrePreLaunchEntrypoint {
    @Override
    public void onLanguageAdapterLaunch() {
        PreloadingTricks.LOGGER.info("[{}] installed.", PreloadingTricks.NAME);
        PreloadingTricksCallback.invoker.onSetupLanguageAdapter();
    }
}
