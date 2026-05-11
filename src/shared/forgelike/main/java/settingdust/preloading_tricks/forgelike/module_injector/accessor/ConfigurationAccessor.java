package settingdust.preloading_tricks.forgelike.module_injector.accessor;

import settingdust.preloading_tricks.forgelike.UnsafeHacks;

import java.lang.module.Configuration;
import java.lang.module.ResolvedModule;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class ConfigurationAccessor {
    public static final Class<Configuration> clazz = Configuration.class;

    private static final Field graphField;
    private static final Field modulesField;
    private static final Field nameToModuleField;

    static {
        try {
            graphField = clazz.getDeclaredField("graph");
            modulesField = clazz.getDeclaredField("modules");
            nameToModuleField = clazz.getDeclaredField("nameToModule");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<ResolvedModule, Set<ResolvedModule>> getGraph(Configuration configuration) {
        return UnsafeHacks.getField(graphField, configuration);
    }

    public static void setGraph(Configuration configuration, Map<ResolvedModule, Set<ResolvedModule>> map) {
        UnsafeHacks.setField(graphField, configuration, map);
    }

    public static Set<ResolvedModule> getModules(Configuration configuration) {
        return UnsafeHacks.getField(modulesField, configuration);
    }

    public static void setModules(Configuration configuration, Set<ResolvedModule> set) {
        UnsafeHacks.setField(modulesField, configuration, set);
    }

    public static Map<String, ResolvedModule> getNameToModule(Configuration configuration) {
        return UnsafeHacks.getField(nameToModuleField, configuration);
    }

    public static void setNameToModule(Configuration configuration, Map<String, ResolvedModule> map) {
        UnsafeHacks.setField(nameToModuleField, configuration, map);
    }
}
