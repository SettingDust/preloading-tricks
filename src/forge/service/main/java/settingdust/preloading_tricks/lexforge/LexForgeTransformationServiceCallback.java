package settingdust.preloading_tricks.lexforge;

import net.minecraftforge.fml.loading.FMLLoader;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;
import settingdust.preloading_tricks.modlauncher.PreloadingTricksTransformationService;
import settingdust.preloading_tricks.modlauncher.TransformationServiceCallback;

public class LexForgeTransformationServiceCallback implements TransformationServiceCallback {
    @Override
    public void init() {
        try {
            FMLLoader.class.getSimpleName();
        } catch (Throwable e) {
            return;
        }
        PreloadingTricksTransformationService.init();

        ClassTransformBootstrap.INSTANCE.addConfig(
            PreloadingTricks.MOD_ID + ".lexforge.classtransform.json",
            PreloadingTricksTransformationService.class.getClassLoader()
        );
    }
}
