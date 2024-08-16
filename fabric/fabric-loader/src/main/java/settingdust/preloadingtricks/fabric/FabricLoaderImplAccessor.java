package settingdust.preloadingtricks.fabric;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class FabricLoaderImplAccessor {
    public static final Field FIELD_MODS;

    public static final Field FIELD_MOD_MAP;

    public static final Method METHOD_ADD_MOD;

    static {
        try {
            FIELD_MODS = FabricLoaderImpl.class.getDeclaredField("mods");
            FIELD_MODS.setAccessible(true);
            FIELD_MOD_MAP = FabricLoaderImpl.class.getDeclaredField("modMap");
            FIELD_MOD_MAP.setAccessible(true);
            METHOD_ADD_MOD = FabricLoaderImpl.class.getDeclaredMethod("addMod", ModCandidateImpl.class);
            METHOD_ADD_MOD.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
