package settingdust.preloading_tricks.neoforge.modlauncher.virtual_mod;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.IModLanguageLoader;
import net.neoforged.neoforgespi.language.ModFileScanData;

public class VirtualLanguageProvider implements IModLanguageLoader {
    @Override
    public String name() {
        return "virtual";
    }

    @Override
    public String version() {
        return "0";
    }

    @Override
    public ModContainer loadMod(
        final IModInfo info,
        final ModFileScanData modFileScanResults,
        final ModuleLayer layer
    ) throws ModLoadingException {
        return new VirtualModContainer(info);
    }
}
