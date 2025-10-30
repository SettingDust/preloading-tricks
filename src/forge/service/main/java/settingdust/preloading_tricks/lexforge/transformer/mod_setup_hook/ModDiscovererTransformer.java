package settingdust.preloading_tricks.lexforge.transformer.mod_setup_hook;

import net.lenni0451.classtransform.annotations.CLocalVariable;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import net.minecraftforge.fml.loading.moddiscovery.ModDiscoverer;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import org.slf4j.Logger;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;
import settingdust.preloading_tricks.lexforge.LexForgeModManager;

import java.util.List;

@CTransformer(ModDiscoverer.class)
public class ModDiscovererTransformer {
    @CShadow private static Logger LOGGER;

    @CInject(
        method = "discoverMods",
        target = @CTarget(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;")
    )
    private void preloading_tricks$onSetupMods(@CLocalVariable(name = "loadedFiles") List<ModFile> loadedFiles) {
        LOGGER.info("PreloadingTricks calling SetupModCallback in `ModDiscoverer#discoverMods`");
        LexForgeModManager.mods = loadedFiles;
        PreloadingTricksCallback.invoker.onSetupMods();
        LexForgeModManager.mods = null;
    }
}
