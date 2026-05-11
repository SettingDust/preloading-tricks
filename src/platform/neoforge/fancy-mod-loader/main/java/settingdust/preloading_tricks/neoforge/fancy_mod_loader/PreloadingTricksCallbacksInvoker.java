package settingdust.preloading_tricks.neoforge.fancy_mod_loader;

import net.neoforged.fml.loading.moddiscovery.ModFile;
import settingdust.preloading_tricks.api.PreloadingTricksCallbacks;

import java.util.List;

public class PreloadingTricksCallbacksInvoker {
    public static void onSetupMods(List<ModFile> mods) {
        PreloadingTricksCallbacks.SETUP_MODS.getInvoker().onSetupMods(new NeoForgeModManager(mods));
    }

    public static void onSetupLanguageAdapter() {
        PreloadingTricksCallbacks.SETUP_LANGUAGE_ADAPTER.getInvoker().onSetupLanguageAdapter();
    }
}
