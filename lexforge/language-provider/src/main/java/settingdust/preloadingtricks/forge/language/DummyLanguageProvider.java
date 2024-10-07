package settingdust.preloadingtricks.forge.language;

import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import settingdust.preloadingtricks.LanguageProviderCallback;
import settingdust.preloadingtricks.util.ServiceLoaderUtil;

import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DummyLanguageProvider implements IModLanguageProvider {
    static {
        final var logger = LogManager.getLogger("PreloadingTricks/LanguageProvider");
        final var prefix = String.format("[%s] ", logger.getName());
        logger.warn(
            "{}Errors when loading preloading tricks may be intended since implementations may targeting multiple mod loaders",
            prefix);
        // Why forge construct the instance twice?
        ServiceLoaderUtil.loadServices(
                LanguageProviderCallback.class,
                ServiceLoader.load(LanguageProviderCallback.class, DummyLanguageProvider.class.getClassLoader()),
                logger);
    }

    public DummyLanguageProvider() {}

    @Override
    public String name() {
        return "preloading tricks dummy";
    }

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {
        return data -> {};
    }

    @Override
    public <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(Supplier<R> consumeEvent) {}
}
