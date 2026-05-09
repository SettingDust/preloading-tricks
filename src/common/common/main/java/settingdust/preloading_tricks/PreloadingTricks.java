package settingdust.preloading_tricks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settingdust.preloading_tricks.util.CommonIdentifier;

public final class PreloadingTricks {
    public static final String ID = "preloading_tricks";
    public static final String NAME = "Preloading Tricks";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    private PreloadingTricks() {}

    public static CommonIdentifier id(String path) {
        return CommonIdentifier.of(ID, path);
    }
}
