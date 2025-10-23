package settingdust.preloading_tricks.fabric.transformer;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTransformer;

import java.util.List;
import java.util.Map;

@CTransformer(FabricLoaderImpl.class)
public class FabricLoaderImplAccessor implements settingdust.preloading_tricks.fabric.FabricLoaderImplAccessor {

    @CShadow
    private Map<String, ModContainerImpl> modMap;
    @CShadow
    private List<ModContainerImpl> mods;

    @CShadow
    private native void addMod(ModCandidateImpl candidate);

    @Override
    public Map<String, ModContainerImpl> preloading_tricks$modMap() {
        return modMap;
    }

    @Override
    public List<ModContainerImpl> preloading_tricks$mods() {
        return mods;
    }

    @Override
    public void preloading_tricks$addMod(final ModCandidateImpl modCandidate) {
        addMod(modCandidate);
    }
}
