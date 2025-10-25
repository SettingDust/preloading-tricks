package settingdust.preloading_tricks.forgelike.module_injector.accessor;

import settingdust.preloading_tricks.forgelike.JavaBypass;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.module.ModuleDescriptor;
import java.net.URI;

public class ModuleAccessor {
    public static final Class<Module> clazz = Module.class;

    private static final MethodHandle constructor;
    private static final MethodHandle implAddReadMethod;

    static {
        try {
            var lookup = JavaBypass.TRUSTED_LOOKUP.in(clazz);
            constructor = lookup.findConstructor(
                clazz, MethodType.methodType(
                    void.class,
                    ModuleLayer.class,
                    ClassLoader.class,
                    ModuleDescriptor.class,
                    URI.class
                )
            );
            implAddReadMethod = lookup.findVirtual(
                clazz,
                "implAddReads",
                MethodType.methodType(void.class, Module.class)
            );
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Module construct(
        ModuleLayer moduleLayer,
        ClassLoader classLoader,
        ModuleDescriptor moduleDescriptor,
        URI uri
    ) {
        try {
            return (Module) constructor.invokeExact(moduleLayer, classLoader, moduleDescriptor, uri);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void implAddReads(Module module, Module other) {
        try {
            implAddReadMethod.invokeExact(module, other);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
