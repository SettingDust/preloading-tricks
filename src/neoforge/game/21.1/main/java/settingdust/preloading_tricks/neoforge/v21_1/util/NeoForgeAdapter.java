package settingdust.preloading_tricks.neoforge.v21_1.util;

import net.neoforged.fml.loading.FMLLoader;
import settingdust.preloading_tricks.neoforge.util.NeoForgeAdapter;
import settingdust.preloading_tricks.util.MinecraftVersion;

public class NeoForgeAdapter implements NeoForgeAdapter {
    public NeoForgeAdapter() {
        MinecraftVersion.V1211.requireCurrent();
    }

    @Override
    public net.neoforged.api.distmarker.Dist getDist() {
        return FMLLoader.getDist();
    }
}
