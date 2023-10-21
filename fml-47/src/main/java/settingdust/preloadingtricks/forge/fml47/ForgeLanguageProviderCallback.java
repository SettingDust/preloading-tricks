package settingdust.preloadingtricks.forge.fml47;

import net.minecraftforge.fml.loading.EarlyLoadingException;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.BackgroundScanHandler;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModValidator;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import settingdust.preloadingtricks.LanguageProviderCallback;
import settingdust.preloadingtricks.SetupModCallback;
import settingdust.preloadingtricks.forge.api.ForgeModSetupService;
import settingdust.preloadingtricks.util.ServiceLoaderUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
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
        // Proxy won't work with non interface
        fieldModValidator.set(null, new DummyModValidator());

        new ForgeModSetupService(candidateMods);
    }

    private void setupModsInvoking() throws IllegalAccessException {
        fieldModValidator.set(null, validator);
        candidateMods = (List<ModFile>) fieldCandidateMods.get(validator);
        ServiceLoaderUtil.loadServices(
                SetupModCallback.class,
                ServiceLoader.load(SetupModCallback.class, SetupModCallback.class.getClassLoader()),
                LogManager.getLogger("PreloadingTricks/ModSetup"));
    }

    private class DummyModValidator extends ModValidator {
        private static final Field fieldModFiles;

        private static final Field fieldBrokenFiles;

        private static final Field fieldDiscoveryErrorData;

        static {
            try {
                fieldModFiles = ModValidator.class.getDeclaredField("modFiles");
                fieldBrokenFiles = ModValidator.class.getDeclaredField("brokenFiles");
                fieldDiscoveryErrorData = ModValidator.class.getDeclaredField("discoveryErrorData");

                fieldModFiles.setAccessible(true);
                fieldBrokenFiles.setAccessible(true);
                fieldDiscoveryErrorData.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        public DummyModValidator() throws IllegalAccessException {
            super(
                    (Map<IModFile.Type, List<ModFile>>) fieldModFiles.get(validator),
                    ((List<IModFile>) fieldBrokenFiles.get(validator))
                            .stream().map(IModFile::getModFileInfo).collect(Collectors.toList()),
                    (List<EarlyLoadingException.ExceptionData>) fieldDiscoveryErrorData.get(validator));
        }

        @Override
        public BackgroundScanHandler stage2Validation() {
            try {
                setupModsInvoking();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return validator.stage2Validation();
        }
    }
}
