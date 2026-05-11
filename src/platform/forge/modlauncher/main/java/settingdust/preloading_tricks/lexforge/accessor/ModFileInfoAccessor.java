package settingdust.preloading_tricks.lexforge.accessor;

import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.field.FieldWrapper;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.List;

public class ModFileInfoAccessor {
    public static final Class<ModFileInfo> clazz = ModFileInfo.class;

    private static final RStream stream = RStream.of(clazz);

    private static final FieldWrapper modsField = stream.fields().by("mods");

    public static void setMods(ModFileInfo modFileInfo, List<IModInfo> mods) {
        modsField.set(modFileInfo, mods);
    }
}
