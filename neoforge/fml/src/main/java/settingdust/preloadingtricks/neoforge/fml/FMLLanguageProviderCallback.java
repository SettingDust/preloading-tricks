package settingdust.preloadingtricks.neoforge.fml;

import cpw.mods.modlauncher.api.ITransformationService;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.fml.loading.moddiscovery.ModValidator;
import net.neoforged.fml.loading.modscan.BackgroundScanHandler;
import net.neoforged.neoforgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import settingdust.preloadingtricks.LanguageProviderCallback;
import settingdust.preloadingtricks.SetupModCallback;
import settingdust.preloadingtricks.neoforge.FMLModSetupService;
import settingdust.preloadingtricks.util.ServiceLoaderUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

@SuppressWarnings("UnstableApiUsage")
public class FMLLanguageProviderCallback implements LanguageProviderCallback {
    private final Field fieldModValidator;
    private final ModValidator validator;

    private final Field fieldCandidateMods;
    private List<ModFile> candidateMods;

    public FMLLanguageProviderCallback() throws NoSuchFieldException, IllegalAccessException {
        fieldModValidator = FMLLoader.class.getDeclaredField("modValidator");
        fieldModValidator.setAccessible(true);

        fieldCandidateMods = ModValidator.class.getDeclaredField("candidateMods");
        fieldCandidateMods.setAccessible(true);

        validator = (ModValidator) fieldModValidator.get(null);
        // Proxy won't work with non-interface
        fieldModValidator.set(null, new DummyModValidator());
    }

    private void setupModsInvoking() throws IllegalAccessException {
        fieldModValidator.set(null, validator);
        candidateMods = (List<ModFile>) fieldCandidateMods.get(validator);
        new FMLModSetupService(candidateMods);
        ServiceLoaderUtil.loadServices(
                SetupModCallback.class,
                ServiceLoader.load(SetupModCallback.class, SetupModCallback.class.getClassLoader()),
                LogManager.getLogger("PreloadingTricks/ModSetup"));
    }

    private class DummyModValidator extends ModValidator {
        private static final Field fieldModFiles;

        private static final Field fieldIssues;

        static {
            try {
                fieldModFiles = ModValidator.class.getDeclaredField("modFiles");
                fieldIssues = ModValidator.class.getDeclaredField("issues");

                fieldModFiles.setAccessible(true);
                fieldIssues.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        public DummyModValidator() throws IllegalAccessException {
            super((Map<IModFile.Type, List<ModFile>>) fieldModFiles.get(validator), (List<ModLoadingIssue>) fieldIssues.get(validator));
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

        public void stage1Validation() {
            validator.stage1Validation();
        }

        public ITransformationService.Resource getPluginResources() {
            return validator.getPluginResources();
        }

        public ITransformationService.Resource getModResources() {
            return validator.getModResources();
        }
    }

}
