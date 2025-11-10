package settingdust.preloading_tricks.neoforge.modlauncher.mod_candidate;

import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import net.neoforged.neoforgespi.locating.IncompatibleFileReporting;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;

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
        PreloadingTricksCallback.invoker.onCollectModCandidates();
        pipeline.addPath(definedCandidates, ModFileDiscoveryAttributes.DEFAULT, IncompatibleFileReporting.WARN_ALWAYS);
    }
}
