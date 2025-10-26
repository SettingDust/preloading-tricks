package settingdust.preloading_tricks.neoforge.v121x.accessor;

import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.field.FieldWrapper;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.List;

public class ModFileInfoAccessor {
    public static final Class<ModFileInfo> clazz = ModFileInfo.class;

    private static final RStream stream = RStream.of(clazz);

    private static final FieldWrapper modsField = stream.fields().by("mods");

    public static void setMods(ModFileInfo modFileInfo, List<IModInfo> mods) {
        modsField.set(modFileInfo, mods);
    }
}
