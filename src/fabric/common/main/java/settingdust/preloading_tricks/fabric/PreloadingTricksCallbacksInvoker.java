package settingdust.preloading_tricks.fabric;

import settingdust.preloading_tricks.api.PreloadingTricksCallbacks;
import settingdust.preloading_tricks.fabric.mod_candidate.FabricModCandidatesManager;

public class PreloadingTricksCallbacksInvoker {
    public static void onSetupMods() {
        PreloadingTricksCallbacks.SETUP_MODS.getInvoker().onSetupMods(new FabricModManager());
    }

    public static void onSetupLanguageAdapter() {
        PreloadingTricksCallbacks.SETUP_LANGUAGE_ADAPTER.getInvoker().onSetupLanguageAdapter();
    }

    public static void onCollectModCandidates() {
        PreloadingTricksCallbacks.COLLECT_MOD_CANDIDATES
            .getInvoker()
            .onCollectModCandidates(new FabricModCandidatesManager());
    }
}
