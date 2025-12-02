package settingdust.preloading_tricks.neoforge.modlauncher.transformer.preloading_callback;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CModifyExpressionValue;
import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.stream.RStream;
import net.neoforged.fml.loading.moddiscovery.ModDiscoverer;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import org.slf4j.Logger;
import settingdust.preloading_tricks.neoforge.modlauncher.PreloadingTricksCallbacksInvoker;

import java.util.List;

@CTransformer(ModDiscoverer.class)
public class ModDiscovererTransformer {
    @CShadow private static Logger LOGGER;

    @CModifyExpressionValue(
        method = "discoverMods",
        target = @CTarget(
            value = "INVOKE",
            ordinal = 2,
            target = "Lnet/neoforged/fml/loading/UniqueModListBuilder$UniqueModListData;modFiles()Ljava/util/List;"
        )
    )
    private List<ModFile> preloading_tricks$onSetupMods(List<ModFile> mods) {
        LOGGER.info("PreloadingTricks calling PreloadingTricksCallback in `ModDiscoverer#discoverMods`");

        try {
            PreloadingTricksCallbacksInvoker.onSetupMods(mods);
        } catch (NoClassDefFoundError e) {
            var serviceLayer =
                Launcher.INSTANCE.findLayerManager()
                                 .orElseThrow()
                                 .getLayer(IModuleLayerManager.Layer.SERVICE)
                                 .orElseThrow();

            var serviceClassLoader = serviceLayer.modules().iterator().next().getClassLoader();

            var callbackClazz = RStream.of(Classes.byName(
                "settingdust.preloading_tricks.neoforge.modlauncher.PreloadingTricksCallbacksInvoker",
                serviceClassLoader
            ));
            callbackClazz.methods().by("onSetupMods").invokeArgs(mods);
        }
        return mods;
    }
}
