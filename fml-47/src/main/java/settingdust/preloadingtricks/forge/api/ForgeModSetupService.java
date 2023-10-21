package settingdust.preloadingtricks.forge.api;

import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import settingdust.preloadingtricks.SetupModService;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ForgeModSetupService implements SetupModService<ModFile> {
    public static ForgeModSetupService INSTANCE;
    private final List<ModFile> candidateMods;

    public ForgeModSetupService(List<ModFile> candidateMods) {
        INSTANCE = this;
        this.candidateMods = candidateMods;
    }

    @Override
    public Collection<ModFile> all() {
        return candidateMods;
    }

    @Override
    public void add(ModFile modFile) {
        candidateMods.add(modFile);
    }

    @Override
    public void addAll(Collection<ModFile> mod) {
        candidateMods.addAll(mod);
    }

    @Override
    public void remove(ModFile modFile) {
        candidateMods.remove(modFile);
    }

    @Override
    public void removeIf(Predicate<ModFile> predicate) {
        candidateMods.removeIf(predicate);
    }

    @Override
    public void removeAll(Collection<ModFile> modFiles) {
        candidateMods.removeAll(modFiles);
    }
}