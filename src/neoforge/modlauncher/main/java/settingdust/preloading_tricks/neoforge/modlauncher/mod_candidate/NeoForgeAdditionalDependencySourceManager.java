package settingdust.preloading_tricks.neoforge.modlauncher.mod_candidate;

import cpw.mods.jarhandling.SecureJar;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import settingdust.preloading_tricks.modlauncher.AdditionalDependencySourceManager;
import settingdust.preloading_tricks.neoforge.modlauncher.virtual_mod.VirtualModFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NeoForgeAdditionalDependencySourceManager implements AdditionalDependencySourceManager {
    public static final List<ModFile> additionalDependencySources = new ArrayList<>();

    @Override
    public void add(final Path path, final String name) {
        additionalDependencySources.add(new VirtualModFile(name, SecureJar.from(path)));
    }

    @Override
    public void addAll(final Collection<Path> paths, final String name) {
        for (final var path : paths) {
            add(path, name);
        }
    }
}
