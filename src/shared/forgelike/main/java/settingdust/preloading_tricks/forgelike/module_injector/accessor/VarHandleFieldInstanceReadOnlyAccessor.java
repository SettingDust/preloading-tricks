package settingdust.preloading_tricks.forgelike.module_injector.accessor;

import settingdust.preloading_tricks.forgelike.JavaBypass;

import java.lang.invoke.VarHandle;

public class VarHandleFieldInstanceReadOnlyAccessor {
    public static final Class<?> clazz;

    private static final VarHandle fieldOffsetField;

    static {
        try {
            clazz = Class.forName("java.lang.invoke.VarHandleReferences$FieldInstanceReadOnly");
            var lookup = JavaBypass.TRUSTED_LOOKUP.in(clazz);

            fieldOffsetField = lookup.findVarHandle(clazz, "fieldOffset", long.class);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static long getFieldOffset(VarHandle varHandle) {
        return (long) fieldOffsetField.get(varHandle);
    }
}
