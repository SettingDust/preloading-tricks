package settingdust.preloading_tricks.forgelike;

import settingdust.preloading_tricks.api.PreloadingEntrypoint;
import settingdust.preloading_tricks.util.class_transform.ClassTransformBootstrap;

public class ForgeLikePreloadingEntrypoint implements PreloadingEntrypoint {
    public ForgeLikePreloadingEntrypoint() {
        ClassTransformBootstrap.INSTANCE.addConfig("preloading_tricks.forgelike.classtransform.json");
    }
}
