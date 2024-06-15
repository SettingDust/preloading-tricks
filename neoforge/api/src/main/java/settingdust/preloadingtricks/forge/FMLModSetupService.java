package settingdust.preloadingtricks.forge;

import net.neoforged.fml.loading.moddiscovery.ModFile;
import settingdust.preloadingtricks.SetupModService;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public class FMLModSetupService implements SetupModService<ModFile> {
    public static FMLModSetupService INSTANCE;

    private final List<ModFile> mods;

    public FMLModSetupService(List<ModFile> mods) {
        this.mods = mods;
        INSTANCE = this;
    }

    @Override
    public Collection<ModFile> all() {
        return mods;
    }

    @Override
    public void add(ModFile modFile) {
        mods.add(modFile);
    }

    @Override
    public void addAll(Collection<ModFile> mod) {
        mods.addAll(mod);
    }

    @Override
    public void remove(ModFile modFile) {
        mods.remove(modFile);
    }

    @Override
    public void removeIf(Predicate<ModFile> predicate) {
        mods.removeIf(predicate);
    }

    @Override
    public void removeAll(Collection<ModFile> modFiles) {
        mods.removeAll(modFiles);
    }
}
