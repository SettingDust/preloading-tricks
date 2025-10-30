package settingdust.preloading_tricks.neoforge.fancy_mod_loader.class_transform;

import com.google.gson.Gson;
import net.neoforged.fml.classloading.JarContentsModule;
import net.neoforged.fml.jarcontents.JarContents;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformConfig;

import java.io.IOException;
import java.util.List;

public class ClassTransformFancyModLoader {
    private static final Gson gson = new Gson();

    public static void addConfig(
        String configName,
        JarContents jar
    ) throws IOException, ClassNotFoundException {
        var config = gson.fromJson(jar.get(configName).bufferedReader(), ClassTransformConfig.class);
        ClassTransformBootstrap.INSTANCE.addConfig(config);
    }

    public static void addConfig(
        JarContents jar
    ) {
        var classTransformConfigString =
            jar.getManifest().getMainAttributes().get(ClassTransformBootstrap.CLASS_TRANSFORM_CONFIG);
        if (classTransformConfigString == null) {
            return;
        }
        var classTransformConfigs = classTransformConfigString.toString().split(",");
        for (var classTransformConfig : classTransformConfigs) {
            try {
                addConfig(classTransformConfig, jar);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void addConfigForLayer(String layerName, List<JarContents> jars) {
        PreloadingTricks.LOGGER.info("Adding transformer config for layer: {}", layerName);
        for (final var jar : jars) {
            addConfig(jar);
        }
    }

    public static void addConfigForGameLayer(List<JarContentsModule> jars) {
        PreloadingTricks.LOGGER.info("Adding transformer config for layer: Game");
        for (final var jar : jars) {
            addConfig(jar.contents());
        }
    }
}
