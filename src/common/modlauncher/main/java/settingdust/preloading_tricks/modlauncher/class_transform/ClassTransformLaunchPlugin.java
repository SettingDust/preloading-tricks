package settingdust.preloading_tricks.modlauncher.class_transform;

import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

public class ClassTransformLaunchPlugin implements ILaunchPluginService {
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
            try {
                ClassTransformModLauncher.addConfig(ClassTransformBootstrap.INSTANCE, jar);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
