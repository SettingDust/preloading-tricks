package settingdust.preloading_tricks.api;

import java.nio.file.Path;
import java.util.Collection;

/**
 * A manager interface for handling mod candidate paths during the preloading phase.
 *
 * <p>This interface provides methods to dynamically add mod candidate paths that
 * should be considered during the mod loading process. Implementations of this interface
 * are responsible for collecting and managing these paths before the actual mod loading begins.</p>
 */
public interface ModCandidatesManager {
    /**
     * Adds a single mod candidate path to the collection.
     *
     * @param path The path to a mod candidate file or directory to be added
     */
    void add(Path path);

    /**
     * Adds multiple mod candidate paths to the collection.
     *
     * @param paths A collection of paths to mod candidate files or directories to be added
     */
    void addAll(Collection<Path> paths);
}