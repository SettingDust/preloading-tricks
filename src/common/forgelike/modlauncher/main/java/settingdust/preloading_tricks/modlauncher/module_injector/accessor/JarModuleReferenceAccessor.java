package settingdust.preloading_tricks.modlauncher.module_injector.accessor;

import cpw.mods.jarhandling.SecureJar;
import settingdust.preloading_tricks.forgelike.UnsafeHacks;

import java.lang.module.ModuleReference;
import java.lang.reflect.Field;

/**
 * Accessor for {@code cpw.mods.cl.JarModuleFinder$JarModuleReference} internal fields.
 */
public class JarModuleReferenceAccessor {
    private static final Class<?> clazz;
    private static final Field jarField;

    static {
        try {
            clazz = Class.forName("cpw.mods.cl.JarModuleFinder$JarModuleReference");
            jarField = clazz.getDeclaredField("jar");
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if a ModuleReference is an instance of JarModuleReference.
     *
     * @param reference module reference to check
     * @return true if it's a JarModuleReference
     */
    public static boolean isJarModuleReference(ModuleReference reference) {
        return clazz.isInstance(reference);
    }

    /**
     * Gets the SecureJar.ModuleDataProvider from a JarModuleReference.
     *
     * @param reference JarModuleReference instance
     * @return the jar field value
     */
    public static SecureJar.ModuleDataProvider getJar(ModuleReference reference) {
        if (!isJarModuleReference(reference)) {
            throw new IllegalArgumentException("Reference is not a JarModuleReference");
        }
        return UnsafeHacks.getField(jarField, reference);
    }

    /**
     * Sets the SecureJar.ModuleDataProvider in a JarModuleReference.
     *
     * @param reference JarModuleReference instance
     * @param jar new jar value
     */
    public static void setJar(ModuleReference reference, SecureJar.ModuleDataProvider jar) {
        if (!isJarModuleReference(reference)) {
            throw new IllegalArgumentException("Reference is not a JarModuleReference");
        }
        UnsafeHacks.setField(jarField, reference, jar);
    }
}
