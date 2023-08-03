package settingdust.preloadingtricks;

import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DummyLanguageProvider implements IModLanguageProvider {
    public DummyLanguageProvider() {
        for (final var entrypoint : ServiceLoader.load(LanguageProviderCallback.class)) {
            Objects.requireNonNull(entrypoint);
        }
    }

    @Override
    public String name() {
        return "preloading tricks dummy";
    }

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {
        return data -> {
        };
    }

    @Override
    public <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(Supplier<R> consumeEvent) {

    }
}
