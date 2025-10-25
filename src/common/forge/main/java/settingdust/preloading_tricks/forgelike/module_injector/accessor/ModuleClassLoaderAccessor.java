package settingdust.preloading_tricks.forgelike.module_injector.accessor;

import cpw.mods.cl.ModuleClassLoader;
import settingdust.preloading_tricks.forgelike.UnsafeHacks;

import java.lang.module.Configuration;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.lang.reflect.Field;
import java.util.Map;

public class ModuleClassLoaderAccessor {
    public static final Class<ModuleClassLoader> clazz = ModuleClassLoader.class;

    private static final Field configurationField;
    private static final Field packageLookupField;
    private static final Field resolvedRootsField;

    static {
        try {
            configurationField = clazz.getDeclaredField("configuration");
            packageLookupField = clazz.getDeclaredField("packageLookup");
            resolvedRootsField = clazz.getDeclaredField("resolvedRoots");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Configuration getConfiguration(ModuleClassLoader moduleClassLoader) {
        return UnsafeHacks.getField(configurationField, moduleClassLoader);
    }

    public static void setConfiguration(ModuleClassLoader moduleClassLoader, Configuration configuration) {
        UnsafeHacks.setField(configurationField, moduleClassLoader, configuration);
    }

    public static Map<String, ResolvedModule> getPackageLookup(ModuleClassLoader moduleClassLoader) {
        return UnsafeHacks.getField(packageLookupField, moduleClassLoader);
    }

    public static void setPackageLookup(ModuleClassLoader moduleClassLoader, Map<String, ResolvedModule> map) {
        UnsafeHacks.setField(packageLookupField, moduleClassLoader, map);
    }

    public static Map<String, ModuleReference> getResolvedRoots(ModuleClassLoader moduleClassLoader) {
        return UnsafeHacks.getField(resolvedRootsField, moduleClassLoader);
    }

    public static void setResolvedRoots(ModuleClassLoader moduleClassLoader, Map<String, ModuleReference> map) {
        UnsafeHacks.setField(resolvedRootsField, moduleClassLoader, map);
    }
}
