package settingdust.preloadingtricks;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Point is just after language adapters/providers setup and before mods <br>
 * Will be loaded with {@link java.util.ServiceLoader}. <br>
 * <br>
 * Mod adding should be done with platform specific methods like ModLocator or ModCandidateFinder <br>
 */
public interface SetupModCallback<TMod> {
    Collection<TMod> all();

    void add(TMod mod);

    void addAll(Collection<TMod> mod);

    void remove(TMod mod);

    void removeIf(Predicate<TMod> predicate);

    void removeAll(Collection<TMod> mods);
}
