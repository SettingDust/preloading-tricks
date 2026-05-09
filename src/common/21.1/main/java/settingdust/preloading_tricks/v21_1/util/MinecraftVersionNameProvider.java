package settingdust.preloading_tricks.v21_1.util;

import net.minecraft.SharedConstants;
import settingdust.preloading_tricks.util.MinecraftVersionNameProvider;

public class MinecraftVersionNameProvider implements settingdust.preloading_tricks.util.MinecraftVersionNameProvider {
    @Override
    public String currentVersionName() {
        return SharedConstants.getCurrentVersion().getName();
    }
}
