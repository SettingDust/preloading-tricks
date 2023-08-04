package settingdust.preloadingtricks.quilt;

import org.quiltmc.loader.api.LanguageAdapter;
import org.slf4j.LoggerFactory;
import settingdust.preloadingtricks.LanguageProviderCallback;

import java.util.Objects;
import java.util.ServiceLoader;

@SuppressWarnings("unused")
public class DummyLanguageAdapter implements LanguageAdapter {
    public DummyLanguageAdapter() {
        final var logger = LoggerFactory.getLogger("PreloadingTricks/LanguageProvider");
        ServiceLoader.load(LanguageProviderCallback.class).stream()
                .map(it -> {
                    try {
                        return it.get();
                    } catch (Throwable t) {
                        logger.debug("Invoke " + it.type().getName() + " failed", t);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(it -> logger.info("Invoked " + it));
    }

    @Override
    public <T> T create(org.quiltmc.loader.api.ModContainer mod, String value, Class<T> type) {
        throw new UnsupportedOperationException("This is a dummy language adapter for preloading entry points");
    }
}
