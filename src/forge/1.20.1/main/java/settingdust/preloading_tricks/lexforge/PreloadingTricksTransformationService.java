package settingdust.preloading_tricks.lexforge;

import cpw.mods.modlauncher.api.ITransformer;
import net.lenni0451.reflect.Agents;
import net.minecraftforge.fml.loading.FMLLoader;
import settingdust.preloading_tricks.PreloadingTricks;

import java.io.IOException;
import java.util.List;

public class PreloadingTricksTransformationService
    extends settingdust.preloading_tricks.forgelike.PreloadingTricksTransformationService {

    static {
        try {
            FMLLoader.class.getSimpleName();
        } catch (Throwable e) {
            throw new UnsupportedOperationException("Avoid running of NeoForge with LexForge service. Just ignore this error");
        }
        init();
    }

    public PreloadingTricksTransformationService() throws IOException {
        CLASS_TRANSFORM.addConfig(
            PreloadingTricks.MOD_ID + ".lexforge.classtransform.json",
            PreloadingTricksTransformationService.class.getClassLoader()
        );

        CLASS_TRANSFORM.getTransformerManager().hookInstrumentation(Agents.getInstrumentation());
    }

    @Override
    public String name() {
        return "Forge Preloading Tricks";
    }

    @Override
    public List<ITransformer> transformers() {
        return List.of();
    }
}
