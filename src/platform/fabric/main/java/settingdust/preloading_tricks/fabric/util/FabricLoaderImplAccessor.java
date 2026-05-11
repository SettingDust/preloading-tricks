package settingdust.preloading_tricks.fabric.util;

import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.field.FieldWrapper;
import net.lenni0451.reflect.stream.method.MethodWrapper;

import java.util.Map;

public class FabricLoaderImplAccessor {
    public static RStream stream = RStream.of(FabricLoaderImpl.class);


    private static final MethodWrapper addMod = stream.methods().by("addMod");

    private static final FieldWrapper modMap = stream.fields().by("modMap");
    private static final FieldWrapper adapterMap = stream.fields().by("adapterMap");

    public static Map<String, ModContainerImpl> modMap() {
        return modMap.get(FabricLoaderImpl.INSTANCE);
    }

    public static void addMod(ModCandidateImpl modCandidate) {
        addMod.invokeInstance(FabricLoaderImpl.INSTANCE, modCandidate);
    }

    public static Map<String, LanguageAdapter> adapterMap() {
        return adapterMap.get(FabricLoaderImpl.INSTANCE);
    }
}
