package settingdust.preloading_tricks.lexforge;

import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import settingdust.preloading_tricks.api.PreloadingTricksCallbacks;
import settingdust.preloading_tricks.lexforge.mod_candidate.LexForgeModCandidatesManager;

import java.util.List;

public class PreloadingTricksCallbacksInvoker {
    public static void onSetupMods(List<ModFile> mods) {
        PreloadingTricksCallbacks.SETUP_MODS.getInvoker().onSetupMods(new LexForgeModManager(mods));
    }

    public static void onSetupLanguageAdapter() {
        PreloadingTricksCallbacks.SETUP_LANGUAGE_ADAPTER.getInvoker().onSetupLanguageAdapter();
    }

    public static void onCollectModCandidates() {
        PreloadingTricksCallbacks.COLLECT_MOD_CANDIDATES
            .getInvoker()
            .onCollectModCandidates(new LexForgeModCandidatesManager());
    }
}
