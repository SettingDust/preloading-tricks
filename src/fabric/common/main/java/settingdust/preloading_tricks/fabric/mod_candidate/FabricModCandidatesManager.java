package settingdust.preloading_tricks.fabric.mod_candidate;

import settingdust.preloading_tricks.api.PreloadingTricksModCandidatesManager;

import java.nio.file.Path;
import java.util.Collection;

public class FabricModCandidatesManager implements PreloadingTricksModCandidatesManager {
    @Override
    public void add(final Path path) {
        DefinedModCandidateFinder.definedCandidates.add(path);
    }

    @Override
    public void addAll(final Collection<Path> paths) {
        DefinedModCandidateFinder.definedCandidates.addAll(paths);
    }
}
