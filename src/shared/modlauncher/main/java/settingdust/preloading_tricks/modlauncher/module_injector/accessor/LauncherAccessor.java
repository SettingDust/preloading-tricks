package settingdust.preloading_tricks.modlauncher.module_injector.accessor;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IModuleLayerManager;

import java.lang.reflect.Field;

public class LauncherAccessor {
    public static final Class<Launcher> clazz = Launcher.class;
    private static final Field transformationServiceField;

    static {
        try {
            transformationServiceField = clazz.getDeclaredField("transformationServicesHandler");
            transformationServiceField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static ModuleLayer getModuleLayer(IModuleLayerManager.Layer layer) {
        return Launcher.INSTANCE.findLayerManager().flatMap(it -> it.getLayer(layer)).orElseThrow();
    }

    public static Object getTransformationServicesHandler() {
        try {
            return transformationServiceField.get(Launcher.INSTANCE);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
