package settingdust.preloading_tricks.neoforge.modlauncher;

import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;
import settingdust.preloading_tricks.modlauncher.PreloadingTricksTransformationService;
import settingdust.preloading_tricks.util.LoaderPredicates;

public class TransformationServiceCallback implements
                                           settingdust.preloading_tricks.modlauncher.TransformationServiceCallback {
    @Override
    public void init() {
        if (!LoaderPredicates.NeoForgeModLauncher.strictTest()) {
            return;
        }

        PreloadingTricksTransformationService.init();

        ClassTransformBootstrap.INSTANCE.addConfig(
            PreloadingTricks.MOD_ID + ".neoforge.modlauncher.classtransform.json",
            PreloadingTricksTransformationService.class.getClassLoader()
        );
    }
}
