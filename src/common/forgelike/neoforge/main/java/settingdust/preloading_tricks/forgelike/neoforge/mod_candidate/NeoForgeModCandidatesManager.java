package settingdust.preloading_tricks.forgelike.neoforge.mod_candidate;

import settingdust.preloading_tricks.api.PreloadingTricksModCandidatesManager;
import settingdust.preloading_tricks.util.LoaderPredicates;

import java.nio.file.Path;
import java.util.Collection;

public class NeoForgeModCandidatesManager implements PreloadingTricksModCandidatesManager {
    public NeoForgeModCandidatesManager() {
        if (!LoaderPredicates.NeoForgeModLauncher.strictTest() && !LoaderPredicates.NeoForge.strictTest()) {
            throw new IllegalStateException("NeoForgeModCandidatesManager won't run on wrong loader");
        }
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
