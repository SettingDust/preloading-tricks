package settingdust.preloading_tricks.api;

import com.google.common.base.Suppliers;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.util.ServiceLoaderUtil;

import java.util.function.Supplier;

public interface PreloadingTricksCallback {
    Supplier<Iterable<PreloadingTricksCallback>> supplier =
        Suppliers.memoize(() -> ServiceLoaderUtil.findServices(PreloadingTricksCallback.class));

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
            for (final var callback : supplier.get()) {
                callback.onSetupMods();
            }
        }
    };

    void onSetupLanguageAdapter();

    void onSetupMods();
}
