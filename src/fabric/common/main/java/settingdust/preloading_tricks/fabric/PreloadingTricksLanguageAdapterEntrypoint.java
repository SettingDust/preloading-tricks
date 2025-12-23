package settingdust.preloading_tricks.fabric;

import de.florianmichael.asmfabricloader.api.event.PrePrePreLaunchEntrypoint;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingEntrypoint;
import settingdust.preloading_tricks.util.ServiceLoaderUtil;

import java.util.ServiceLoader;

public class PreloadingTricksLanguageAdapterEntrypoint implements PrePrePreLaunchEntrypoint {
    @Override
    public void onLanguageAdapterLaunch() {
        PreloadingTricks.LOGGER.info("[{}] installed.", PreloadingTricks.NAME);

        ServiceLoaderUtil.loadServices(
            PreloadingEntrypoint.class,
            ServiceLoader.load(PreloadingEntrypoint.class, PreloadingEntrypoint.class.getClassLoader()),
            false
        );

        PreloadingTricksCallbacksInvoker.onSetupLanguageAdapter();
    }
}
