package settingdust.preloading_tricks.api;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Module management interface providing access and modification capabilities for the module list during the preloading phase.
 *
 * <p>This interface manages the lifecycle of modules and supports dynamic addition, deletion, and querying of modules.
 * Implementation classes are responsible for maintaining the current set of loaded modules.</p>
 *
 * <p>This interface obtains its singleton implementation through the {@link java.util.ServiceLoader} mechanism,
 * ensuring singleton behavior throughout the application lifecycle.</p>
 *
 * @param <M> The type of module object, defined by the specific implementation
 * @see java.util.ServiceLoader
 * @see PreloadingTricksCallback
 */
public interface ModManager<M> {
    /**
     * Retrieves all currently managed modules.
     *
     * @return A collection containing all modules, never null but may be empty
     */
    Collection<M> all();

    /**
     * Adds a single module to the management list.
     *
     * @param mod The module instance to add
     */
    void add(M mod);

    /**
     * Batch adds multiple modules to the management list.
     *
     * @param mod A collection containing the modules to add
     */
    void addAll(Collection<M> mod);

    M getById(String id);

    /**
     * Removes a specific module.
     *
     * @param mod The module instance to remove
     */
    boolean remove(M mod);

    /**
     * Removes modules matching the specified predicate condition.
     *
     * @param predicate A predicate function used to determine which modules to remove
     */
    boolean removeIf(Predicate<M> predicate);

    /**
     * Batch removes multiple modules.
     *
     * @param mods A collection containing the modules to remove
     */
    boolean removeAll(Collection<M> mods);

    /**
     * Removes a single module by its ID.
     *
     * @param id The unique identifier of the module to remove
     */
    boolean removeById(String id);

    /**
     * Batch removes multiple modules by their IDs.
     *
     * @param ids A set containing the IDs of modules to remove
     */
    boolean removeByIds(Set<String> ids);

    /**
     * Creates a virtual/dummy module instance with the given ID.
     *
     * <p>Virtual modules serve as placeholders during the loading process or virtual modules that don't
     * correspond to actual module files.
     * They can be used for dynamic injection or testing scenarios.</p>
     *
     * @param id            The unique identifier for the virtual module
     * @param referencePath A path to a reference file or directory, for debugging and display purposes
     *
     * @return The created virtual module instance
     */
    M createVirtualMod(String id, Path referencePath);
}
