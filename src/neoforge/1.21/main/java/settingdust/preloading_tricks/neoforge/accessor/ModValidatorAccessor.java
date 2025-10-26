package settingdust.preloading_tricks.neoforge.accessor;

import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.field.FieldWrapper;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.fml.loading.moddiscovery.ModValidator;

import java.util.List;

public class ModValidatorAccessor {
    public static final Class<ModValidator> clazz = ModValidator.class;

    private static final RStream stream = RStream.of(clazz);

    private static final FieldWrapper candidateMods = stream.fields().by("candidateMods");

    public static List<ModFile> getCandidateMods(ModValidator validator) {
        return candidateMods.get(validator);
    }
}
