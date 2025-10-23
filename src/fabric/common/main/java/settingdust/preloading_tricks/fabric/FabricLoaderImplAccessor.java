package settingdust.preloading_tricks.fabric;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;

import java.util.List;
import java.util.Map;

public interface FabricLoaderImplAccessor {
    static FabricLoaderImplAccessor cast(FabricLoaderImpl instance) {
        return (FabricLoaderImplAccessor) (Object) instance;
    }

    Map<String, ModContainerImpl> preloading_tricks$modMap();
    List<ModContainerImpl> preloading_tricks$mods();

    void preloading_tricks$addMod(ModCandidateImpl modCandidate);
}
