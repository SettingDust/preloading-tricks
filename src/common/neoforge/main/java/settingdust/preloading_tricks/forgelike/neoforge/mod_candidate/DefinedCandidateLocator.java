package settingdust.preloading_tricks.forgelike.neoforge.mod_candidate;

import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import net.neoforged.neoforgespi.locating.IncompatibleFileReporting;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallbacks;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DefinedCandidateLocator implements IModFileCandidateLocator {
    public static final List<Path> definedCandidates = new ArrayList<>();

    @Override
    public String toString() {
        return "{" + PreloadingTricks.NAME + " defined}";
    }

    @Override
    public void findCandidates(final ILaunchContext context, final IDiscoveryPipeline pipeline) {
        PreloadingTricksCallbacks.COLLECT_MOD_CANDIDATES
            .getInvoker()
            .onCollectModCandidates(new NeoForgeModCandidatesManager());
        PreloadingTricks.LOGGER.info("Loading {} additional mods", definedCandidates.size());
        for (final var candidate : definedCandidates) {
            pipeline.addPath(candidate, ModFileDiscoveryAttributes.DEFAULT, IncompatibleFileReporting.WARN_ALWAYS);
        }
    }
}
