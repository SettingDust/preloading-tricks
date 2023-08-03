package settingdust.preloadingtricks.forge;

import com.google.common.collect.Sets;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModValidator;
import settingdust.preloadingtricks.LanguageProviderCallback;
import settingdust.preloadingtricks.SetupModCallback;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ForgeLanguageProviderCallback implements LanguageProviderCallback {
    private final Field fieldModValidator;
    private final ModValidator validator;

    private final Field fieldCandidateMods;
    private List<ModFile> candidateMods;


    public ForgeLanguageProviderCallback() throws NoSuchFieldException, IllegalAccessException {
        fieldModValidator = FMLLoader.class.getDeclaredField("modValidator");
        fieldModValidator.setAccessible(true);


        fieldCandidateMods = ModValidator.class.getDeclaredField("candidateMods");
        fieldCandidateMods.setAccessible(true);

        validator = (ModValidator) fieldModValidator.get(null);
        fieldModValidator.set(null, Proxy.newProxyInstance(
                validator.getClass().getClassLoader(),
                validator.getClass().getInterfaces(),
                new ModValidatorProxy()
        ));
    }

    private void setupModsInvoking() throws IllegalAccessException {
        fieldModValidator.set(null, validator);
        candidateMods = (List<ModFile>) fieldCandidateMods.get(validator);
        final var event = new ForgeModSetupCallback();
        for (final var callback : ForgeModSetupCallback.CALLBACKS) {
            callback.accept(event);
        }
    }


    private class ModValidatorProxy implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("stage2Validation")) setupModsInvoking();
            return method.invoke(validator, args);
        }
    }

    public class ForgeModSetupCallback implements SetupModCallback<ModFile> {
        public static final Set<Consumer<ForgeModSetupCallback>> CALLBACKS = Sets.newHashSet();

        @Override
        public Collection<ModFile> all() {
            return candidateMods;
        }

        @Override
        public void add(ModFile modFile) {
            candidateMods.add(modFile);
        }

        @Override
        public void addAll(Collection<ModFile> mod) {
            candidateMods.addAll(mod);
        }

        @Override
        public void remove(ModFile modFile) {
            candidateMods.remove(modFile);
        }

        @Override
        public void removeIf(Predicate<ModFile> predicate) {
            candidateMods.removeIf(predicate);
        }

        @Override
        public void removeAll(Collection<ModFile> modFiles) {
            candidateMods.removeAll(modFiles);
        }
    }
}
