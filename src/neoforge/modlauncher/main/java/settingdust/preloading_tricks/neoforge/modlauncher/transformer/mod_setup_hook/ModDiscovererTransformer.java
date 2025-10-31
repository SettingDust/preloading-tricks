package settingdust.preloading_tricks.neoforge.modlauncher.transformer.mod_setup_hook;

import net.lenni0451.classtransform.annotations.CLocalVariable;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import net.neoforged.fml.loading.moddiscovery.ModDiscoverer;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import org.slf4j.Logger;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;
import settingdust.preloading_tricks.neoforge.modlauncher.NeoForgeModManager;

import java.util.List;

@CTransformer(ModDiscoverer.class)
public class ModDiscovererTransformer {
    @CShadow private static Logger LOGGER;

    @CInject(
        method = "discoverMods",
        target = @CTarget(value = "INVOKE", target = "Ljava/util/Collections;emptyMap()Ljava/util/Map;")
    )
    private void preloading_tricks$onSetupMods(@CLocalVariable(name = "loadedFiles") List<ModFile> loadedFiles) {
        LOGGER.info("PreloadingTricks calling SetupModCallback in `ModDiscoverer#discoverMods`");
        NeoForgeModManager.mods = loadedFiles;
        PreloadingTricksCallback.invoker.onSetupMods();
        NeoForgeModManager.mods = null;
    }
}
