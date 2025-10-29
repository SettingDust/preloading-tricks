package settingdust.preloading_tricks.neoforge.virtual_mod;

import cpw.mods.jarhandling.SecureJar;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;

import java.util.Map;
import java.util.function.Supplier;

public class VirtualModFile extends ModFile {
    private final ModFileScanData scanData = new ModFileScanData();

    public VirtualModFile(final SecureJar jar) {
        super(jar, VirtualModFileInfo::new, Type.GAMELIBRARY, ModFileDiscoveryAttributes.DEFAULT);
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
    public String getFileName() {
        return getSecureJar().name();
    }
}
