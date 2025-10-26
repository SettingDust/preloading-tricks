package settingdust.preloading_tricks.modlauncher.class_transform;

import com.google.gson.Gson;
import cpw.mods.jarhandling.SecureJar;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformConfig;

import java.io.IOException;
import java.nio.file.Files;

public class ClassTransformModLauncher {
    private static final Gson gson = new Gson();

    public static void addConfig(
        ClassTransformBootstrap classTransform,
        String configName,
        SecureJar jar
    ) throws IOException, ClassNotFoundException {
        var config = gson.fromJson(Files.newBufferedReader(jar.getPath(configName)), ClassTransformConfig.class);
        classTransform.addConfig(config);
    }

    public static void addConfig(
        ClassTransformBootstrap classTransform,
        SecureJar jar
    ) throws IOException, ClassNotFoundException {
        var classTransformConfigString =
            jar.moduleDataProvider()
               .getManifest()
               .getMainAttributes()
               .get(ClassTransformBootstrap.CLASS_TRANSFORM_CONFIG);
        if (classTransformConfigString == null) return;
        var classTransformConfigs = classTransformConfigString.toString().split(",");
        for (final var configName : classTransformConfigs) {
            addConfig(classTransform, configName, jar);
        }
    }
}
