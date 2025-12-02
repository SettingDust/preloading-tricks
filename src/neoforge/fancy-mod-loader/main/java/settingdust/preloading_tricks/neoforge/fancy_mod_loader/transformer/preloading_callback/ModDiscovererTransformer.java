package settingdust.preloading_tricks.neoforge.fancy_mod_loader.transformer.preloading_callback;

import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CModifyExpressionValue;
import net.lenni0451.reflect.stream.RStream;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModDiscoverer;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
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
    private List<ModFile> preloading_tricks$onSetupMods(List<ModFile> mods) throws
                                                                            ClassNotFoundException,
                                                                            NoSuchFieldException,
                                                                            IllegalAccessException,
                                                                            NoSuchMethodException,
                                                                            InvocationTargetException {
        LOGGER.info("PreloadingTricks calling PreloadingTricksCallback in `ModDiscoverer#discoverMods`");
        var currentClassLoader = FMLLoader.getCurrent().getCurrentClassLoader();
        var invokerClass = currentClassLoader.loadClass(
            "settingdust.preloading_tricks.neoforge.fancy_mod_loader.PreloadingTricksCallbacksInvoker");
        RStream.of(invokerClass).methods().by("onSetupMods").invokeArgs(mods);
        return mods;
    }
}
