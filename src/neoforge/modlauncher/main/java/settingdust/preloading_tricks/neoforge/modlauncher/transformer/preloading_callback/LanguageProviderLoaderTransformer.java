package settingdust.preloading_tricks.neoforge.modlauncher.transformer.preloading_callback;

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
import net.neoforged.fml.loading.LanguageProviderLoader;
import org.slf4j.Logger;
import settingdust.preloading_tricks.neoforge.modlauncher.PreloadingTricksCallbacksInvoker;

import java.util.EnumMap;

@CTransformer(LanguageProviderLoader.class)
public class LanguageProviderLoaderTransformer {
    @CShadow private static Logger LOGGER;

    @CInject(
        method = "<init>",
        target = @CTarget(
            value = "INVOKE",
            target = "Lnet/neoforged/fml/util/ServiceLoaderUtil;loadServices(Lnet/neoforged/neoforgespi/ILaunchContext;Ljava/lang/Class;)Ljava/util/List;"
        )
    )
    private void preloading_tricks$onSetupLanguageAdapter() {
        LOGGER.info(
            "PreloadingTricks calling PreloadingTricksCallback#onSetupLanguageAdapter in `LanguageLoadingProvider#<init>`");
        try {
            PreloadingTricksCallbacksInvoker.onSetupLanguageAdapter();
        } catch (NoClassDefFoundError e) {
            var moduleLayerManager = (ModuleLayerHandler) Launcher.INSTANCE.findLayerManager().orElseThrow();
            var completedLayers = RStream.of(moduleLayerManager)
                    .fields()
                    .by("completedLayers")
                    .<EnumMap<IModuleLayerManager.Layer, Object>>get();
            var info = completedLayers.get(IModuleLayerManager.Layer.SERVICE);
            var serviceClassLoader = RStream.of(info).fields().by("cl").<ModuleClassLoader>get();

            var invokerClass = RStream.of(Classes.byName(
                "settingdust.preloading_tricks.neoforge.modlauncher.PreloadingTricksCallbacksInvoker",
                serviceClassLoader
            ));
            invokerClass.methods().by("onSetupLanguageAdapter").invoke();
        }
    }
}
