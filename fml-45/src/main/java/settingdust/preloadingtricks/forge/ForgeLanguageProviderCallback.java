package settingdust.preloadingtricks.forge;

import net.minecraftforge.fml.loading.EarlyLoadingException;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModValidator;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import settingdust.preloadingtricks.LanguageProviderCallback;
import settingdust.preloadingtricks.SetupModCallback;
import settingdust.preloadingtricks.SetupModService;
import settingdust.preloadingtricks.util.ServiceLoaderUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ForgeLanguageProviderCallback implements LanguageProviderCallback {
    private final List<ModFile> candidateMods;

    public ForgeLanguageProviderCallback() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Field fieldModValidator = FMLLoader.class.getDeclaredField("modValidator");
        fieldModValidator.setAccessible(true);

        Field fieldCandidateMods = ModValidator.class.getDeclaredField("candidateMods");
        fieldCandidateMods.setAccessible(true);

        ModValidator validator = (ModValidator) fieldModValidator.get(null);
        // Proxy won't work with non interface
        validator = ModValidator(validator);
        fieldModValidator.set(null, validator);
        candidateMods = (List<ModFile>) fieldCandidateMods.get(validator);

        ServiceLoaderUtil.loadServices(
                SetupModCallback.class,
                ServiceLoader.load(SetupModCallback.class, SetupModCallback.class.getClassLoader()),
                LogManager.getLogger("PreloadingTricks/ModSetup"));

        new ForgeModSetupService();
    }

    public record ModValidatorRecord(Integer parameters, Constructor<?> constructor) {}

    public ModValidatorRecord getValidatorConstructor() {
        Class<?> modValidatorClass = ModValidator.class;
        Constructor<?>[] constructors = modValidatorClass.getConstructors();
        for (Constructor<?> constructor : constructors) { // This method has just one constructor
            return new ModValidatorRecord(constructor.getParameterCount(), constructor);
        }
        return new ModValidatorRecord(-1, null);
    }

    public ModValidator createInstance(Constructor<?> constructor, Object[] parameters) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        return (ModValidator) constructor.newInstance(parameters);
    }

    public ModValidator ModValidator(ModValidator validator) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        Field fieldModFiles = ModValidator.class.getDeclaredField("modFiles");
        Field fieldBrokenFiles = ModValidator.class.getDeclaredField("brokenFiles");
        Field fieldDiscoveryErrorData = ModValidator.class.getDeclaredField("discoveryErrorData");

        fieldModFiles.setAccessible(true);
        fieldBrokenFiles.setAccessible(true);
        fieldDiscoveryErrorData.setAccessible(true);

        var modFiles = (Map<IModFile.Type, List<ModFile>>) fieldModFiles.get(validator);
        var brokenFiles = ((List<IModFile>) fieldBrokenFiles.get(validator)).stream().map(IModFile::getModFileInfo).collect(Collectors.toList());
        var exceptionData = (List<EarlyLoadingException.ExceptionData>) fieldDiscoveryErrorData.get(validator);


        var validatorConstructor = getValidatorConstructor();
        if (validatorConstructor.parameters == 3) {

            // forge >= 41.0.39

            // See constructor of ModValidator class
            Object[] parameters = {
                    modFiles,
                    brokenFiles,
                    exceptionData
            };

            return createInstance(validatorConstructor.constructor, parameters);
        } else if (validatorConstructor.parameters == 2) {

            // forge < 41.0.39

            // See https://github.com/MinecraftForge/MinecraftForge/blob/9523232a377e0704b2c016f0665bb15571ab5ac8/fmlloader/src/main/java/net/minecraftforge/fml/loading/moddiscovery/ModValidator.java#L31C47-L31C56
            Object[] parameters = {
                    modFiles,
                    exceptionData
            };

            ModValidator modValidator = createInstance(validatorConstructor.constructor, parameters);

            // We need to manually set this to initialize the list, if we don't do that, forge gonna crash.
            fieldBrokenFiles.set(modValidator, brokenFiles);

            return modValidator;
        }

        throw new RuntimeException("Couldn't find any valid constructor for ModValidator class");
    }

    public class ForgeModSetupService implements SetupModService<ModFile> {
        public static ForgeModSetupService INSTANCE;

        public ForgeModSetupService() {
            INSTANCE = this;
        }

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
