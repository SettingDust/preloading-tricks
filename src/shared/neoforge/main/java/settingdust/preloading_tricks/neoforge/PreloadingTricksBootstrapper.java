package settingdust.preloading_tricks.neoforge;

import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.util.ServiceLoaderUtil;

import java.io.IOException;
import java.net.URISyntaxException;

public final class PreloadingTricksBootstrapper implements GraphicsBootstrapper {
    public PreloadingTricksBootstrapper() throws URISyntaxException, IOException {
        bootstrap();
    }

    public void bootstrap() throws URISyntaxException, IOException {
        for (var service : ServiceLoaderUtil.findServices(NeoForgeAdapter.class, false)) {
            service.bootstrap(PreloadingTricksBootstrapper.class);
        }
        PreloadingTricks.LOGGER.debug("Looks like we are in wrong loader. Needn't to run");
    }

    @Override
    public String name() {
        return "Preloading Tricks";
    }

    @Override
    public void bootstrap(final String[] arguments) {
    }
}
