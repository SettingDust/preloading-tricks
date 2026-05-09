package settingdust.preloading_tricks.neoforge.v26_1;

import settingdust.preloading_tricks.util.Entrypoint;
import settingdust.preloading_tricks.util.MinecraftVersion;

public class PreloadingTricksNeoForgeEntrypoint implements Entrypoint {
    public PreloadingTricksNeoForgeEntrypoint() {
        MinecraftVersion.V261.requireCurrent();
    }

    @Override
    public void onConstruct() {
    }
}
