package settingdust.preloading_tricks.fabric.transformer;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.util.log.Log;
import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;
import settingdust.preloading_tricks.fabric.PreloadingTricksLanguageAdapterEntrypoint;

import java.util.List;

@CTransformer(FabricLoaderImpl.class)
public class HookModSetupTransformer {
    @CShadow
    private List<ModContainerImpl> mods;

    @CInject(method = "setupMods", target = @CTarget(value = "HEAD"))
    @CInline
    private void preloading_tricks$onSetupMods() {
        Log.info(
            PreloadingTricksLanguageAdapterEntrypoint.LOG_CATEGORY,
            "PreloadingTricks calling SetupModsCallback in `FabricLoaderImpl#setupMods`"
        );
        PreloadingTricksCallback.invoker.onSetupMods();
    }
}
