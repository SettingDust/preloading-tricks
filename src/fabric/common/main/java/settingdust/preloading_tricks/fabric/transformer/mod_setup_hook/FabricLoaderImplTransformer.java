package settingdust.preloading_tricks.fabric.transformer.mod_setup_hook;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.util.log.Log;
import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import net.lenni0451.reflect.stream.RStream;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;
import settingdust.preloading_tricks.fabric.PreloadingTricksLanguageAdapterEntrypoint;

import java.util.List;

@CTransformer(FabricLoaderImpl.class)
public class FabricLoaderImplTransformer {
    @CShadow
    private List<ModContainerImpl> mods;

    @CInject(method = "setupMods", target = @CTarget(value = "HEAD"))
    @CInline
    private void preloading_tricks$onSetupMods() throws ClassNotFoundException {
        Log.info(
            PreloadingTricksLanguageAdapterEntrypoint.LOG_CATEGORY,
            "PreloadingTricks calling SetupModsCallback in `FabricLoaderImpl#setupMods`"
        );
        var knotClassLoader = FabricLauncherBase.getLauncher().getTargetClassLoader();
        @SuppressWarnings("unchecked") var callbackClass = (Class<PreloadingTricksCallback>) Class.forName(
            "settingdust.preloading_tricks.api.PreloadingTricksCallback",
            true,
            knotClassLoader
        );
        var stream = RStream.of(callbackClass);
        var invoker = stream.fields().by("invoker").get(null);
        stream.methods().by("onSetupMods").invokeInstance(invoker);
    }
}
