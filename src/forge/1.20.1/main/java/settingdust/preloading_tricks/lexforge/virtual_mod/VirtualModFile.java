package settingdust.preloading_tricks.lexforge.virtual_mod;

import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.util.Map;
import java.util.function.Supplier;

public class VirtualModFile extends ModFile {
    private static final VirtualModProvider provider = new VirtualModProvider();
    private final ModFileScanData scanData = new ModFileScanData();

    public VirtualModFile(final SecureJar jar) {
        super(jar, provider, VirtualModFileInfo::new, "GAMELIBRARY");
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
