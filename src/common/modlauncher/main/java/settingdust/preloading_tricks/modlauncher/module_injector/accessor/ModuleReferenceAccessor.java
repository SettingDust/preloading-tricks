package settingdust.preloading_tricks.modlauncher.module_injector.accessor;

import settingdust.preloading_tricks.forgelike.UnsafeHacks;

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleReference;
import java.lang.reflect.Field;
import java.net.URI;

/**
 * Accessor for {@link ModuleReference} internal fields.
 */
public class ModuleReferenceAccessor {
    public static final Class<ModuleReference> clazz = ModuleReference.class;

    private static final Field descriptorField;
    private static final Field locationField;

    static {
        try {
            descriptorField = clazz.getDeclaredField("descriptor");
            locationField = clazz.getDeclaredField("location");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static ModuleDescriptor getDescriptor(ModuleReference reference) {
        return UnsafeHacks.getField(descriptorField, reference);
    }

    public static void setDescriptor(ModuleReference reference, ModuleDescriptor descriptor) {
        UnsafeHacks.setField(descriptorField, reference, descriptor);
    }

    public static URI getLocation(ModuleReference reference) {
        return UnsafeHacks.getField(locationField, reference);
    }

    public static void setLocation(ModuleReference reference, URI location) {
        UnsafeHacks.setField(locationField, reference, location);
    }
}
