package settingdust.preloadingtricks.fabric;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import settingdust.preloadingtricks.SetupModService;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FabricModSetupService implements SetupModService<ModContainerImpl> {
    public static FabricModSetupService INSTANCE;

    private final List<ModContainerImpl> mods;
    private final Map<String, ModContainerImpl> modMap;

    public FabricModSetupService() throws IllegalAccessException {
        this.mods = (List<ModContainerImpl>) FabricLoaderImplAccessor.FIELD_MODS.get(FabricLoaderImpl.INSTANCE);
        this.modMap =
                (Map<String, ModContainerImpl>) FabricLoaderImplAccessor.FIELD_MOD_MAP.get(FabricLoaderImpl.INSTANCE);
    }

    @Override
    public Collection<ModContainerImpl> all() {
        return mods;
    }

    @Override
    public void add(ModContainerImpl modContainer) {
        mods.add(modContainer);
        LoaderModMetadata metadata = modContainer.getMetadata();
        modMap.put(metadata.getId(), modContainer);

        for (String provides : metadata.getProvides()) {
            modMap.put(provides, modContainer);
        }
    }

    public void add(ModCandidateImpl modCandidate) throws InvocationTargetException, IllegalAccessException {
        FabricLoaderImplAccessor.METHOD_ADD_MOD.invoke(FabricLoaderImpl.INSTANCE, modCandidate);
    }

    @Override
    public void addAll(Collection<ModContainerImpl> mod) {
        for (ModContainerImpl container : mod) add(container);
    }

    @Override
    public void remove(ModContainerImpl modContainer) {
        mods.remove(modContainer);
        LoaderModMetadata metadata = modContainer.getMetadata();
        modMap.remove(metadata.getId());
        for (String provide : metadata.getProvides()) {
            modMap.remove(provide);
        }
    }

    @Override
    public void removeIf(Predicate<ModContainerImpl> predicate) {
        removeAll(mods.stream().filter(predicate).collect(Collectors.toSet()));
    }

    @Override
    public void removeAll(Collection<ModContainerImpl> modContainers) {
        mods.removeAll(modContainers);
        Set<String> provides = modContainers.stream()
                .flatMap(it -> it.getMetadata().getProvides().stream())
                .collect(Collectors.toSet());
        Set<String> modIds =
                modContainers.stream().map(it -> it.getMetadata().getId()).collect(Collectors.toSet());
        modMap.keySet().removeAll(provides);
        modMap.keySet().removeAll(modIds);
    }
}
