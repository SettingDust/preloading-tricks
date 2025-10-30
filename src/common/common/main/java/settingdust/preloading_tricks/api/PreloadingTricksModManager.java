package settingdust.preloading_tricks.api;

import com.google.common.base.Suppliers;
import settingdust.preloading_tricks.util.ServiceLoaderUtil;

import java.nio.file.Path;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
public interface PreloadingTricksModManager<M> {
    /**
     * Supplier that discovers the module manager implementation through the {@link ServiceLoader} mechanism.
     * Results are memoized to ensure singleton behavior.
     */
    Supplier<PreloadingTricksModManager<?>> supplier =
        Suppliers.memoize(() -> ServiceLoaderUtil.findService(PreloadingTricksModManager.class));

    /**
     * Retrieves the singleton instance of the module manager.
     *
     * <p>This method returns the module manager implementation discovered via {@link ServiceLoader}.
     * The return value should be cast to the concrete implementation class.</p>
     *
     * @param <I> The implementation type, should be {@link PreloadingTricksModManager} implementation class or this interface with the correct {@link M} type parameter
     * @return The singleton instance of the module manager
     */
    static <I extends PreloadingTricksModManager<?>> I get() {
        return (I) supplier.get();
    }

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

    /**
     * Removes a specific module.
     *
     * @param mod The module instance to remove
     */
    void remove(M mod);

    /**
     * Removes modules matching the specified predicate condition.
     *
     * @param predicate A predicate function used to determine which modules to remove
     */
    void removeIf(Predicate<M> predicate);

    /**
     * Batch removes multiple modules.
     *
     * @param mods A collection containing the modules to remove
     */
    void removeAll(Collection<M> mods);

    /**
     * Removes a single module by its ID.
     *
     * @param id The unique identifier of the module to remove
     */
    void removeById(String id);

    /**
     * Batch removes multiple modules by their IDs.
     *
     * @param ids A set containing the IDs of modules to remove
     */
    void removeByIds(Set<String> ids);

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
