package settingdust.preloading_tricks.fabric;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import net.lenni0451.reflect.JavaBypass;
import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.field.FieldWrapper;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;

public class FabricLoaderImplAccessor {
    public static RStream stream = RStream.of(FabricLoaderImpl.class);
    public static MethodHandle addMod;

    static {
        try {
            addMod = JavaBypass.TRUSTED_LOOKUP.unreflect(stream.methods().by("addMod").raw());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static FieldWrapper modMap = stream.fields().by("modMap");
    public static FieldWrapper mods = stream.fields().by("mods");

    public static Map<String, ModContainerImpl> modMap() {
        return modMap.get(FabricLoaderImpl.INSTANCE);
    }

    public static List<ModContainerImpl> mods() {
        return mods.get(FabricLoaderImpl.INSTANCE);
    }

    public static void addMod(ModCandidateImpl modCandidate) throws Throwable {
        addMod.invokeExact(FabricLoaderImpl.INSTANCE, modCandidate);
    }
}
