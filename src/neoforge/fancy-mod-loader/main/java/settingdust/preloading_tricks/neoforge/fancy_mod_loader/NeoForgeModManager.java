package settingdust.preloading_tricks.neoforge.fancy_mod_loader;

import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.fml.loading.moddiscovery.ModJarMetadata;
import settingdust.preloading_tricks.api.ModManager;
import settingdust.preloading_tricks.neoforge.fancy_mod_loader.accessor.ModFileInfoAccessor;
import settingdust.preloading_tricks.neoforge.fancy_mod_loader.virtual_mod.VirtualModFile;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class NeoForgeModManager implements ModManager<ModFile> {
    public List<ModFile> mods;

    public NeoForgeModManager(final List<ModFile> mods) {
        this.mods = mods;
    }

    @Override
    public Collection<ModFile> all() {
        return mods;
    }

    @Override
    public void add(final ModFile mod) {
        mods.add(mod);
    }

    @Override
    public void addAll(final Collection<ModFile> mod) {
        mods.addAll(mod);
    }

    @Override
    public ModFile getById(final String id) {
        return mods.stream()
                   .filter(it -> it.getModFileInfo() != null &&
                                 it.getModInfos().stream().anyMatch(modInfo -> modInfo.getModId().equals(id)))
                   .findFirst()
                   .orElse(null);
    }

    @Override
    public boolean remove(final ModFile mod) {
        return mods.remove(mod);
    }

    @Override
    public boolean removeIf(final Predicate<ModFile> predicate) {
        return mods.removeIf(predicate);
    }

    @Override
    public boolean removeAll(final Collection<ModFile> modFiles) {
        return mods.removeAll(modFiles);
    }

    @Override
    public boolean removeById(final String id) {
        return removeByIds(Set.of(id));
    }

    @Override
    public boolean removeByIds(final Set<String> ids) {
        var iterator = mods.iterator();
        var removed = false;
        while (iterator.hasNext()) {
            var mod = iterator.next();
            if (mod.getModFileInfo() == null || mod.getModInfos().isEmpty()) continue;
            if (!(mod.getModFileInfo() instanceof ModFileInfo modFileInfo)) continue;
            var filtered = mod.getModInfos().stream().filter(it -> !ids.contains(it.getModId())).toList();
            if (filtered.isEmpty()) {
                iterator.remove();
                removed = true;
            } else if (filtered.size() != mod.getModInfos().size()) {
                removed = true;
                ModFileInfoAccessor.setMods(modFileInfo, filtered);
            }
        }
        return removed;
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
