package settingdust.preloading_tricks.lexforge;

import com.google.common.collect.Iterators;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import settingdust.preloading_tricks.api.PreloadingTricksModManager;
import settingdust.preloading_tricks.lexforge.accessor.FMLLoaderAccessor;
import settingdust.preloading_tricks.lexforge.accessor.ModFileInfoAccessor;
import settingdust.preloading_tricks.lexforge.accessor.ModValidatorAccessor;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class LexForgeModManager implements PreloadingTricksModManager<ModFile> {
    private final List<ModFile> mods = ModValidatorAccessor.getCandidateMods(FMLLoaderAccessor.getModValidator());
    private final List<ModFile> gameLibraries =
        ModValidatorAccessor.getGameLibraries(FMLLoaderAccessor.getModValidator());

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
    public void remove(final ModFile mod) {
        mods.remove(mod);
    }

    @Override
    public void removeIf(final Predicate<ModFile> predicate) {
        mods.removeIf(predicate);
        gameLibraries.removeIf(predicate);
    }

    @Override
    public void removeAll(final Collection<ModFile> modFiles) {
        mods.removeAll(modFiles);
        gameLibraries.removeAll(modFiles);
    }

    @Override
    public void removeById(final String id) {
        removeByIds(Set.of(id));
    }

    @Override
    public void removeByIds(final Set<String> ids) {
        var iterator = Iterators.concat(mods.iterator(), gameLibraries.iterator());
        while (iterator.hasNext()) {
            var mod = iterator.next();
            if (mod.getModInfos().isEmpty()) continue;
            if (!(mod.getModFileInfo() instanceof ModFileInfo modFileInfo)) continue;
            var filtered = mod.getModInfos().stream().filter(it -> !ids.contains(it.getModId())).toList();
            if (filtered.isEmpty()) {
                iterator.remove();
            } else {
                ModFileInfoAccessor.setMods(modFileInfo, filtered);
            }
        }
    }
}
