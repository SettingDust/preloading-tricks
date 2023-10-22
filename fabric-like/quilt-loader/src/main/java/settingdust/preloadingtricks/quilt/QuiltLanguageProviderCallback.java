package settingdust.preloadingtricks.quilt;

import org.apache.logging.log4j.LogManager;
import org.quiltmc.loader.api.plugin.ModContainerExt;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import settingdust.preloadingtricks.LanguageProviderCallback;
import settingdust.preloadingtricks.SetupModCallback;
import settingdust.preloadingtricks.util.ServiceLoaderUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.ServiceLoader;

public class QuiltLanguageProviderCallback implements LanguageProviderCallback {
    private final Field fieldMods;

    private final QuiltLoaderImpl loader = QuiltLoaderImpl.INSTANCE;
    private final List<ModContainerExt> mods;

    public QuiltLanguageProviderCallback() throws NoSuchFieldException, IllegalAccessException {
        fieldMods = QuiltLoaderImpl.class.getDeclaredField("mods");
        fieldMods.setAccessible(true);

        mods = (List<ModContainerExt>) fieldMods.get(loader);

        fieldMods.set(
                loader,
                Proxy.newProxyInstance(
                        mods.getClass().getClassLoader(), mods.getClass().getInterfaces(), new ModsListProxy()));

        new QuiltModSetupService(mods);
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
