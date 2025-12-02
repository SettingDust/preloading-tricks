package settingdust.preloading_tricks.forgelike.neoforge.mod_candidate;

import settingdust.preloading_tricks.api.ModCandidatesManager;
import settingdust.preloading_tricks.util.LoaderPredicates;

import java.nio.file.Path;
import java.util.Collection;

public class NeoForgeModCandidatesManager implements ModCandidatesManager {
    public NeoForgeModCandidatesManager() {
        if (!LoaderPredicates.NeoForgeModLauncher.test() && !LoaderPredicates.NeoForge.test()) {
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
