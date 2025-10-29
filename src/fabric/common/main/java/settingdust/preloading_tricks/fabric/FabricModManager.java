package settingdust.preloading_tricks.fabric;

import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import settingdust.preloading_tricks.api.PreloadingTricksModManager;
import settingdust.preloading_tricks.fabric.virtual_mod.VirtualModContainer;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FabricModManager implements PreloadingTricksModManager<ModContainerImpl> {
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

    public void add(ModCandidateImpl modCandidate) throws Throwable {
        FabricLoaderImplAccessor.addMod(modCandidate);
    }

    @Override
    public void addAll(Collection<ModContainerImpl> mod) {
        for (var container : mod) add(container);
    }

    @Override
    public void remove(ModContainerImpl modContainer) {
        mods.remove(modContainer);
        var metadata = modContainer.getMetadata();
        modMap.remove(metadata.getId());
        for (String provide : metadata.getProvides()) {
            modMap.remove(provide);
        }
    }

    @Override
    public void removeIf(Predicate<ModContainerImpl> predicate) {
        // 避免重复遍历：先选出要移除的集合再批量操作
        var toRemove = mods.stream()
                           .filter(predicate)
                           .collect(Collectors.toList());
        removeAll(toRemove);
    }

    @Override
    public void removeAll(Collection<ModContainerImpl> modContainers) {
        if (modContainers.isEmpty()) return;

        // 一次性从 mods 中删除
        mods.removeAll(modContainers);

        // 收集所有需要移除的键（mod id + provides）
        var keysToRemove = modContainers.stream()
                                        .flatMap(container -> container.getMetadata().getProvides().stream())
                                        .collect(Collectors.toSet());

        // 一次性删除 map 中的键，避免多次调用 remove
        keysToRemove.forEach(modMap::remove);
    }

    @Override
    public void removeById(final String id) {
        mods.remove(modMap.remove(id));
    }

    @Override
    public void removeByIds(final Set<String> ids) {
        mods.removeAll(ids.stream().map(modMap::remove).toList());
    }

    @Override
    public ModContainerImpl createVirtualMod(final String id, final Path referencePath) {
        return new VirtualModContainer(referencePath, id);
    }
}
