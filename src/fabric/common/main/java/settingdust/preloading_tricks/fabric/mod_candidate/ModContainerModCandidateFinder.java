package settingdust.preloading_tricks.fabric.mod_candidate;

import net.fabricmc.loader.api.metadata.ModOrigin;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.discovery.ClasspathModCandidateFinder;

import java.util.Collection;

/**
 * Fabric loader won't provide mod candidate. We have to scan the paths twice.
 */
public class ModContainerModCandidateFinder extends ClasspathModCandidateFinder {
    private final Collection<ModContainerImpl> containers;

    public ModContainerModCandidateFinder(Collection<ModContainerImpl> containers) {
        this.containers = containers;
    }

    @Override
    public void findCandidates(ModCandidateConsumer out) {
        containers.forEach((ModContainerImpl container) -> {
            if (container.getOrigin().getKind().equals(ModOrigin.Kind.PATH))
                out.accept(container.getOrigin().getPaths(), false);
        });
    }
}
