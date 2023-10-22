package settingdust.preloadingtricks.fabric;

import net.fabricmc.loader.impl.ModContainerImpl;
import settingdust.preloadingtricks.SetupModService;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class FabricModSetupService implements SetupModService<ModContainerImpl> {
    public static FabricModSetupService INSTANCE;

    private final List<ModContainerImpl> mods;

    public FabricModSetupService(List<ModContainerImpl> mods) {
        this.mods = mods;
        INSTANCE = this;
    }

    @Override
    public Collection<ModContainerImpl> all() {
        return mods;
    }

    @Override
    public void add(ModContainerImpl modContainer) {
        mods.add(modContainer);
    }

    @Override
    public void addAll(Collection<ModContainerImpl> mod) {
        mods.addAll(mod);
    }

    @Override
    public void remove(ModContainerImpl modContainer) {
        mods.remove(modContainer);
    }

    @Override
    public void removeIf(Predicate<ModContainerImpl> predicate) {
        mods.removeIf(predicate);
    }

    @Override
    public void removeAll(Collection<ModContainerImpl> modContainers) {
        mods.removeAll(modContainers);
    }
}
