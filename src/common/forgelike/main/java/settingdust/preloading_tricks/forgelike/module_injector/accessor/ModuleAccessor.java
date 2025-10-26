package settingdust.preloading_tricks.forgelike.module_injector.accessor;

import settingdust.preloading_tricks.forgelike.JavaBypass;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.module.ModuleDescriptor;
import java.net.URI;

public class ModuleAccessor {
    public static final Class<Module> clazz = Module.class;

    private static final MethodHandle constructor;
    private static final MethodHandle implAddReadMethod;
    private static final VarHandle layerField;
    public static final long layerFieldOffset;

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
            layerField = lookup.findVarHandle(clazz, "layer", ModuleLayer.class);
            layerFieldOffset = VarHandleFieldInstanceReadOnlyAccessor.getFieldOffset(layerField);
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            for (var field : Module.class.getDeclaredFields()) {
                System.out.println(field);
            }
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

    public static void setLayer(Module module, ModuleLayer moduleLayer) {
        JavaBypass.UNSAFE.putObject(
            module,
            layerFieldOffset,
            moduleLayer
        );
    }
}
