package settingdust.preloading_tricks.v26_1.util;

import settingdust.preloading_tricks.util.MinecraftVersion;

public class MinecraftAdapter implements settingdust.preloading_tricks.util.MinecraftAdapter {
    public MinecraftAdapter() {
        MinecraftVersion.V261.requireCurrent();
    }
}
