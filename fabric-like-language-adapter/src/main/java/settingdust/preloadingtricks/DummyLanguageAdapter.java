package settingdust.preloadingtricks;

import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.ModContainer;
import org.slf4j.LoggerFactory;
import settingdust.preloadingtricks.util.ServiceLoaderUtil;

import java.util.ServiceLoader;

@SuppressWarnings("unused")
public class DummyLanguageAdapter implements LanguageAdapter {
    public DummyLanguageAdapter() {
        ServiceLoaderUtil.loadServices(
                LanguageProviderCallback.class,
                ServiceLoader.load(LanguageProviderCallback.class),
                LoggerFactory.getLogger("PreloadingTricks/LanguageAdapter"));
    }

    @Override
    public <T> T create(ModContainer mod, String value, Class<T> type) {
        throw new UnsupportedOperationException("This is a dummy language adapter for preloading entry points");
    }
}
