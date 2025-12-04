package settingdust.preloading_tricks.lexforge.mod_candidate;

import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileModLocator;
import net.minecraftforge.forgespi.locating.IModFile;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallbacks;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DefinedModLocator extends AbstractJarFileModLocator {
    public static final List<Path> definedCandidates = new ArrayList<>();

    public static DefinedModLocator INSTANCE;

    public DefinedModLocator() {
        INSTANCE = this;
    }

    @Override
    protected String getDefaultJarModType() {
        return IModFile.Type.GAMELIBRARY.name();
    }

    @Override
    public Stream<Path> scanCandidates() {
        PreloadingTricksCallbacks.COLLECT_MOD_CANDIDATES
            .getInvoker()
            .onCollectModCandidates(new LexForgeModCandidatesManager());
        PreloadingTricks.LOGGER.info("Loading {} additional mods", definedCandidates.size());
        return definedCandidates.stream();
    }

    @Override
    public String name() {
        return PreloadingTricks.NAME + " Defined";
    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {}
}
