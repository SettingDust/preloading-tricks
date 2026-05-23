package settingdust.preloading_tricks.neoforge;

import java.io.IOException;
import java.net.URISyntaxException;

public interface NeoForgeAdapter {
    void bootstrap(Class<?> sourceClass) throws URISyntaxException, IOException;
}
