package settingdust.preloading_tricks.api.modlauncher;

import settingdust.preloading_tricks.util.Event;

public interface ModLauncherPreloadingCallbacks {
    Event<CollectAdditionalDependencySources> COLLECT_ADDITIONAL_DEPENDENCY_SOURCES = new Event<>(listeners ->
        manager -> {
            for (var listener : listeners) {
                listener.onCollectAdditionalDependencySources(manager);
            }
        }
    );

    @FunctionalInterface
    interface CollectAdditionalDependencySources {
        void onCollectAdditionalDependencySources(AdditionalDependencySourceManager manager);
    }
}
