package settingdust.preloading_tricks.neoforge.fancy_mod_loader.transformer.preloading_callback;

import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.stream.RStream;
import net.neoforged.fml.loading.LanguageProviderLoader;
import org.slf4j.Logger;

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
            "PreloadingTricks calling PreloadingTricksCallbacks.SETUP_LANGUAGE_ADAPTER in `LanguageLoadingProvider#<init>`");
        var callbackClass = Classes.byName("settingdust.preloading_tricks.neoforge.fancy_mod_loader.PreloadingTricksCallbacksInvoker");
        var stream = RStream.of(callbackClass);
        stream.methods().by("onSetupLanguageAdapter").invoke();
    }
}
