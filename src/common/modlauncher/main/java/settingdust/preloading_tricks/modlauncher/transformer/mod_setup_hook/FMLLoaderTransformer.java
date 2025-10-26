package settingdust.preloading_tricks.modlauncher.transformer.mod_setup_hook;

import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;

@CTransformer(name = {"net.minecraftforge.fml.loading.FMLLoader", "net.neoforged.fml.loading.FMLLoader"})
public class FMLLoaderTransformer {
    @CInline
    @CInject(method = "completeScan", target = @CTarget("HEAD"))
    private static void preloading_tricks$onCompleteScan() {
        PreloadingTricks.LOGGER.info("PreloadingTricks calling SetupModCallback in `FMLLoader#completeScan`");
        PreloadingTricksCallback.invoker.onSetupMods();
    }
}
