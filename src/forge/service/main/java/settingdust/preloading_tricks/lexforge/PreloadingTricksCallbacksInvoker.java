package settingdust.preloading_tricks.lexforge;

import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import settingdust.preloading_tricks.api.PreloadingTricksCallbacks;
import settingdust.preloading_tricks.api.modlauncher.ModLauncherPreloadingCallbacks;
import settingdust.preloading_tricks.lexforge.mod_candidate.LexForgeAdditionalDependencySourceManager;

import java.util.List;

public class PreloadingTricksCallbacksInvoker {
    public static void onSetupMods(List<ModFile> mods) {
        PreloadingTricksCallbacks.SETUP_MODS.getInvoker().onSetupMods(new LexForgeModManager(mods));
    }

    public static void onSetupLanguageAdapter() {
        PreloadingTricksCallbacks.SETUP_LANGUAGE_ADAPTER.getInvoker().onSetupLanguageAdapter();
    }

    public static List<ModFile> onCollectAdditionalDependencySources() {
        ModLauncherPreloadingCallbacks.COLLECT_ADDITIONAL_DEPENDENCY_SOURCES
            .getInvoker()
            .onCollectAdditionalDependencySources(new LexForgeAdditionalDependencySourceManager());
        return LexForgeAdditionalDependencySourceManager.additionalDependencySources;
    }
}
