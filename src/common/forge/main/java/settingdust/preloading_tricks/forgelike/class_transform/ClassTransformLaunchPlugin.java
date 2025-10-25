package settingdust.preloading_tricks.forgelike.class_transform;

import com.google.gson.Gson;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.List;

public class ClassTransformLaunchPlugin implements ILaunchPluginService {
    private final Gson gson = new Gson();
    private final ClassTransformBootstrap classTransform;

    public ClassTransformLaunchPlugin(final ClassTransformBootstrap classTransform) {
        this.classTransform = classTransform;
    }


    @Override
    public String name() {
        return "Preloading Tricks Class Transform";
    }

    @Override
    public EnumSet<Phase> handlesClass(final Type classType, final boolean isEmpty) {
        return EnumSet.noneOf(Phase.class);
    }

    @Override
    public void addResources(final List<SecureJar> resources) {
        for (final var jar : resources) {
            var classTransformConfigString =
                jar.moduleDataProvider()
                   .getManifest()
                   .getMainAttributes()
                   .get(ClassTransformBootstrap.CLASS_TRANSFORM_CONFIG);
            if (classTransformConfigString == null) continue;
            var classTransformConfigs = classTransformConfigString.toString().split(",");
            for (final var configName : classTransformConfigs) {
                try {
                    var config = gson.fromJson(
                        Files.newBufferedReader(jar.getPath(configName)),
                        ClassTransformConfig.class
                    );
                    classTransform.addConfig(config);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
