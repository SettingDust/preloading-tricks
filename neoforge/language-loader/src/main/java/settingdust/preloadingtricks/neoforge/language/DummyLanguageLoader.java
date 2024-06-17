package settingdust.preloadingtricks.neoforge.language;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.IModLanguageLoader;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import settingdust.preloadingtricks.LanguageProviderCallback;
import settingdust.preloadingtricks.util.ServiceLoaderUtil;

import java.util.ServiceLoader;

public class DummyLanguageLoader implements IModLanguageLoader {
    static {
        final var logger = LogManager.getLogger("PreloadingTricks/LanguageProvider");
        final var prefix = String.format("[%s] ", logger.getName());
        logger.warn(
                prefix
                        + "Errors when loading preloading tricks may be intended since implementations may targeting multiple mod loaders");
        // Why forge constructs the instance twice?
        ServiceLoaderUtil.loadServices(
                LanguageProviderCallback.class,
                ServiceLoader.load(LanguageProviderCallback.class, DummyLanguageLoader.class.getClassLoader()),
                logger);
    }

    @Override
    public String name() {
        return "preloading tricks dummy";
    }

    @Override
    public String version() {
        return "1";
    }

    @Override
    public ModContainer loadMod(
        final IModInfo info,
        final ModFileScanData modFileScanResults,
        final ModuleLayer layer
    ) throws ModLoadingException {
        throw new ModLoadingException(ModLoadingIssue.error("preloading_tricks.language_adapter.shouldnt_be_used"));
    }
}
