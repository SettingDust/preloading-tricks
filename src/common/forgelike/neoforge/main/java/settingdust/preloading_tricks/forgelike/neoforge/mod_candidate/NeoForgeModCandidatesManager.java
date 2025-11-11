package settingdust.preloading_tricks.forgelike.neoforge.mod_candidate;

import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import settingdust.preloading_tricks.api.PreloadingTricksModCandidatesManager;

import java.nio.file.Path;
import java.util.Collection;

public class NeoForgeModCandidatesManager implements PreloadingTricksModCandidatesManager {
    public NeoForgeModCandidatesManager() {
        IModFileCandidateLocator.class.getSimpleName();
    }

    @Override
    public void add(final Path path) {
        DefinedCandidateLocator.definedCandidates.add(path);
    }

    @Override
    public void addAll(final Collection<Path> paths) {
        DefinedCandidateLocator.definedCandidates.addAll(paths);
    }
}
