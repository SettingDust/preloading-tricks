package settingdust.preloading_tricks.modlauncher;

import cpw.mods.modlauncher.api.IModuleLayerManager;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.ModuleLayerHandlerAccessor;

@SuppressWarnings("unused")
public class PreloadingTricksCallbackHelper {
    public static void onSetupLanguageAdapter() {
        var pluginClassLoader = ModuleLayerHandlerAccessor.getModuleClassLoader(IModuleLayerManager.Layer.PLUGIN);
        PreloadingTricksCallback.CLASS_LOADERS.add(pluginClassLoader);
        PreloadingTricksCallback.invoker.onSetupLanguageAdapter();
    }
}
