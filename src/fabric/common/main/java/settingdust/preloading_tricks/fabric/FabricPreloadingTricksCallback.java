package settingdust.preloading_tricks.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.metadata.ModOrigin;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.discovery.*;
import net.fabricmc.loader.impl.gui.FabricGuiEntry;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.metadata.DependencyOverrides;
import net.fabricmc.loader.impl.metadata.VersionOverrides;
import net.fabricmc.loader.impl.util.SystemProperties;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;
import settingdust.preloading_tricks.api.PreloadingTricksModManager;
import settingdust.preloading_tricks.fabric.mod_candidate.DefinedModCandidateFinder;
import settingdust.preloading_tricks.fabric.mod_candidate.ModContainerModCandidateFinder;
import settingdust.preloading_tricks.fabric.util.FabricLoaderImplAccessor;
import settingdust.preloading_tricks.util.LoaderPredicates;

import java.io.IOException;
import java.util.*;

public class FabricPreloadingTricksCallback implements PreloadingTricksCallback {
    public FabricPreloadingTricksCallback() {
        LoaderPredicates.Fabric.throwIfNot();
    }

    @Override
    public void onSetupMods() {
        var service = PreloadingTricksModManager.<FabricModManager>get();

        var remapRegularMods = FabricLoader.getInstance().isDevelopmentEnvironment();
        var versionOverrides = new VersionOverrides();
        var dependencyOverrides = new DependencyOverrides(FabricLoaderImpl.INSTANCE.getConfigDir());

        var discoverer = new ModDiscoverer(versionOverrides, dependencyOverrides);
        discoverer.addCandidateFinder(new DefinedModCandidateFinder(remapRegularMods));
        discoverer.addCandidateFinder(new ModContainerModCandidateFinder(service.all()));

        PreloadingTricksCallback.invoker.onCollectModCandidates();

        var envDisabledMods = new HashMap<String, Set<ModCandidateImpl>>();

        try {
            var modCandidates = discoverer.discoverMods(FabricLoaderImpl.INSTANCE, envDisabledMods);

            modCandidates = ModResolver.resolve(
                modCandidates,
                FabricLoader.getInstance().getEnvironmentType(),
                envDisabledMods
            );

            var idToCandidates = new HashMap<String, ModCandidateImpl>();
            for (final var candidate : modCandidates) {
                idToCandidates.put(candidate.getId(), candidate);
                for (final var provide : candidate.getProvides()) {
                    idToCandidates.put(provide, candidate);
                }
            }

            var iterator = FabricLoaderImplAccessor.modMap().entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                var id = entry.getKey();
                var modContainer = entry.getValue();
                var origin = modContainer.getOrigin();
                var candidate = idToCandidates.get(id);
                var exists = origin.getKind() == ModOrigin.Kind.PATH
                             && Objects.equals(origin.getPaths(), candidate.getOriginPaths());
                if (!exists) {
                    var candidateParentMods = candidate.getParentMods();
                    if (!candidateParentMods.isEmpty()) {
                        var parentMod = candidateParentMods.iterator().next();
                        exists = origin.getKind() == ModOrigin.Kind.NESTED
                                 && Objects.equals(origin.getParentModId(), parentMod.getId())
                                 && Objects.equals(origin.getParentSubLocation(), candidate.getLocalPath());
                    }
                }
                if (exists) {
                    idToCandidates.remove(id);
                } else {
                    iterator.remove();
                    service.remove(modContainer);
                }
            }

            dumpModList(idToCandidates.values());
            FabricLoaderImpl.INSTANCE.dumpNonFabricMods(discoverer.getNonFabricMods());

            var cacheDir = FabricLoader.getInstance().getGameDir().resolve(FabricLoaderImpl.CACHE_DIR_NAME);
            var outputDir = cacheDir.resolve("processedMods");

            if (remapRegularMods) {
                if (System.getProperty(SystemProperties.REMAP_CLASSPATH_FILE) == null) {
                    PreloadingTricks.LOGGER.warn(
                        "Runtime mod remapping disabled due to no fabric.remapClasspathFile being specified. You may need to update loom.");
                } else {
                    RuntimeModRemapper.remap(idToCandidates.values(), cacheDir.resolve("tmp"), outputDir);
                }
            }

            for (final var mod : idToCandidates.values()) {
                if (!mod.hasPath() && !mod.isBuiltin()) {
                    try {
                        mod.setPaths(Collections.singletonList(mod.copyToDir(outputDir, false)));
                    } catch (IOException e) {
                        throw new RuntimeException("Error extracting mod " + mod, e);
                    }
                }

                var container = new ModContainerImpl(mod);
                service.add(container);
                for (final var path : mod.getPaths()) {
                    // This may add two different version of mod to the classpath.
                    // Should remove the old path in the loop before.
                    // But it's difficult.
                    // So I leave this here.
                    // I believe the KnotClassLoader is smart enough to pick the correct class!
                    FabricLauncherBase.getLauncher().addToClassPath(path);
                }
                setupLanguageAdapter(container);
            }
        } catch (ModResolutionException e) {
            FabricGuiEntry.displayCriticalError(e, true);
        }
    }

    public static void dumpModList(Collection<ModCandidateImpl> mods) {
        StringBuilder modListText = new StringBuilder();

        boolean[] lastItemOfNestLevel = new boolean[mods.size()];
        List<ModCandidateImpl> topLevelMods = mods.stream()
                                                  .filter(mod -> mod.getParentMods().isEmpty())
                                                  .toList();
        int topLevelModsCount = topLevelMods.size();

        for (int i = 0; i < topLevelModsCount; i++) {
            boolean lastItem = i == topLevelModsCount - 1;

            if (lastItem) lastItemOfNestLevel[0] = true;

            dumpModList0(topLevelMods.get(i), modListText, 0, lastItemOfNestLevel);
        }

        int modsCount = mods.size();
        PreloadingTricks.LOGGER.info(
            "Loading {} additional mod{}:\n{}",
            modsCount,
            modsCount != 1 ? "s" : "",
            modListText
        );
    }

    private static void dumpModList0(
        ModCandidateImpl mod,
        StringBuilder log,
        int nestLevel,
        boolean[] lastItemOfNestLevel
    ) {
        if (!log.isEmpty()) log.append('\n');

        for (int depth = 0; depth < nestLevel; depth++) {
            log.append(depth == 0 ? "\t" : lastItemOfNestLevel[depth] ? "     " : "   | ");
        }

        log.append(nestLevel == 0 ? "\t" : "  ");
        log.append(nestLevel == 0 ? "-" : lastItemOfNestLevel[nestLevel] ? " \\--" : " |--");
        log.append(' ');
        log.append(mod.getId());
        log.append(' ');
        log.append(mod.getVersion().getFriendlyString());

        List<ModCandidateImpl> nestedMods = new ArrayList<>(mod.getNestedMods());
        nestedMods.sort(Comparator.comparing(nestedMod -> nestedMod.getMetadata().getId()));

        if (!nestedMods.isEmpty()) {
            Iterator<ModCandidateImpl> iterator = nestedMods.iterator();
            ModCandidateImpl nestedMod;
            boolean lastItem;

            while (iterator.hasNext()) {
                nestedMod = iterator.next();
                lastItem = !iterator.hasNext();

                if (lastItem) lastItemOfNestLevel[nestLevel + 1] = true;

                dumpModList0(nestedMod, log, nestLevel + 1, lastItemOfNestLevel);

                if (lastItem) lastItemOfNestLevel[nestLevel + 1] = false;
            }
        }
    }

    private void setupLanguageAdapter(ModContainerImpl mod) {
        var adapterMap = FabricLoaderImplAccessor.adapterMap();
        // add language adapters
        for (var laEntry : mod.getMetadata().getLanguageAdapterDefinitions().entrySet()) {
            if (adapterMap.containsKey(laEntry.getKey())) {
                throw new RuntimeException(
                    "Duplicate language adapter key: " + laEntry.getKey() + "! (" + laEntry.getValue() + ", " +
                    adapterMap.get(laEntry.getKey()).getClass().getName() + ")");
            }

            try {
                adapterMap.put(
                    laEntry.getKey(), (LanguageAdapter) Class.forName(
                        laEntry.getValue(),
                        true,
                        FabricLauncherBase.getLauncher().getTargetClassLoader()
                    ).getDeclaredConstructor().newInstance()
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate language adapter: " + laEntry.getKey(), e);
            }
        }
    }
}
