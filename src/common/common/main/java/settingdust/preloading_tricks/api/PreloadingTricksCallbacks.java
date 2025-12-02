package settingdust.preloading_tricks.api;

import settingdust.preloading_tricks.util.Event;

public interface PreloadingTricksCallbacks {
    Event<SetupLanguageAdapter> SETUP_LANGUAGE_ADAPTER = new Event<>(listeners ->
        () -> {
            for (var listener : listeners) {
                listener.onSetupLanguageAdapter();
            }
        }
    );

    Event<CollectModCandidates> COLLECT_MOD_CANDIDATES = new Event<>(listeners ->
        manager -> {
            for (var listener : listeners) {
                listener.onCollectModCandidates(manager);
            }
        }
    );

    Event<SetupMods> SETUP_MODS = new Event<>(listeners ->
        manager -> {
            for (var listener : listeners) {
                //noinspection unchecked
                listener.onSetupMods(manager);
            }
        }
    );

    @FunctionalInterface
    interface SetupLanguageAdapter {
        void onSetupLanguageAdapter();
    }

    @FunctionalInterface
    interface CollectModCandidates {
        void onCollectModCandidates(ModCandidatesManager manager);
    }

    @FunctionalInterface
    interface SetupMods {
        void onSetupMods(ModManager<?> manager);
    }
}
