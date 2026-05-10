package settingdust.preloading_tricks.util;

import settingdust.preloading_tricks.api.ModManager;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public interface ListBackedModManager<M, I> extends ModManager<M> {
    @Override
    default Collection<M> all() {
        return getMods();
    }

    @Override
    default void add(final M mod) {
        getMods().add(mod);
    }

    @Override
    default void addAll(final Collection<M> mod) {
        getMods().addAll(mod);
    }

    @Override
    default M getById(final String id) {
        return getMods().stream()
                   .filter(mod -> getInfos(mod).stream().anyMatch(info -> getId(info).equals(id)))
                   .findFirst()
                   .orElse(null);
    }

    @Override
    default boolean remove(final M mod) {
        return getMods().remove(mod);
    }

    @Override
    default boolean removeIf(final Predicate<M> predicate) {
        return getMods().removeIf(predicate);
    }

    @Override
    default boolean removeAll(final Collection<M> mods) {
        return getMods().removeAll(mods);
    }

    @Override
    default boolean removeById(final String id) {
        return removeByIds(Set.of(id));
    }

    @Override
    default boolean removeByIds(final Set<String> ids) {
        var iterator = getMods().iterator();
        var removed = false;
        while (iterator.hasNext()) {
            var mod = iterator.next();
            var infos = getInfos(mod);
            if (infos.isEmpty()) continue;
            var filtered = infos.stream().filter(info -> !ids.contains(getId(info))).toList();
            if (filtered.isEmpty()) {
                iterator.remove();
                removed = true;
            } else if (filtered.size() != infos.size()) {
                removed = true;
                setInfos(mod, filtered);
            }
        }
        return removed;
    }

    List<M> getMods();

    Collection<I> getInfos(M mod);

    String getId(I info);

    void setInfos(M mod, List<I> infos);

    @Override
    M createVirtualMod(String id, Path referencePath);
}
