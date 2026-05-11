package settingdust.preloading_tricks.forgelike.module_injector.accessor;

import settingdust.preloading_tricks.forgelike.JavaBypass;

import java.lang.invoke.VarHandle;
import java.util.Map;
import java.util.Set;

public class ModuleLayerAccessor {
    public static final Class<ModuleLayer> clazz = ModuleLayer.class;

    private static final VarHandle nameToModuleField;
    private static final VarHandle modulesField;


    static {
        try {
            var lookup = JavaBypass.TRUSTED_LOOKUP.in(clazz);
            nameToModuleField = lookup.findVarHandle(clazz, "nameToModule", Map.class);
            modulesField = lookup.findVarHandle(clazz, "modules", Set.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Module> getNameToModule(ModuleLayer moduleLayer) {
        return (Map<String, Module>) nameToModuleField.get(moduleLayer);
    }

    public static void clearModules(ModuleLayer moduleLayer) {
        modulesField.set(moduleLayer, null);
    }
}
