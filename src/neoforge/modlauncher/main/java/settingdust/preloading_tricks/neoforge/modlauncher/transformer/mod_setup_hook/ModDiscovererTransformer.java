package settingdust.preloading_tricks.neoforge.modlauncher.transformer.mod_setup_hook;

import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CModifyExpressionValue;
import net.neoforged.fml.loading.moddiscovery.ModDiscoverer;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import org.slf4j.Logger;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;
import settingdust.preloading_tricks.neoforge.modlauncher.NeoForgeModManager;

import java.util.List;

@CTransformer(ModDiscoverer.class)
public class ModDiscovererTransformer {
    @CShadow private static Logger LOGGER;

    @CModifyExpressionValue(
        method = "discoverMods",
        target = @CTarget(
            value = "INVOKE",
            ordinal = 2,
            target = "Lnet/neoforged/fml/loading/UniqueModListBuilder$UniqueModListData;modFiles()Ljava/util/List;"
        )
    )
    private List<ModFile> preloading_tricks$onSetupMods(List<ModFile> mods) {
        LOGGER.info("PreloadingTricks calling SetupModCallback in `ModDiscoverer#discoverMods`");
        NeoForgeModManager.mods = mods;
        PreloadingTricksCallback.invoker.onSetupMods();
        NeoForgeModManager.mods = null;
        return mods;
    }
}
