package settingdust.preloading_tricks.neoforge.fancy_mod_loader.virtual_mod;

import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;

import java.util.Map;
import java.util.function.Supplier;

public class VirtualModFile extends ModFile {
    public VirtualModFile(final JarContents jar, final String id) {
        super(
            jar,
            null,
            file -> new VirtualModFileInfo(id, (ModFile) file),
            Type.MOD,
            ModFileDiscoveryAttributes.DEFAULT
        );
    }

    @Override
    public Supplier<Map<String, Object>> getSubstitutionMap() {
        return Map::of;
    }
}
