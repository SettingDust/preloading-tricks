package settingdust.preloading_tricks.modlauncher.module_injector.accessor;

import cpw.mods.cl.ModuleClassLoader;
import settingdust.preloading_tricks.forgelike.JavaBypass;

import java.lang.invoke.MethodHandle;

public class LayerInfoAccessor {
    public static final Class<?> clazz;

    private static final MethodHandle cl;

    static {
        try {
            clazz = Class.forName("cpw.mods.modlauncher.ModuleLayerHandler$LayerInfo");
            var lookup = JavaBypass.getTrustedLookup().in(clazz);
            cl = lookup.findGetter(clazz, "cl", ModuleClassLoader.class);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static ModuleClassLoader getModuleClassLoader(Object layerInfo) {
        try {
            return (ModuleClassLoader) cl.invoke(layerInfo);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
