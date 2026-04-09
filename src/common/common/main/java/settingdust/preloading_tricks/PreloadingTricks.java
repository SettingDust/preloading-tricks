package settingdust.preloading_tricks;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settingdust.preloading_tricks.util.MinecraftAdapter;

public final class PreloadingTricks {
    public static final String ID = "preloading_tricks";
    public static final String NAME = "Preloading Tricks";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    private PreloadingTricks() {}

    public static ResourceLocation id(String path) {
        return MinecraftAdapter.getInstance().id(ID, path);
    }
}
