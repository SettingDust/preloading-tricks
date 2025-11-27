package settingdust.preloading_tricks.neoforge.modlauncher.transformer.preloading_callback;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.stream.RStream;
import net.neoforged.fml.loading.LanguageProviderLoader;
import org.slf4j.Logger;
import settingdust.preloading_tricks.modlauncher.PreloadingTricksCallbackHelper;

@CTransformer(LanguageProviderLoader.class)
public class LanguageProviderLoaderTransformer {
    @CShadow private static Logger LOGGER;

    @CInject(method = "<init>", target = @CTarget(value = "HEAD"))
    private void preloading_tricks$onSetupLanguageAdapter() {
        LOGGER.info(
            "PreloadingTricks calling PreloadingTricksCallback#onSetupLanguageAdapter in `LanguageLoadingProvider#<init>`");
        try {
            PreloadingTricksCallbackHelper.onSetupLanguageAdapter();
        }  catch (NoClassDefFoundError e) {
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
}
