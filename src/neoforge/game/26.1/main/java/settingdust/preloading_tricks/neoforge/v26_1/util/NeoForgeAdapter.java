package settingdust.preloading_tricks.neoforge.v26_1.util;

import net.neoforged.fml.loading.FMLLoader;
import settingdust.preloading_tricks.neoforge.util.NeoForgeAdapter;
import settingdust.preloading_tricks.util.MinecraftVersion;

public class NeoForgeAdapter implements NeoForgeAdapter {
    public NeoForgeAdapter() {
        MinecraftVersion.V261.requireCurrent();
    }

    @Override
    public net.neoforged.api.distmarker.Dist getDist() {
        return FMLLoader.getCurrent().dist();
    }
}
