package settingdust.preloadingtricks;

import java.util.Collection;
import java.util.ServiceLoader;
import java.util.function.Predicate;

/**
 * Mod adding should be done with platform specific methods like ModLocator or ModCandidateFinder <br>
 */
public interface SetupModService<TMod> {
    Collection<TMod> all();

    void add(TMod mod);

    void addAll(Collection<TMod> mod);

    void remove(TMod mod);

    void removeIf(Predicate<TMod> predicate);

    void removeAll(Collection<TMod> mods);
}
