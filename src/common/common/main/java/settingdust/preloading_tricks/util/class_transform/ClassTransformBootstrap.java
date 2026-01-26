package settingdust.preloading_tricks.util.class_transform;

import com.google.gson.Gson;
import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.additionalclassprovider.InstrumentationClassProvider;
import net.lenni0451.classtransform.mixinstranslator.MixinsTranslator;
import net.lenni0451.classtransform.utils.ASMUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;

public final class ClassTransformBootstrap {
    public static final String CLASS_TRANSFORM_CONFIG = "ClassTransformConfig";
    public static ClassTransformBootstrap INSTANCE;

    private final TransformerManager transformerManager;
    private final Gson gson = new Gson();

    public ClassTransformBootstrap(Instrumentation instrumentation) throws IOException {
        if (INSTANCE != null) throw new IllegalStateException("ClassTransformBootstrap is already initialized");
        INSTANCE = this;
        transformerManager = new TransformerManager(new InstrumentationClassProvider(instrumentation));
        transformerManager.addTransformerPreprocessor(new MixinsTranslator());
    }

    public TransformerManager getTransformerManager() {
        return transformerManager;
    }

    public void addConfig(ClassTransformConfig config) throws ClassNotFoundException {
        for (final var transformer : config.transformers()) {
            var transformerClassName = config.packageName() + "." + transformer;
            transformerManager.addTransformer(
                    ASMUtils.fromBytes(transformerManager.getClassProvider().getClass(transformerClassName)),
                    true,
                    true);
        }
    }

    public void addConfig(String configName, ClassLoader classLoader) {
        try {
            var reader = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(configName)));
            var config = gson.fromJson(reader, ClassTransformConfig.class);
            addConfig(config);
            reader.close();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to read config " + configName, e);
        }
    }

    public void addConfig(String configName) {
        addConfig(configName, Thread.currentThread().getContextClassLoader());
    }
}
