package settingdust.preloading_tricks.neoforge.v121x.virtual_mod;

import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;

import java.util.Map;
import java.util.function.Supplier;

public class VirtualModFile extends ModFile {
    private final String id;
    private final ModFileScanData scanData = new ModFileScanData();

    public VirtualModFile(final JarContents jar, final String id) {
        super(jar, null, VirtualModFileInfo::new, Type.GAMELIBRARY, ModFileDiscoveryAttributes.DEFAULT);
        this.id = id;
    }

    @Override
    public Supplier<Map<String, Object>> getSubstitutionMap() {
        return Map::of;
    }

    @Override
    public ModFileScanData getScanResult() {
        return scanData;
    }

    @Override
    public String getId() {
        return id;
    }
}
