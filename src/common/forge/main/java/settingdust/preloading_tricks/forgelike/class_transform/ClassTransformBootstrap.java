package settingdust.preloading_tricks.forgelike.class_transform;

import com.google.gson.Gson;
import cpw.mods.jarhandling.SecureJar;
import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.additionalclassprovider.InstrumentationClassProvider;
import net.lenni0451.classtransform.utils.ASMUtils;
import net.lenni0451.reflect.Agents;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

public final class ClassTransformBootstrap {
    public static final String CLASS_TRANSFORM_CONFIG = "ClassTransformConfig";

    private final TransformerManager transformerManager;
    private final Gson gson = new Gson();

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

    public void addConfig(String configName, SecureJar jar) throws IOException, ClassNotFoundException {
        var config = gson.fromJson(Files.newBufferedReader(jar.getPath(configName)), ClassTransformConfig.class);
        addConfig(config);
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

    public void addConfig(String configName) throws ClassNotFoundException {
        addConfig(configName, Thread.currentThread().getContextClassLoader());
    }
}
