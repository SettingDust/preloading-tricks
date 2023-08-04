package settingdust.preloadingtricks.fabric;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import org.slf4j.LoggerFactory;
import settingdust.preloadingtricks.LanguageProviderCallback;
import settingdust.preloadingtricks.SetupModCallback;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FabricLanguageProviderCallback implements LanguageProviderCallback {
    private final Field fieldMods;

    private final FabricLoaderImpl loader = FabricLoaderImpl.INSTANCE;
    private final List<ModContainerImpl> mods;

    public FabricLanguageProviderCallback() throws NoSuchFieldException, IllegalAccessException {
        fieldMods = FabricLoaderImpl.class.getDeclaredField("mods");
        fieldMods.setAccessible(true);

        mods = (List<ModContainerImpl>) fieldMods.get(loader);

        fieldMods.set(
                loader,
                Proxy.newProxyInstance(
                        mods.getClass().getClassLoader(), mods.getClass().getInterfaces(), new ModsListProxy()));
    }

    private void setupModsInvoking() throws IllegalAccessException {
        fieldMods.set(loader, mods);
        final var event = new FabricModSetupCallback();
        final var logger = LoggerFactory.getLogger("PreloadingTricks/ModSetup");
        for (final var callback : FabricModSetupCallback.CALLBACKS) {
            logger.info("Invoking callback " + callback);
            callback.accept(event);
        }
    }

    private class ModsListProxy implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("iterator")) setupModsInvoking();
            return method.invoke(mods, args);
        }
    }

    public class FabricModSetupCallback implements SetupModCallback<ModContainerImpl> {
        public static final Set<Consumer<FabricModSetupCallback>> CALLBACKS = new HashSet<>();

        @Override
        public Collection<ModContainerImpl> all() {
            return mods;
        }

        @Override
        public void add(ModContainerImpl modContainer) {
            mods.add(modContainer);
        }

        @Override
        public void addAll(Collection<ModContainerImpl> mod) {
            mods.addAll(mod);
        }

        @Override
        public void remove(ModContainerImpl modContainer) {
            mods.remove(modContainer);
        }

        @Override
        public void removeIf(Predicate<ModContainerImpl> predicate) {
            mods.removeIf(predicate);
        }

        @Override
        public void removeAll(Collection<ModContainerImpl> modContainers) {
            mods.removeAll(modContainers);
        }
    }
}
