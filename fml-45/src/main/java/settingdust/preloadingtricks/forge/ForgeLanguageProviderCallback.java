package settingdust.preloadingtricks.forge;

import net.minecraftforge.fml.loading.EarlyLoadingException;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.BackgroundScanHandler;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModValidator;
import net.minecraftforge.forgespi.locating.IModFile;
import org.slf4j.LoggerFactory;
import settingdust.preloadingtricks.LanguageProviderCallback;
import settingdust.preloadingtricks.SetupModCallback;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    }

    private void setupModsInvoking() throws IllegalAccessException {
        fieldModValidator.set(null, validator);
        candidateMods = (List<ModFile>) fieldCandidateMods.get(validator);
        final var event = new ForgeModSetupCallback();
        final var logger = LoggerFactory.getLogger("PreloadingTricks/SetupMod");
        for (final var callback : ForgeModSetupCallback.CALLBACKS) {
            logger.info("Invoking callback " + callback);
            callback.accept(event);
        }
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
            return super.stage2Validation();
        }
    }

    public class ForgeModSetupCallback implements SetupModCallback<ModFile> {
        public static final Set<Consumer<ForgeModSetupCallback>> CALLBACKS = new HashSet<>();

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
