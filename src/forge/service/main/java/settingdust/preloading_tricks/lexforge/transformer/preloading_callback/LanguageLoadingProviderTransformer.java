package settingdust.preloading_tricks.lexforge.transformer.preloading_callback;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.stream.RStream;
import net.minecraftforge.fml.loading.LanguageLoadingProvider;
import org.slf4j.Logger;

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
            "PreloadingTricks calling PreloadingTricksCallback#onSetupLanguageAdapter in `LanguageLoadingProvider#loadLanguageProviders`");
        var serviceLayer =
            Launcher.INSTANCE.findLayerManager()
                             .orElseThrow()
                             .getLayer(IModuleLayerManager.Layer.SERVICE)
                             .orElseThrow();

        var serviceClassLoader = serviceLayer.modules().iterator().next().getClassLoader();
        var callbackClazz = RStream.of(Classes.byName(
            "settingdust.preloading_tricks.modlauncher.PreloadingTricksCallbackHelper",
            serviceClassLoader
        ));
        var onSetupLanguageAdapterMethod = callbackClazz.methods().by("onSetupLanguageAdapter");
        onSetupLanguageAdapterMethod.invoke();
    }
}
