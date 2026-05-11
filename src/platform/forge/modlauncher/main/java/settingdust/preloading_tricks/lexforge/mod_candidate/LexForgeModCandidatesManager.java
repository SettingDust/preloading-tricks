package settingdust.preloading_tricks.lexforge.mod_candidate;

import settingdust.preloading_tricks.api.ModCandidatesManager;

import java.nio.file.Path;
import java.util.Collection;

public class LexForgeModCandidatesManager implements ModCandidatesManager {
    @Override
    public void add(final Path path) {
        DefinedModLocator.definedCandidates.add(path);
    }

    @Override
    public void addAll(final Collection<Path> paths) {
        DefinedModLocator.definedCandidates.addAll(paths);
    }
}
