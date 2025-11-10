package settingdust.preloading_tricks.neoforge.modlauncher.mod_candidate;

import settingdust.preloading_tricks.api.PreloadingTricksModCandidatesManager;

import java.nio.file.Path;
import java.util.Collection;

public class NeoForgeModCandidatesManager implements PreloadingTricksModCandidatesManager {
    @Override
    public void add(final Path path) {
        DefinedCandidateLocator.definedCandidates.add(path);
    }

    @Override
    public void addAll(final Collection<Path> paths) {
        DefinedCandidateLocator.definedCandidates.addAll(paths);
    }
}
