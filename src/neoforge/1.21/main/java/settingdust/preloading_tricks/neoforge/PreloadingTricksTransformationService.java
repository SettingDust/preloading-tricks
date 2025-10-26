package settingdust.preloading_tricks.neoforge;

import cpw.mods.modlauncher.api.ITransformer;
import net.lenni0451.reflect.Agents;
import net.neoforged.fml.loading.FMLLoader;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;

import java.io.IOException;
import java.util.List;

public class PreloadingTricksTransformationService
    extends settingdust.preloading_tricks.modlauncher.PreloadingTricksTransformationService {

    public PreloadingTricksTransformationService() throws IOException {
        try {
            FMLLoader.class.getSimpleName();
        } catch (Throwable e) {
            throw new UnsupportedOperationException(
                "Avoid running of LexForge with NeoForge service. Just ignore this error");
        }
        init();

        ClassTransformBootstrap.INSTANCE.addConfig(
            PreloadingTricks.MOD_ID + ".neoforge.modlauncher.classtransform.json",
            PreloadingTricksTransformationService.class.getClassLoader()
        );

        ClassTransformBootstrap.INSTANCE.getTransformerManager().hookInstrumentation(Agents.getInstrumentation());
    }

    @Override
    public String name() {
        return "NeoForge Preloading Tricks";
    }

    @Override
    public List<? extends ITransformer<?>> transformers() {
        return List.of();
    }
}
