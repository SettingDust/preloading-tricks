package settingdust.preloading_tricks.neoforge.virtual_mod;

import cpw.mods.jarhandling.SecureJar;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;

import java.util.Map;
import java.util.function.Supplier;

public class VirtualModFile extends ModFile {
    public VirtualModFile(final String id, final SecureJar jar) {
        super(
            jar,
            file -> new VirtualModFileInfo(id, (VirtualModFile) file),
            Type.GAMELIBRARY,
            ModFileDiscoveryAttributes.DEFAULT
        );
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
