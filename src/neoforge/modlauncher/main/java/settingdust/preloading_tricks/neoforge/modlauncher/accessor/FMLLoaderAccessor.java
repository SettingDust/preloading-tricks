package settingdust.preloading_tricks.neoforge.modlauncher.accessor;

import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.field.FieldWrapper;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModValidator;

public class FMLLoaderAccessor {
    public static final Class<FMLLoader> clazz = FMLLoader.class;

    private static final RStream stream = RStream.of(clazz);

    private static final FieldWrapper modValidatorField = stream.fields().by("modValidator");

    public static ModValidator getModValidator() {
        return modValidatorField.get();
    }
}
