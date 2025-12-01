package settingdust.preloading_tricks.neoforge.modlauncher;

import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import settingdust.preloading_tricks.api.PreloadingTricksModManager;
import settingdust.preloading_tricks.neoforge.modlauncher.accessor.ModFileInfoAccessor;
import settingdust.preloading_tricks.neoforge.modlauncher.virtual_mod.VirtualJar;
import settingdust.preloading_tricks.neoforge.modlauncher.virtual_mod.VirtualModFile;
import settingdust.preloading_tricks.util.LoaderPredicates;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class NeoForgeModManager implements PreloadingTricksModManager<ModFile> {
    public static List<ModFile> mods = null;

    public NeoForgeModManager() {
        LoaderPredicates.NeoForgeModLauncher.throwIfNot();
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
    public void remove(final ModFile mod) {
        mods.remove(mod);
    }

    @Override
    public void removeIf(final Predicate<ModFile> predicate) {
        mods.removeIf(predicate);
    }

    @Override
    public void removeAll(final Collection<ModFile> all) {
        mods.removeAll(all);
    }

    @Override
    public void removeById(final String id) {
        removeByIds(Set.of(id));
    }

    @Override
    public void removeByIds(final Set<String> ids) {
        var iterator = mods.iterator();
        while (iterator.hasNext()) {
            var mod = iterator.next();
            if (mod.getModFileInfo() == null || mod.getModInfos().isEmpty()) continue;
            if (!(mod.getModFileInfo() instanceof ModFileInfo modFileInfo)) continue;
            var filtered = mod.getModInfos().stream().filter(it -> !ids.contains(it.getModId())).toList();
            if (filtered.isEmpty()) {
                iterator.remove();
            } else {
                ModFileInfoAccessor.setMods(modFileInfo, filtered);
            }
        }
    }

    @Override
    public ModFile createVirtualMod(final String id, final Path referencePath) {
        return new VirtualModFile(id, new VirtualJar(id, referencePath));
    }
}
