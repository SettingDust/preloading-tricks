package settingdust.preloading_tricks.fabric.virtual_mod.accessor;

import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.constructor.ConstructorWrapper;
import settingdust.preloading_tricks.fabric.virtual_mod.VirtualModMetadata;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class ModCandidateImplAccessor {
    private static Class<ModCandidateImpl> clazz = ModCandidateImpl.class;

    private static final RStream stream = RStream.of(clazz);

    private static final ConstructorWrapper consturctor = stream.constructors().by(
        List.class,
        String.class,
        long.class,
        LoaderModMetadata.class,
        boolean.class,
        Collection.class
    );

    public static ModCandidateImpl createVirtual(Path path, String id) {
        return consturctor.newInstance(List.of(path), null, -1, new VirtualModMetadata(id), false, List.of());
    }
}
