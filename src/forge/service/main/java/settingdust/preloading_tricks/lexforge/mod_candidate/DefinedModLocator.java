package settingdust.preloading_tricks.lexforge.mod_candidate;

import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileModLocator;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DefinedModLocator extends AbstractJarFileModLocator {
    public static final List<Path> definedCandidates = new ArrayList<>();

    @Override
    public Stream<Path> scanCandidates() {
        PreloadingTricksCallback.invoker.onCollectModCandidates();
        return definedCandidates.stream();
    }

    @Override
    public String name() {
        return PreloadingTricks.NAME + " Defined";
    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {}
}
