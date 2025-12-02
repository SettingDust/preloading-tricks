package settingdust.preloading_tricks.fabric;

import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import settingdust.preloading_tricks.api.ModManager;
import settingdust.preloading_tricks.fabric.util.FabricLoaderImplAccessor;
import settingdust.preloading_tricks.fabric.virtual_mod.VirtualModContainer;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FabricModManager implements ModManager<ModContainerImpl> {
    private final List<ModContainerImpl> mods;
    private final Map<String, ModContainerImpl> modMap;

    public FabricModManager() {
        mods = FabricLoaderImplAccessor.mods();
        modMap = FabricLoaderImplAccessor.modMap();
    }

    @Override
    public Collection<ModContainerImpl> all() {
        return mods;
    }

    @Override
    public void add(ModContainerImpl modContainer) {
        mods.add(modContainer);
        var metadata = modContainer.getMetadata();
        modMap.put(metadata.getId(), modContainer);

        for (String provides : metadata.getProvides()) {
            modMap.put(provides, modContainer);
        }
    }

    public void add(ModCandidateImpl modCandidate) {
        FabricLoaderImplAccessor.addMod(modCandidate);
    }

    @Override
    public void addAll(Collection<ModContainerImpl> mod) {
        for (var container : mod) add(container);
    }

    @Override
    public ModContainerImpl getById(final String id) {
        return modMap.get(id);
    }

    @Override
    public boolean remove(ModContainerImpl modContainer) {
        var removed = mods.remove(modContainer);
        var metadata = modContainer.getMetadata();
        modMap.remove(metadata.getId());
        for (String provide : metadata.getProvides()) {
            if (modMap.get(provide).equals(modContainer))
                modMap.remove(provide);
        }
        return removed;
    }

    @Override
    public boolean removeIf(Predicate<ModContainerImpl> predicate) {
        var toRemove = mods.stream()
                           .filter(predicate)
                           .collect(Collectors.toList());
        return removeAll(toRemove);
    }

    @Override
    public boolean removeAll(Collection<ModContainerImpl> modContainers) {
        if (modContainers.isEmpty()) return true;

        var removed = mods.removeAll(modContainers);

        var keysToRemove = modContainers.stream()
                                        .flatMap(container -> container.getMetadata().getProvides().stream())
                                        .collect(Collectors.toSet());

        keysToRemove.forEach(modMap::remove);
        return removed;
    }

    @Override
    public boolean removeById(final String id) {
        var modContainer = modMap.remove(id);
        var metadata = modContainer.getMetadata();
        var removed = mods.remove(modContainer);
        for (String provide : metadata.getProvides()) {
            if (modMap.get(provide).equals(modContainer))
                modMap.remove(provide);
        }
        return removed;
    }

    @Override
    public boolean removeByIds(final Set<String> ids) {
        return mods.removeAll(ids.stream().map(modMap::remove).toList());
    }

    @Override
    public ModContainerImpl createVirtualMod(final String id, final Path referencePath) {
        return new VirtualModContainer(referencePath, id);
    }
}
