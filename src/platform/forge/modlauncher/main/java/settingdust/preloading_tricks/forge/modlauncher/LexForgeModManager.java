package settingdust.preloading_tricks.forge.modlauncher;

import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import settingdust.preloading_tricks.forge.modlauncher.accessor.ModFileInfoAccessor;
import settingdust.preloading_tricks.forge.modlauncher.virtual_mod.VirtualJar;
import settingdust.preloading_tricks.forge.modlauncher.virtual_mod.VirtualModFile;
import settingdust.preloading_tricks.util.ListBackedModManager;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class LexForgeModManager implements ListBackedModManager<ModFile, net.minecraftforge.forgespi.language.IModInfo> {
    public final List<ModFile> mods;

    public LexForgeModManager(List<ModFile> mods) {
        this.mods = mods;
    }

    @Override
    public List<ModFile> getMods() {
        return mods;
    }

    @Override
    public Collection<net.minecraftforge.forgespi.language.IModInfo> getInfos(final ModFile mod) {
        if (mod.getModFileInfo() == null) return List.of();
        return mod.getModInfos();
    }

    @Override
    public String getId(final net.minecraftforge.forgespi.language.IModInfo info) {
        return info.getModId();
    }

    @Override
    public void setInfos(final ModFile mod, final List<net.minecraftforge.forgespi.language.IModInfo> infos) {
        if (mod.getModFileInfo() instanceof ModFileInfo modFileInfo) ModFileInfoAccessor.setMods(modFileInfo, infos);
    }

    @Override
    public ModFile createVirtualMod(final String id, Path referencePath) {
        return new VirtualModFile(id, new VirtualJar(id, referencePath));
    }
}
