package settingdust.preloading_tricks.neoforge.modlauncher.transformer.preloading_callback;

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
import net.neoforged.fml.loading.moddiscovery.ModDiscoverer;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import settingdust.preloading_tricks.neoforge.modlauncher.PreloadingTricksCallbacksInvoker;
import settingdust.preloading_tricks.neoforge.modlauncher.mod_candidate.NeoForgeAdditionalDependencySourceManager;

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

    @CInject(
        method = "discoverMods",
        target = @CTarget(
            value = "FIELD",
            target = "Lnet/neoforged/fml/loading/moddiscovery/ModDiscoverer;dependencyLocators:Ljava/util/List;"
        )
    )
    private void preloading_tricks$logAdditionalDependencySources(
        @CShared("additionalDependencySources") List<ModFile> additionalDependencySources
    ) {
        try {
            additionalDependencySources = NeoForgeAdditionalDependencySourceManager.additionalDependencySources;
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
            additionalDependencySources = callbackClazz.methods().by("onCollectAdditionalDependencySources").invoke();
        }
        LOGGER.info("PreloadingTricks adding additional {} dependency sources", additionalDependencySources.size());
    }

    @CModifyExpressionValue(
        method = "discoverMods",
        target = @CTarget(
            value = "INVOKE",
            target = "Ljava/util/List;copyOf(Ljava/util/Collection;)Ljava/util/List;"
        )
    )
    private List<ModFile> preloading_tricks$appendAdditionalDependencySources(
        List<ModFile> mods,
        @CShared("additionalDependencySources") List<ModFile> additionalDependencySources
    ) {
        return List.copyOf(Lists.newArrayList(Iterables.concat(mods, additionalDependencySources).iterator()));
    }
}
