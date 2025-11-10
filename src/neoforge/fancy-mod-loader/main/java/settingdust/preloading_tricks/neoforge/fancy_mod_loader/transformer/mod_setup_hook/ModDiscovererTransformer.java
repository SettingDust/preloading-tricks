package settingdust.preloading_tricks.neoforge.fancy_mod_loader.transformer.mod_setup_hook;

import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CModifyExpressionValue;
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
        var managerClazz = currentClassLoader.loadClass(
            "settingdust.preloading_tricks.neoforge.fancy_mod_loader.NeoForgeModManager");
        var modsField = managerClazz.getDeclaredField("mods");
        modsField.set(null, mods);
        var callbackClazz = currentClassLoader.loadClass("settingdust.preloading_tricks.api.PreloadingTricksCallback");
        var setupModsMethod = callbackClazz.getDeclaredMethod("onSetupMods");
        var invoker = callbackClazz.getDeclaredField("invoker");
        setupModsMethod.invoke(invoker.get(null));
        modsField.set(null, null);
        return mods;
    }
}
