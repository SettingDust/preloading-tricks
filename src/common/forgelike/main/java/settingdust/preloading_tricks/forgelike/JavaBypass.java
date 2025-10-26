package settingdust.preloading_tricks.forgelike;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

/**
 * The main class for bypassing java restrictions.<br>
 * This class contains the unsafe instance and the trusted lookup instance used for everything else.
 */
public class JavaBypass {

    /**
     * The instance of the unsafe class.
     */
    public static final Unsafe UNSAFE;

    /**
     * The instance of the trusted lookup.
     */
    public static final MethodHandles.Lookup TRUSTED_LOOKUP;

    static {
        try {
            UNSAFE = getUnsafe();
            TRUSTED_LOOKUP = getTrustedLookup();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the unsafe instance.<br>
     * You should use the static instance {@link #UNSAFE} instead.
     *
     * @return The unsafe instance
     * @throws IllegalStateException If the unsafe instance could not be gotten
     */
    public static Unsafe getUnsafe() throws IllegalAccessException {
        for (Field field : Unsafe.class.getDeclaredFields()) {
            if (field.getType().equals(Unsafe.class)) {
                field.setAccessible(true);
                return (Unsafe) field.get(null);
            }
        }
        return null;
    }

    /**
     * Get the trusted lookup instance.<br>
     * You should use the static instance {@link #TRUSTED_LOOKUP} instead.
     *
     * @return The trusted lookup instance
     * @throws IllegalStateException If the trusted lookup instance could not be gotten
     */
    public static MethodHandles.Lookup getTrustedLookup() throws NoSuchFieldException {
        MethodHandles.lookup(); //Load class before getting the trusted lookup
        Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        long lookupFieldOffset = UNSAFE.staticFieldOffset(lookupField);
        return (MethodHandles.Lookup) UNSAFE.getObject(MethodHandles.Lookup.class, lookupFieldOffset);
    }
}
