package settingdust.preloadingtricks.quilt;

import org.quiltmc.loader.api.plugin.ModContainerExt;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
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
    }

    private void setupModsInvoking() throws IllegalAccessException {
        fieldMods.set(loader, mods);
        final var event = new QuiltModSetupCallback();
        for (final var callback : QuiltModSetupCallback.CALLBACKS) {
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

    public class QuiltModSetupCallback implements SetupModCallback<ModContainerExt> {
        public static final Set<Consumer<QuiltModSetupCallback>> CALLBACKS = new HashSet<>();

        @Override
        public Collection<ModContainerExt> all() {
            return mods;
        }

        @Override
        public void add(ModContainerExt modContainer) {
            mods.add(modContainer);
        }

        @Override
        public void addAll(Collection<ModContainerExt> mod) {
            mods.addAll(mod);
        }

        @Override
        public void remove(ModContainerExt modContainer) {
            mods.remove(modContainer);
        }

        @Override
        public void removeIf(Predicate<ModContainerExt> predicate) {
            mods.removeIf(predicate);
        }

        @Override
        public void removeAll(Collection<ModContainerExt> modContainers) {
            mods.removeAll(modContainers);
        }
    }
}
