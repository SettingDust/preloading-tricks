package settingdust.preloading_tricks.fabric.util;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.field.FieldWrapper;
import net.lenni0451.reflect.stream.method.MethodWrapper;

import java.util.List;
import java.util.Map;

public class FabricLoaderImplAccessor {
    public static RStream stream = RStream.of(FabricLoaderImpl.class);


    private static final MethodWrapper addMod = stream.methods().by("addMod");

    private static final FieldWrapper modCandidates = stream.fields().by("modCandidates");
    private static final FieldWrapper modMap = stream.fields().by("modMap");
    private static final FieldWrapper mods = stream.fields().by("mods");

    public static Map<String, ModContainerImpl> modMap() {
        return modMap.get(FabricLoaderImpl.INSTANCE);
    }

    public static List<ModContainerImpl> mods() {
        return mods.get(FabricLoaderImpl.INSTANCE);
    }

    public static void addMod(ModCandidateImpl modCandidate) {
        addMod.invokeInstance(FabricLoaderImpl.INSTANCE, modCandidate);
    }

    public static List<ModCandidateImpl> modCandidates() {
        return modCandidates.get(FabricLoaderImpl.INSTANCE);
    }
}
