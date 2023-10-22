package settingdust.preloadingtricks.quilt;

import org.quiltmc.loader.api.plugin.ModContainerExt;
import settingdust.preloadingtricks.SetupModService;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class QuiltModSetupService implements SetupModService<ModContainerExt> {
    public static QuiltModSetupService INSTANCE;

    private final List<ModContainerExt> mods;

    public QuiltModSetupService(List<ModContainerExt> mods) {
        this.mods = mods;
        INSTANCE = this;
    }

    @Override
    public Collection<ModContainerExt> all() {
        return mods;
    }

    @Override
    public void add(ModContainerExt modContainer) {
        mods.add(modContainer);
    }

    @Override
    public void addAll(Collection<ModContainerExt> mod) {
        mods.addAll(mod);
    }

    @Override
    public void remove(ModContainerExt modContainer) {
        mods.remove(modContainer);
    }

    @Override
    public void removeIf(Predicate<ModContainerExt> predicate) {
        mods.removeIf(predicate);
    }

    @Override
    public void removeAll(Collection<ModContainerExt> modContainers) {
        mods.removeAll(modContainers);
    }
}
