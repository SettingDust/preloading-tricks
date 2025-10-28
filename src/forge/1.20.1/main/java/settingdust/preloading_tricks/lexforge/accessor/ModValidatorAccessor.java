package settingdust.preloading_tricks.lexforge.accessor;

import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.field.FieldWrapper;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModValidator;

import java.util.List;

public class ModValidatorAccessor {
    public static final Class<ModValidator> clazz = ModValidator.class;

    private static final RStream stream = RStream.of(clazz);

    private static final FieldWrapper candidateMods = stream.fields().by("candidateMods");

    private static final FieldWrapper gameLibraries = stream.fields().by("gameLibraries");

    public static List<ModFile> getCandidateMods(ModValidator validator) {
        return candidateMods.get(validator);
    }

    public static List<ModFile> getGameLibraries(ModValidator validator) {
        return gameLibraries.get(validator);
    }
}
