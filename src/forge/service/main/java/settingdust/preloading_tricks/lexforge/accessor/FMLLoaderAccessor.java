package settingdust.preloading_tricks.lexforge.accessor;

import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.field.FieldWrapper;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModValidator;

public class FMLLoaderAccessor {
    public static final Class<FMLLoader> clazz = FMLLoader.class;

    private static final RStream stream = RStream.of(clazz);

    private static final FieldWrapper modValidatorField = stream.fields().by("modValidator");

    public static ModValidator getModValidator() {
        return modValidatorField.get();
    }
}
