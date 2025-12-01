package settingdust.preloading_tricks.lexforge;

import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;
import settingdust.preloading_tricks.modlauncher.PreloadingTricksTransformationService;
import settingdust.preloading_tricks.modlauncher.TransformationServiceCallback;
import settingdust.preloading_tricks.util.LoaderPredicates;

public class LexForgeTransformationServiceCallback implements TransformationServiceCallback {
    @Override
    public void init() {
        if (!LoaderPredicates.Forge.strictTest()) {
            return;
        }

        PreloadingTricksTransformationService.init();

        ClassTransformBootstrap.INSTANCE.addConfig(
            PreloadingTricks.MOD_ID + ".lexforge.classtransform.json",
            PreloadingTricksTransformationService.class.getClassLoader()
        );
    }
}
