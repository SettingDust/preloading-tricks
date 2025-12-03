package settingdust.preloading_tricks.api;

import settingdust.preloading_tricks.util.Event;

/**
 * Central callback registry for the Preloading Tricks system.
 *
 * <p>This interface defines the core events that can be subscribed to during the mod preloading phase.
 * It provides hooks at critical points in the mod loading lifecycle, allowing mods to perform
 * initialization tasks before the main game loading begins.</p>
 *
 * <p>All callbacks are implemented as {@link Event} instances, supporting multiple listeners.
 * Mods can register their implementations using the appropriate event's {@code register} method.</p>
 *
 * @see Event
 */
public interface PreloadingTricksCallbacks {
    /**
     * Event triggered during language adapter setup phase.
     *
     * <p>This callback is invoked early in the loading process, allowing mods to register
     * custom language adapters or modify existing ones before mod classes are loaded.</p>
     *
     * <p>Listener implementations should implement the {@link SetupLanguageAdapter} functional interface.</p>
     */
    Event<SetupLanguageAdapter> SETUP_LANGUAGE_ADAPTER = new Event<>(listeners ->
        () -> {
            for (var listener : listeners) {
                listener.onSetupLanguageAdapter();
            }
        }
    );

    /**
     * Event triggered during mod candidate collection phase.
     *
     * <p>This callback allows mods to dynamically add additional mod candidates to the loading process.
     * It is called before the actual mod discovery and loading begins.</p>
     *
     * <p>Listener implementations should implement the {@link CollectModCandidates} functional interface
     * and use the provided {@link ModCandidatesManager} to register additional mod paths.</p>
     *
     * @see ModCandidatesManager
     */
    Event<CollectModCandidates> COLLECT_MOD_CANDIDATES = new Event<>(listeners -> 
        manager -> {
            for (var listener : listeners) {
                listener.onCollectModCandidates(manager);
            }
        }
    );

    /**
     * Event triggered during mod setup phase.
     *
     * <p>This callback is invoked after mod candidates have been collected and basic mod metadata
     * has been processed, but before the actual mod initialization. It allows mods to modify
     * the mod list, add virtual mods, or perform other setup tasks.</p>
     *
     * <p>Listener implementations should implement the {@link SetupMods} functional interface
     * and use the provided {@link ModManager} to interact with the mod list.</p>
     *
     * @see ModManager
     */
    Event<SetupMods> SETUP_MODS = new Event<>(listeners -> 
        manager -> {
            for (var listener : listeners) {
                //noinspection unchecked
                listener.onSetupMods(manager);
            }
        }
    );

    /**
     * Functional interface for language adapter setup callbacks.
     *
     * <p>Implementations of this interface can be registered with the {@link #SETUP_LANGUAGE_ADAPTER} event
     * to perform custom language adapter configuration during the preloading phase.</p>
     */
    @FunctionalInterface
    interface SetupLanguageAdapter {
        /**
         * Called during the language adapter setup phase.
         *
         * <p>Implementations should perform any necessary language adapter configuration here.</p>
         */
        void onSetupLanguageAdapter();
    }

    /**
     * Functional interface for mod candidate collection callbacks.
     *
     * <p>Implementations of this interface can be registered with the {@link #COLLECT_MOD_CANDIDATES} event
     * to dynamically add mod candidates during the preloading phase.</p>
     *
     * @see ModCandidatesManager
     */
    @FunctionalInterface
    interface CollectModCandidates {
        /**
         * Called during the mod candidate collection phase.
         *
         * @param manager The manager instance that can be used to add additional mod candidate paths
         */
        void onCollectModCandidates(ModCandidatesManager manager);
    }

    /**
     * Functional interface for mod setup callbacks.
     *
     * <p>Implementations of this interface can be registered with the {@link #SETUP_MODS} event
     * to modify the mod list or perform other setup tasks during the preloading phase.</p>
     *
     * @see ModManager
     */
    @FunctionalInterface
    interface SetupMods {
        /**
         * Called during the mod setup phase.
         *
         * @param manager The manager instance that can be used to interact with the mod list
         */
        void onSetupMods(ModManager<?> manager);
    }
}