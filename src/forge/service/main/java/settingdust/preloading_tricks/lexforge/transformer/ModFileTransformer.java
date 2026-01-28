package settingdust.preloading_tricks.lexforge.transformer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModFile.class)
public class ModFileTransformer {
    @Shadow
    @Final
    private SecureJar jar;

    @WrapOperation(
            method = "toString",
            at = @At(value = "INVOKE", target = "Ljava/util/Objects;toString(Ljava/lang/Object;)Ljava/lang/String;"))
    private String preloading_tricks$useURI(Object o, Operation<String> original) {
        return jar.moduleDataProvider().uri().toString();
    }
}
