package settingdust.preloading_tricks.modlauncher.module_injector.accessor;

import cpw.mods.modlauncher.TransformationServiceDecorator;

import java.lang.reflect.Field;
import java.util.Map;

public class TransformationServicesHandlerAccessor {
    public static final Class<?> clazz;

    private static final Field serviceLookupField;

    static {
        try {
            clazz = Class.forName("cpw.mods.modlauncher.TransformationServicesHandler");
            serviceLookupField = clazz.getDeclaredField("serviceLookup");
            serviceLookupField.setAccessible(true);
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, TransformationServiceDecorator> getServiceLookup(Object handler) {
        try {
            return (Map<String, TransformationServiceDecorator>) serviceLookupField.get(handler);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
