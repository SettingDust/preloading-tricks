package settingdust.preloading_tricks.api.modlauncher;

import java.nio.file.Path;
import java.util.Collection;

/**
 * A manager interface for handling additional dependency sources during the mod loading process.
 *
 * <p>This interface provides methods to register additional dependency sources with specific names,
 * allowing the mod loader to locate and load dependencies that may not be in standard locations.</p>
 */
public interface AdditionalDependencySourceManager {
    /**
     * Adds a single dependency source path with an associated name.
     *
     * @param path The path to the dependency source file or directory
     * @param name The name identifier for this dependency source
     */
    void add(Path path, String name);

    /**
     * Adds multiple dependency source paths with an associated name.
     *
     * @param paths A collection of paths to dependency source files or directories
     * @param name  The name identifier shared by all these dependency sources
     */
    void addAll(Collection<Path> paths, String name);
}