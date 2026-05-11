package settingdust.preloading_tricks.neoforge.fancy_mod_loader;

import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.fml.loading.moddiscovery.ModJarMetadata;
import settingdust.preloading_tricks.neoforge.fancy_mod_loader.accessor.ModFileInfoAccessor;
import settingdust.preloading_tricks.neoforge.fancy_mod_loader.virtual_mod.VirtualModFile;
import settingdust.preloading_tricks.util.ListBackedModManager;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class NeoForgeModManager implements ListBackedModManager<ModFile, net.neoforged.neoforgespi.language.IModInfo> {
    public final List<ModFile> mods;

    public NeoForgeModManager(final List<ModFile> mods) {
        this.mods = mods;
    }

    @Override
    public List<ModFile> getMods() {
        return mods;
    }

    @Override
    public Collection<net.neoforged.neoforgespi.language.IModInfo> getInfos(final ModFile mod) {
        if (mod.getModFileInfo() == null) return List.of();
        return mod.getModInfos();
    }

    @Override
    public String getId(final net.neoforged.neoforgespi.language.IModInfo info) {
        return info.getModId();
    }

    @Override
    public void setInfos(final ModFile mod, final List<net.neoforged.neoforgespi.language.IModInfo> infos) {
        if (mod.getModFileInfo() instanceof ModFileInfo modFileInfo) ModFileInfoAccessor.setMods(modFileInfo, infos);
    }

    @Override
    public ModFile createVirtualMod(final String id, final Path referencePath) {
        var contents = JarContents.empty(referencePath);
        var metadata = new ModJarMetadata();
        var file = new VirtualModFile(contents, metadata, id);
        metadata.setModFile(file);
        return file;
    }
}
