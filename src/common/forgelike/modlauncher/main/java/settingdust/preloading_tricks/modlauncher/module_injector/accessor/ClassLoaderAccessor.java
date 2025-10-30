package settingdust.preloading_tricks.modlauncher.module_injector.accessor;

import settingdust.preloading_tricks.forgelike.JavaBypass;

import java.lang.invoke.VarHandle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Accessor for {@link ClassLoader} internal fields.
 */
public class ClassLoaderAccessor {
    public static final Class<ClassLoader> clazz = ClassLoader.class;

    private static final VarHandle package2certsField;

    static {
        try {
            var lookup = JavaBypass.TRUSTED_LOOKUP.in(clazz);
            package2certsField = lookup.findVarHandle(
                ClassLoader.class,
                "package2certs",
                ConcurrentHashMap.class
            );
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the package2certs map from a ClassLoader.
     *
     * @param classLoader class loader
     * @return package to certificates map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, ?> getPackage2Certs(ClassLoader classLoader) {
        return (Map<String, ?>) package2certsField.get(classLoader);
    }
}
