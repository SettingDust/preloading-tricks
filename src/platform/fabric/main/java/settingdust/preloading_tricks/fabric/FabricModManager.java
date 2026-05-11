package settingdust.preloading_tricks.fabric;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import settingdust.preloading_tricks.api.ModManager;
import settingdust.preloading_tricks.fabric.util.FabricLoaderImplAccessor;
import settingdust.preloading_tricks.fabric.virtual_mod.VirtualModContainer;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FabricModManager implements ModManager<ModContainerImpl> {
    private final List<ModContainerImpl> mods;
    private final Map<String, ModContainerImpl> modMap;

    public FabricModManager() {
        mods = FabricLoaderImpl.INSTANCE.getModsInternal();
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

    private void removeMappings(final ModContainerImpl modContainer) {
        if (modContainer == null) return;

        var metadata = modContainer.getMetadata();

        if (Objects.equals(modMap.get(metadata.getId()), modContainer)) {
            modMap.remove(metadata.getId());
        }

        for (String provide : metadata.getProvides()) {
            if (Objects.equals(modMap.get(provide), modContainer)) {
                modMap.remove(provide);
            }
        }
    }

    @Override
    public boolean remove(ModContainerImpl modContainer) {
        if (modContainer == null) return false;

        var removed = mods.remove(modContainer);
        removeMappings(modContainer);
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

        var uniqueContainers = new LinkedHashSet<>(modContainers);
        var removed = mods.removeAll(uniqueContainers);
        uniqueContainers.forEach(this::removeMappings);
        return removed;
    }

    @Override
    public boolean removeById(final String id) {
        return remove(modMap.get(id));
    }

    @Override
    public boolean removeByIds(final Set<String> ids) {
        return removeAll(
            ids.stream()
               .map(modMap::get)
               .filter(Objects::nonNull)
               .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }

    @Override
    public ModContainerImpl createVirtualMod(final String id, final Path referencePath) {
        return new VirtualModContainer(referencePath, id);
    }
}
