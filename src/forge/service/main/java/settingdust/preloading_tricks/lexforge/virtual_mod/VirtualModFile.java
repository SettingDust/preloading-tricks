package settingdust.preloading_tricks.lexforge.virtual_mod;

import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;

import java.util.Map;
import java.util.function.Supplier;

public class VirtualModFile extends ModFile {
    private static final VirtualModProvider provider = new VirtualModProvider();

    public VirtualModFile(final String id, final SecureJar jar) {
        super(jar, provider, file -> new VirtualModFileInfo(id, (VirtualModFile) file), "GAMELIBRARY");
    }

    @Override
    public Supplier<Map<String, Object>> getSubstitutionMap() {
        return Map::of;
    }

    @Override
    public String getFileName() {
        return getSecureJar().name();
    }
}
