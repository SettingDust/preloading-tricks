package settingdust.preloading_tricks.forgelike.module_injector.accessor;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IModuleLayerManager;

public class LauncherAccessor {

    public static ModuleLayer getModuleLayer(IModuleLayerManager.Layer layer) {
        return Launcher.INSTANCE.findLayerManager().flatMap(it -> it.getLayer(layer)).orElseThrow();
    }
}
