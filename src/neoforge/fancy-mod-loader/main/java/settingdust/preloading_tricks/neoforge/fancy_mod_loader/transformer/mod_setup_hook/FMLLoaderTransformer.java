package settingdust.preloading_tricks.neoforge.fancy_mod_loader.transformer.mod_setup_hook;

import net.lenni0451.classtransform.InjectionCallback;
import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import net.neoforged.fml.classloading.JarContentsModule;
import net.neoforged.fml.classloading.transformation.ClassProcessorAuditLog;
import net.neoforged.fml.classloading.transformation.ClassProcessorSet;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.FMLLoader;
import settingdust.preloading_tricks.neoforge.fancy_mod_loader.class_transform.ClassTransformFancyModLoader;

import java.util.List;

@CTransformer(FMLLoader.class)
public class FMLLoaderTransformer {
    @CInline
    @CInject(method = "buildTransformingLoader", target = @CTarget(value = "TAIL"))
    private void preloading_tricks$afterBuildTransformingLoader(
        ClassProcessorSet classProcessorSet,
        ClassProcessorAuditLog auditTrail,
        List<JarContentsModule> content
    ) {
        ClassTransformFancyModLoader.addConfigForGameLayer(content);
    }

    @CInline
    @CInject(method = "appendLoader", target = @CTarget("TAIL"))
    private void preloading_tricks$onAppendLoader(
        String loaderName,
        List<JarContents> jars,
        InjectionCallback callback
    ) {
        ClassTransformFancyModLoader.addConfigForLayer(loaderName, jars);
    }
}
