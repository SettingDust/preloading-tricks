package settingdust.preloadingtricks.fabric;

import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import settingdust.preloadingtricks.LanguageProviderCallback;

import java.util.Objects;
import java.util.ServiceLoader;

@SuppressWarnings("unused")
public class DummyLanguageAdapter implements LanguageAdapter {
    public DummyLanguageAdapter() {
        final var category = LogCategory.create("PreloadingTricks/LanguageProviderCallback");
        for (final var callback : ServiceLoader.load(LanguageProviderCallback.class)) {
            try {
                Objects.requireNonNull(callback);
                Log.info(category, "Invoked " + callback);
            } catch (Exception e) {
                Log.info(category, "Invoke " + callback + " failed", e);
            }
        }
    }

    @Override
    public <T> T create(ModContainer mod, String value, Class<T> type) {
        throw new UnsupportedOperationException("This is a dummy language adapter for preloading entry points");
    }
}
