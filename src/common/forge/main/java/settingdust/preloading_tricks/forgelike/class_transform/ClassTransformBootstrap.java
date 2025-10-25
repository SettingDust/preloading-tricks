package settingdust.preloading_tricks.forgelike.class_transform;

import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.additionalclassprovider.InstrumentationClassProvider;
import net.lenni0451.classtransform.utils.ASMUtils;
import net.lenni0451.reflect.Agents;

import java.io.IOException;

public final class ClassTransformBootstrap {
    public static final String CLASS_TRANSFORM_CONFIG = "ClassTransformConfig";

    private final TransformerManager transformerManager;

    public ClassTransformBootstrap() throws IOException {
        transformerManager = new TransformerManager(new InstrumentationClassProvider(Agents.getInstrumentation()));
    }

    public TransformerManager getTransformerManager() {
        return transformerManager;
    }

    public void addConfig(ClassTransformConfig config) throws ClassNotFoundException {
        for (final var transformer : config.transformers()) {
            var transformerClassName = config.packageName() + "." + transformer;
            transformerManager.addTransformer(
                ASMUtils.fromBytes(transformerManager.getClassProvider().getClass(transformerClassName)));
        }
    }
}
