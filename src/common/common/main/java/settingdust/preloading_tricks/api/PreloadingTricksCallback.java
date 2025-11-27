package settingdust.preloading_tricks.api;

import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import settingdust.preloading_tricks.util.ServiceLoaderUtil;

import java.util.Set;

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
    Set<ClassLoader> CLASS_LOADERS = Sets.newHashSet(
        PreloadingTricksCallback.class.getClassLoader()
    );

    private static Iterable<PreloadingTricksCallback> findAllCallbacks() {
        return CLASS_LOADERS.stream().flatMap(it ->
            Streams.stream(ServiceLoaderUtil.findServices(
                PreloadingTricksCallback.class,
                ServiceLoaderUtil.load(PreloadingTricksCallback.class, it),
                false
            ))
        ).toList();
    }

    /**
     * Global callback invoker responsible for iterating through all registered callback implementations
     * and calling their corresponding methods in order.
     *
     * <p>This object is for internal use; external code should not call it directly.</p>
     */
    PreloadingTricksCallback invoker = new PreloadingTricksCallback() {
        @Override
        public void onSetupLanguageAdapter() {
            PreloadingTricksCallback.CLASS_LOADERS.add(Thread.currentThread().getContextClassLoader());
            for (final var callback : findAllCallbacks()) {
                callback.onSetupLanguageAdapter();
            }
        }

        @Override
        public void onCollectModCandidates() {
            PreloadingTricksCallback.CLASS_LOADERS.add(Thread.currentThread().getContextClassLoader());
            for (final var callback : findAllCallbacks()) {
                callback.onCollectModCandidates();
            }
        }

        @Override
        public void onSetupMods() {
            PreloadingTricksCallback.CLASS_LOADERS.add(Thread.currentThread().getContextClassLoader());
            for (final var callback : findAllCallbacks()) {
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

    default void onCollectModCandidates() {}

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
