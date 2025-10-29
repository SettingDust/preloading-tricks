package settingdust.preloading_tricks.api;

import com.google.common.base.Suppliers;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.util.ServiceLoaderUtil;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Callback interface for preloading phase hooks, used to execute custom logic at key stages of module loading.
 *
 * <p>This interface is implemented based on the Java {@link java.util.ServiceLoader} mechanism,
 * allowing mod providers to intercept the module loading process by registering implementation classes of this interface.</p>
 *
 * <p>Implementation classes of this interface should be registered in the file
 * {@code META-INF/services/settingdust.preloading_tricks.api.PreloadingTricksCallback}
 * with their fully qualified class names.</p>
 *
 * @see java.util.ServiceLoader
 * @see PreloadingTricksModManager
 */
public interface PreloadingTricksCallback {
    /**
     * Supplier that discovers all registered callback implementations through the {@link ServiceLoader} mechanism.
     * Results are memoized for performance optimization.
     */
    Supplier<Iterable<PreloadingTricksCallback>> supplier =
        Suppliers.memoize(() -> ServiceLoaderUtil.findServices(PreloadingTricksCallback.class, false));

    /**
     * Global callback invoker responsible for iterating through all registered callback implementations
     * and calling their corresponding methods in order.
     *
     * <p>This object is for internal use; external code should not call it directly.</p>
     */
    PreloadingTricksCallback invoker = new PreloadingTricksCallback() {
        @Override
        public void onSetupLanguageAdapter() {
            PreloadingTricks.LOGGER.info("[{}] invoking onSetupLanguageAdapter", PreloadingTricks.NAME);
            for (final var callback : supplier.get()) {
                callback.onSetupLanguageAdapter();
            }
        }

        @Override
        public void onSetupMods() {
            PreloadingTricks.LOGGER.info("[{}] invoking onSetupMods", PreloadingTricks.NAME);

            var modManager = PreloadingTricksModManager.<PreloadingTricksModManager<Object>>get();
            try {
                Object mod = modManager.createVirtualMod(
                    "preloading_tricks",
                    Path.of(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI())
                );
                modManager.add(mod);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            for (final var callback : supplier.get()) {
                callback.onSetupMods();
            }
        }
    };

    /**
     * Called during the language adapter setup phase.
     *
     * <p>This method is triggered when the module loader initializes the language system,
     * and can be used to register custom language providers or modify language-related configurations.</p>
     *
     * <p>Default implementation does nothing.</p>
     */
    default void onSetupLanguageAdapter() {}

    /**
     * Called during the mod setup phase.
     *
     * <p>This method is triggered when the module loader prepares to load modules,
     * and can be used to modify the module list, inject virtual modules, or perform other pre-loading operations.</p>
     *
     * <p>Default implementation does nothing.</p>
     */
    default void onSetupMods() {}
}
