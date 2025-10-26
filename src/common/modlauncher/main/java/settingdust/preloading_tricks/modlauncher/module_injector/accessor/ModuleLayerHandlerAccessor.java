package settingdust.preloading_tricks.modlauncher.module_injector.accessor;

import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.ModuleLayerHandler;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import settingdust.preloading_tricks.forgelike.UnsafeHacks;

import java.lang.reflect.Field;
import java.util.EnumMap;

public class ModuleLayerHandlerAccessor {
    public static final Class<ModuleLayerHandler> clazz = ModuleLayerHandler.class;

    private static final Field completedLayersField;

    static {
        try {
            completedLayersField = clazz.getDeclaredField("completedLayers");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static EnumMap<IModuleLayerManager.Layer, Object> getCompletedLayers(ModuleLayerHandler moduleLayerHandler) {
        return UnsafeHacks.getField(completedLayersField, moduleLayerHandler);
    }

    public static ModuleClassLoader getModuleClassLoader(IModuleLayerManager.Layer layer) {
        var completedLayers = ModuleLayerHandlerAccessor
            .getCompletedLayers((ModuleLayerHandler) Launcher.INSTANCE.findLayerManager().orElseThrow());
        var layerInfo = completedLayers.get(layer);
        var classLoader = LayerInfoAccessor.getModuleClassLoader(layerInfo);
        return classLoader;
    }
}
