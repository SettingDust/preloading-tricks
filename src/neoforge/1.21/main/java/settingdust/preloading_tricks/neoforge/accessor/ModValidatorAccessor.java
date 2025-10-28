package settingdust.preloading_tricks.neoforge.accessor;

import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.field.FieldWrapper;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.fml.loading.moddiscovery.ModValidator;
import net.neoforged.neoforgespi.locating.IModFile;

import java.util.List;
import java.util.Map;

public class ModValidatorAccessor {
    public static final Class<ModValidator> clazz = ModValidator.class;

    private static final RStream stream = RStream.of(clazz);

    private static final FieldWrapper candidateMods = stream.fields().by("candidateMods");
    private static final FieldWrapper modFiles = stream.fields().by("modFiles");

    public static List<ModFile> getCandidateMods(ModValidator validator) {
        return candidateMods.get(validator);
    }

    public static Map<IModFile.Type, List<ModFile>> getModFiles(ModValidator validator) {
        return modFiles.get(validator);
    }
}
