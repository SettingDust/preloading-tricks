package settingdust.preloading_tricks.forgelike.module_injector.accessor;

import settingdust.preloading_tricks.forgelike.JavaBypass;
import settingdust.preloading_tricks.forgelike.UnsafeHacks;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.module.Configuration;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.lang.reflect.Field;

public class ResolvedModuleAccessor {
    public static final Class<ResolvedModule> clazz = ResolvedModule.class;

    private static final MethodHandle constructor;
    private static final Field cfField;

    static {
        try {
            var lookup = JavaBypass.TRUSTED_LOOKUP.in(clazz);
            constructor = lookup.findConstructor(
                clazz,
                MethodType.methodType(void.class, Configuration.class, ModuleReference.class)
            );
            cfField = clazz.getDeclaredField("cf");
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static ResolvedModule construct(Configuration cf, ModuleReference ref) {
        try {
            return (ResolvedModule) constructor.invoke(cf, ref);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Configuration getCf(ResolvedModule resolvedModule) {
        return UnsafeHacks.getField(cfField, resolvedModule);
    }

    public static void setCf(ResolvedModule resolvedModule, Configuration cf) {
        UnsafeHacks.setField(cfField, resolvedModule, cf);
    }
}
