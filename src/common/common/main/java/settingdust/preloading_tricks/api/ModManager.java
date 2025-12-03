package settingdust.preloading_tricks.api;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Mod management interface providing access and modification capabilities for the mod list during the preloading phase.
 *
 * <p>This interface manages the lifecycle of mods and supports dynamic addition, deletion, and querying of mods.
 * Implementation classes are responsible for maintaining the current set of loaded mods.</p>
 *
 * <p>Instances of this interface are typically passed to callback functions registered with {@link PreloadingTricksCallbacks#SETUP_MODS},
 * allowing mods to interact with the mod list during the preloading phase.</p>
 *
 * @param <M> The type of mod object, defined by the specific implementation
 * @see PreloadingTricksCallbacks
 */
public interface ModManager<M> {
    /**
     * Retrieves all currently managed mods.
     *
     * @return A collection containing all mods, never null but may be empty
     */
    Collection<M> all();

    /**
     * Adds a single mod to the management list.
     *
     * @param mod The mod instance to add
     */
    void add(M mod);

    /**
     * Batch adds multiple mods to the management list.
     *
     * @param mod A collection containing the mods to add
     */
    void addAll(Collection<M> mod);

    /**
     * Retrieves a mod by its unique identifier.
     *
     * @param id The unique identifier of the mod to retrieve
     * @return The mod instance with the specified ID, or null if not found
     */
    M getById(String id);

    /**
     * Removes a specific mod.
     *
     * @param mod The mod instance to remove
     * @return true if the mod was successfully removed, false otherwise
     */
    boolean remove(M mod);

    /**
     * Removes mods matching the specified predicate condition.
     *
     * @param predicate A predicate function used to determine which mods to remove
     * @return true if any mods were successfully removed, false otherwise
     */
    boolean removeIf(Predicate<M> predicate);

    /**
     * Batch removes multiple mods.
     *
     * @param mods A collection containing the mods to remove
     * @return true if any mods were successfully removed, false otherwise
     */
    boolean removeAll(Collection<M> mods);

    /**
     * Removes a single mod by its ID.
     *
     * @param id The unique identifier of the mod to remove
     * @return true if the mod was successfully removed, false otherwise
     */
    boolean removeById(String id);

    /**
     * Batch removes multiple mods by their IDs.
     *
     * @param ids A set containing the IDs of mods to remove
     * @return true if any mods were successfully removed, false otherwise
     */
    boolean removeByIds(Set<String> ids);

    /**
     * Creates a virtual/dummy mod instance with the given ID.
     *
     * <p>Virtual mods serve as placeholders during the loading process or virtual mods that don't
     * correspond to actual mod files.
     * They can be used for dynamic injection or testing scenarios.</p>
     *
     * @param id            The unique identifier for the virtual mod
     * @param referencePath A path to a reference file or directory, for debugging and display purposes
     *
     * @return The created virtual mod instance
     */
    M createVirtualMod(String id, Path referencePath);
}