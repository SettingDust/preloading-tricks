package settingdust.preloading_tricks.neoforge.modlauncher;

import net.neoforged.fml.loading.FMLLoader;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;
import settingdust.preloading_tricks.modlauncher.PreloadingTricksTransformationService;

public class TransformationServiceCallback implements
                                           settingdust.preloading_tricks.modlauncher.TransformationServiceCallback {
    @Override
    public void init() {
        try {
            FMLLoader.class.getSimpleName();
        } catch (Throwable e) {
            return;
        }
        PreloadingTricksTransformationService.init();

        ClassTransformBootstrap.INSTANCE.addConfig(
            PreloadingTricks.MOD_ID + ".neoforge.modlauncher.classtransform.json",
            PreloadingTricksTransformationService.class.getClassLoader()
        );
    }
}
