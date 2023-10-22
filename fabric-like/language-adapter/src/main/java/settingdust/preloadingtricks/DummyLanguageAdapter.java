package settingdust.preloadingtricks;

import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import settingdust.preloadingtricks.util.ServiceLoaderUtil;

import java.util.ServiceLoader;

@SuppressWarnings("unused")
public class DummyLanguageAdapter implements LanguageAdapter {
    public DummyLanguageAdapter() {
        final var logger = LogManager.getLogger("PreloadingTricks/LanguageAdapter");
        final var prefix = String.format("[%s] ", logger.getName());
        logger.warn(
                prefix
                        + "Errors when loading preloading tricks may be intended since the implementations may targeting multiple mod loaders");
        ServiceLoaderUtil.loadServices(
                LanguageProviderCallback.class, ServiceLoader.load(LanguageProviderCallback.class), logger);
    }

    @Override
    public <T> T create(ModContainer mod, String value, Class<T> type) {
        throw new UnsupportedOperationException("This is a dummy language adapter for preloading entry points");
    }
}
