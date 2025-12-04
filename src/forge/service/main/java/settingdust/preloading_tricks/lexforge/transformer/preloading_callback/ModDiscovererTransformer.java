package settingdust.preloading_tricks.lexforge.transformer.preloading_callback;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CShared;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import net.lenni0451.classtransform.annotations.injection.CModifyExpressionValue;
import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.stream.RStream;
import net.minecraftforge.fml.loading.moddiscovery.ModDiscoverer;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import org.slf4j.Logger;

import java.util.List;

@CTransformer(ModDiscoverer.class)
public class ModDiscovererTransformer {
    @CShadow private static Logger LOGGER;

    @CModifyExpressionValue(
        method = "discoverMods",
        target = @CTarget(
            value = "INVOKE",
            ordinal = 2,
            target = "Lnet/minecraftforge/fml/loading/UniqueModListBuilder$UniqueModListData;modFiles()Ljava/util/List;"
        )
    )
    private List<ModFile> preloading_tricks$onSetupMods(List<ModFile> mods) {
        LOGGER.info("PreloadingTricks calling PreloadingTricksCallbacks.SETUP_MODS in `ModDiscoverer#discoverMods`");
        var serviceLayer =
            Launcher.INSTANCE.findLayerManager()
                             .orElseThrow()
                             .getLayer(IModuleLayerManager.Layer.SERVICE)
                             .orElseThrow();

        var serviceClassLoader = serviceLayer.modules().iterator().next().getClassLoader();

        var callbackClazz = RStream.of(Classes.byName(
            "settingdust.preloading_tricks.lexforge.PreloadingTricksCallbacksInvoker",
            serviceClassLoader
        ));
        callbackClazz.methods().by("onSetupMods").invokeArgs(mods);
        return mods;
    }

    @CInject(
        method = "discoverMods",
        target = @CTarget(
            value = "FIELD",
            target = "Lnet/minecraftforge/fml/loading/moddiscovery/ModDiscoverer;dependencyLocatorList:Ljava/util/List;"
        )
    )
    private void preloading_tricks$logAdditionalDependencySources(@CShared("additionalDependencySources") List<ModFile> additionalDependencySources) {
        var serviceLayer =
            Launcher.INSTANCE.findLayerManager()
                             .orElseThrow()
                             .getLayer(IModuleLayerManager.Layer.SERVICE)
                             .orElseThrow();

        var serviceClassLoader = serviceLayer.modules().iterator().next().getClassLoader();
        var callbackClazz = RStream.of(Classes.byName(
            "settingdust.preloading_tricks.lexforge.PreloadingTricksCallbacksInvoker",
            serviceClassLoader
        ));
        additionalDependencySources = callbackClazz.methods().by("onCollectAdditionalDependencySources").invoke();
        LOGGER.info("PreloadingTricks adding {} additional dependency sources", additionalDependencySources.size());
    }

    @CModifyExpressionValue(
        method = "discoverMods",
        target = @CTarget(
            value = "INVOKE",
            target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;"
        )
    )
    private ImmutableList<ModFile> preloading_tricks$appendAdditionalDependencySources(
        ImmutableList<ModFile> mods,
        @CShared("additionalDependencySources") List<ModFile> additionalDependencySources
    ) {
        return ImmutableList.copyOf(Iterables.concat(mods, additionalDependencySources));
    }
}
