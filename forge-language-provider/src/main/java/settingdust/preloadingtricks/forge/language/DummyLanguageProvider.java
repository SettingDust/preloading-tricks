package settingdust.preloadingtricks.forge.language;

import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.slf4j.LoggerFactory;
import settingdust.preloadingtricks.LanguageProviderCallback;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DummyLanguageProvider implements IModLanguageProvider {
    static {
        // Why forge construct the instance twice?
        final var logger = LoggerFactory.getLogger("PreloadingTricks/LanguageProvider");
        final var serviceLoader =
                ServiceLoader.load(LanguageProviderCallback.class, DummyLanguageProvider.class.getClassLoader());
        serviceLoader.stream()
                .map(p -> {
                    try {
                        return p.get();
                    } catch (Throwable t) {
                        logger.debug("Invoke callback " + p.type().getName() + " failed", t);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(callback -> logger.info("Invoking callback " + callback));
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
