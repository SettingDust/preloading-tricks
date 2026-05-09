package settingdust.preloading_tricks.neoforge.v21_1;

import settingdust.preloading_tricks.util.Entrypoint;
import settingdust.preloading_tricks.util.MinecraftVersion;

public class PreloadingTricksNeoForgeEntrypoint implements Entrypoint {
    public PreloadingTricksNeoForgeEntrypoint() {
        MinecraftVersion.V1211.requireCurrent();
    }

    @Override
    public void onConstruct() {
    }
}
