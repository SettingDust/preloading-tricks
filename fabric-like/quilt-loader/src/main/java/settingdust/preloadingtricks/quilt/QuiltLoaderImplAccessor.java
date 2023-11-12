package settingdust.preloadingtricks.quilt;

import org.quiltmc.loader.api.plugin.ModContainerExt;
import org.quiltmc.loader.impl.QuiltLoaderImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class QuiltLoaderImplAccessor {
    public static final Field FIELD_MODS;

    public static final Field FIELD_MOD_MAP;

    public static final Method METHOD_ADD_MOD;

    static {
        try {
            FIELD_MODS = QuiltLoaderImpl.class.getDeclaredField("mods");
            FIELD_MODS.setAccessible(true);
            FIELD_MOD_MAP = QuiltLoaderImpl.class.getDeclaredField("modMap");
            FIELD_MOD_MAP.setAccessible(true);
            METHOD_ADD_MOD = QuiltLoaderImpl.class.getDeclaredMethod("addMod", ModContainerExt.class);
            METHOD_ADD_MOD.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
