package settingdust.preloading_tricks.fabric.mod_candidate;

import net.fabricmc.loader.impl.discovery.ClasspathModCandidateFinder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DefinedModCandidateFinder extends ClasspathModCandidateFinder {
    public static final List<Path> definedCandidates = new ArrayList<>();
    private final boolean requiresRemap;

    public DefinedModCandidateFinder(final boolean requiresRemap) {
        this.requiresRemap = requiresRemap;
    }

    @Override
    public void findCandidates(ModCandidateConsumer out) {
        for (final var definedCandidate : definedCandidates) {
            out.accept(List.of(definedCandidate), requiresRemap);
        }
    }
}
