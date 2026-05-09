package settingdust.preloading_tricks.v26_1.util;

import net.minecraft.SharedConstants;

public class MinecraftVersionNameProvider implements settingdust.preloading_tricks.util.MinecraftVersionNameProvider {
    @Override
    public String currentVersionName() {
        return SharedConstants.getCurrentVersion().name();
    }
}
