package settingdust.preloading_tricks.fabric.virtual_mod;

import net.fabricmc.loader.impl.ModContainerImpl;
import settingdust.preloading_tricks.fabric.virtual_mod.accessor.ModCandidateImplAccessor;

import java.nio.file.Path;

public class VirtualModContainer extends ModContainerImpl {
    public VirtualModContainer(Path path, String id) {
        super(ModCandidateImplAccessor.createVirtual(path, id));
    }
}
