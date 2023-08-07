package settingdust.preloadingtricks.fabric;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import org.apache.logging.log4j.LogManager;
import settingdust.preloadingtricks.LanguageProviderCallback;
import settingdust.preloadingtricks.SetupModCallback;
import settingdust.preloadingtricks.SetupModService;
import settingdust.preloadingtricks.util.ServiceLoaderUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
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

        new FabricModSetupService();
    }

    private void setupModsInvoking() throws IllegalAccessException {
        fieldMods.set(loader, mods);
        ServiceLoaderUtil.loadServices(
                SetupModCallback.class,
                ServiceLoader.load(SetupModCallback.class),
                LogManager.getLogger("PreloadingTricks/ModSetup"));
    }

    private class ModsListProxy implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("iterator")) setupModsInvoking();
            return method.invoke(mods, args);
        }
    }

    public class FabricModSetupService implements SetupModService<ModContainerImpl> {
        public static FabricModSetupService INSTANCE;

        public FabricModSetupService() {
            INSTANCE = this;
        }

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
