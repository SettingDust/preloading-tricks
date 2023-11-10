package settingdust.preloadingtricks.quilt;

import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.loader.api.plugin.ModContainerExt;
import org.quiltmc.loader.api.plugin.ModMetadataExt;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import settingdust.preloadingtricks.SetupModService;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class QuiltModSetupService implements SetupModService<ModContainerExt> {
    public static QuiltModSetupService INSTANCE;

    private final List<ModContainerExt> mods;
    private final Map<String, ModContainerExt> modMap;

    public QuiltModSetupService() throws IllegalAccessException {
        this.mods = (List<ModContainerExt>) QuiltLoaderImplAccessor.FIELD_MODS.get(QuiltLoaderImpl.INSTANCE);
        this.modMap =
                (Map<String, ModContainerExt>) QuiltLoaderImplAccessor.FIELD_MOD_MAP.get(QuiltLoaderImpl.INSTANCE);
        INSTANCE = this;
    }

    @Override
    public Collection<ModContainerExt> all() {
        return mods;
    }

    @Override
    public void add(ModContainerExt modContainer) {
        try {
            QuiltLoaderImplAccessor.METHOD_ADD_MOD.invoke(QuiltLoaderImpl.INSTANCE, modContainer);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addAll(Collection<ModContainerExt> mod) {
        for (ModContainerExt container : mod) add(container);
    }

    @Override
    public void remove(ModContainerExt modContainer) {
        mods.remove(modContainer);
        ModMetadataExt metadata = modContainer.metadata();
        modMap.remove(metadata.id());
        for (ModMetadata.ProvidedMod provide : metadata.provides()) {
            modMap.remove(provide.id());
        }
    }

    @Override
    public void removeIf(Predicate<ModContainerExt> predicate) {
        removeAll(mods.stream().filter(predicate).collect(Collectors.toSet()));
    }

    @Override
    public void removeAll(Collection<ModContainerExt> modContainers) {
        mods.removeAll(modContainers);
        Set<String> provides = modContainers.stream()
                .flatMap(it -> it.metadata().provides().stream())
                .map(ModMetadata.ProvidedMod::id)
                .collect(Collectors.toSet());
        Set<String> modIds =
                modContainers.stream().map(it -> it.metadata().id()).collect(Collectors.toSet());
        modMap.keySet().removeAll(provides);
        modMap.keySet().removeAll(modIds);
    }
}
