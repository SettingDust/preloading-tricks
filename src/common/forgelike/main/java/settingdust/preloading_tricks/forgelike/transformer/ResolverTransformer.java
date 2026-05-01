package settingdust.preloading_tricks.forgelike.transformer;

import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CLocalVariable;
import net.lenni0451.classtransform.annotations.CSlice;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.injection.CWrapCatch;
import org.spongepowered.asm.mixin.Mixin;

import java.lang.module.ModuleDescriptor;
import java.lang.module.ResolutionException;
import java.lang.module.ResolvedModule;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Mixin(targets = "java.lang.module.Resolver")
public class ResolverTransformer {
    @CInline
    @CWrapCatch(
            value = "checkExportSuppliers",
            slice = @CSlice(
                    to = @CTarget(
                            value = "INVOKE",
                            ordinal = 0,
                            target = "Ljava/lang/module/ModuleDescriptor;isAutomatic()Z")),
            target = "Ljava/lang/module/Resolver;resolveFail(Ljava/lang/String;[Ljava/lang/Object;)V")
    private void preloading_tricks$logMref0(
            ResolutionException exception,
            @CLocalVariable(index = 0) Map<ResolvedModule, Set<ResolvedModule>> graph,
            @CLocalVariable ResolvedModule endpoint) {
        Set<ResolvedModule> relateModules = new HashSet<>();
        for (var entry : graph.entrySet()) {
            if (entry.getKey().name().equals(endpoint.name())) {
                relateModules.add(entry.getKey());
            }
        }
        var info = new StringBuilder();
        info.append("Relate modules path:").append('\n');
        for (var module : relateModules) {
            info.append(" - ")
                    .append(module.name())
                    .append('(')
                    .append(module.reference().location().orElse(null))
                    .append(')')
                    .append('\n');
        }
        throw new IllegalStateException(info.toString(), exception);
    }

    @CInline
    @CWrapCatch(
            value = "checkExportSuppliers",
            target = "Ljava/lang/module/Resolver;failTwoSuppliers(Ljava/lang/module/ModuleDescriptor;Ljava/lang/String;Ljava/lang/module/ModuleDescriptor;Ljava/lang/module/ModuleDescriptor;)V")
    private void preloading_tricks$logMref1(
            ResolutionException exception,
            @CLocalVariable Map<ResolvedModule, Set<ResolvedModule>> graph,
            @CLocalVariable ResolvedModule endpoint,
            @CLocalVariable ModuleDescriptor supplier) {
        Set<ResolvedModule> relateModules = new HashSet<>();
        relateModules.add(endpoint);
        for (var entry : graph.entrySet()) {
            if (entry.getKey().name().equals(supplier.name())) {
                relateModules.add(entry.getKey());
            }
        }
        var info = new StringBuilder();
        info.append("Relate modules path:").append('\n');
        for (var module : relateModules) {
            info.append(" - ")
                    .append(module.name())
                    .append('(')
                    .append(module.reference().location().orElse(null))
                    .append(')')
                    .append('\n');
        }
        throw new IllegalStateException(info.toString(), exception);
    }
}
