package settingdust.preloading_tricks.lexforge;

import net.lenni0451.reflect.Agents;
import net.minecraftforge.fml.loading.FMLLoader;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;
import settingdust.preloading_tricks.modlauncher.PreloadingTricksTransformationService;

import java.io.IOException;

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
            PreloadingTricks.MOD_ID + ".lexforge.classtransform.json",
            PreloadingTricksTransformationService.class.getClassLoader()
        );

        try {
            ClassTransformBootstrap.INSTANCE.getTransformerManager().hookInstrumentation(Agents.getInstrumentation());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
