package settingdust.preloadingtricks.fabric;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import org.apache.logging.log4j.LogManager;
import settingdust.preloadingtricks.LanguageProviderCallback;
import settingdust.preloadingtricks.SetupModCallback;
import settingdust.preloadingtricks.util.ServiceLoaderUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.ServiceLoader;

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

        new FabricModSetupService(mods);
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
}
