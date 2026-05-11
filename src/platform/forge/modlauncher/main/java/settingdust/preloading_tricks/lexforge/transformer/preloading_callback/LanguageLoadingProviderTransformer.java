package settingdust.preloading_tricks.lexforge.transformer.preloading_callback;

import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.ModuleLayerHandler;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.stream.RStream;
import net.minecraftforge.fml.loading.LanguageLoadingProvider;
import org.slf4j.Logger;

import java.util.EnumMap;

@CTransformer(LanguageLoadingProvider.class)
public class LanguageLoadingProviderTransformer {
    @CShadow private static Logger LOGGER;

    @CInject(
        method = "loadLanguageProviders",
        target = @CTarget(
            value = "FIELD",
            target = "Lnet/minecraftforge/fml/loading/LanguageLoadingProvider;serviceLoader:Ljava/util/ServiceLoader;"
        )
    )
    private void preloading_tricks$onSetupLanguageAdapter() {
        LOGGER.info(
            "PreloadingTricks calling PreloadingTricksCallbacks.SETUP_LANGUAGE_ADAPTER in `LanguageLoadingProvider#loadLanguageProviders`");
        var moduleLayerManager = (ModuleLayerHandler) Launcher.INSTANCE.findLayerManager().orElseThrow();
        var completedLayers = RStream.of(moduleLayerManager)
                .fields()
                .by("completedLayers")
                .<EnumMap<IModuleLayerManager.Layer, Object>>get();
        var info = completedLayers.get(IModuleLayerManager.Layer.SERVICE);
        var serviceClassLoader = RStream.of(info).fields().by("cl").<ModuleClassLoader>get();

        var invokerClass = RStream.of(Classes.byName(
            "settingdust.preloading_tricks.lexforge.PreloadingTricksCallbacksInvoker",
            serviceClassLoader
        ));
        invokerClass.methods().by("onSetupLanguageAdapter").invoke();
    }
}
