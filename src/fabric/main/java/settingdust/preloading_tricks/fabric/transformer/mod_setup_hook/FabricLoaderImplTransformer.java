package settingdust.preloading_tricks.fabric.transformer.mod_setup_hook;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@CTransformer(FabricLoaderImpl.class)
public class FabricLoaderImplTransformer {
    @CShadow
    private List<ModContainerImpl> mods;

    @CInject(method = "setupMods", target = @CTarget(value = "HEAD"))
    @CInline
    private void preloading_tricks$onSetupMods()
        throws
        ClassNotFoundException,
        IllegalAccessException,
        NoSuchMethodException,
        InvocationTargetException {
        Log.info(
            LogCategory.createCustom("PreloadingTricks"),
            "PreloadingTricks calling PreloadingTricksCallbacks.SETUP_MODS in `FabricLoaderImpl#setupMods`"
        );
        var knotClassLoader = FabricLauncherBase.getLauncher().getTargetClassLoader();
        var invokerClass = Class.forName(
            "settingdust.preloading_tricks.fabric.PreloadingTricksCallbacksInvoker",
            true,
            knotClassLoader
        );
        invokerClass.getDeclaredMethod("onSetupMods").invoke(null);
    }
}
