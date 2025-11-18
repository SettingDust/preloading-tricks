package settingdust.preloading_tricks.neoforge.fancy_mod_loader;

import net.lenni0451.reflect.Agents;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.forgelike.UcpClassLoaderInjector;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class PreloadingTricksBootstrapper implements GraphicsBootstrapper {
    public PreloadingTricksBootstrapper() throws URISyntaxException, IOException {
        if (!(Thread.currentThread().getContextClassLoader() instanceof URLClassLoader)) {
            PreloadingTricks.LOGGER.debug("Looks like we are in older neoforge fancy mod loader. Needn't to run");
            return;
        }

        var codeSource = PreloadingTricksBootstrapper.class.getProtectionDomain().getCodeSource();
        var rootPath = Path.of(codeSource.getLocation().toURI());
        var contents = JarContents.ofPath(rootPath);
        var prefix = "libs/boot";
        contents.visitContent(
            prefix, (relativePath, resource) -> {
                if (!relativePath.endsWith(".jar")) return;
                UcpClassLoaderInjector.inject(
                    contents.getPrimaryPath(),
                    prefix,
                    relativePath,
                    () -> {
                        try {
                            return contents.openFile(relativePath);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    FMLLoader.class.getClassLoader()
                );
            }
        );
        new ClassTransformBootstrap();
        PreloadingTricks.LOGGER.info("[{}] Installed", PreloadingTricks.NAME);
        ClassTransformBootstrap.INSTANCE.addConfig("preloading_tricks.neoforge.fml.classtransform.json");
        ClassTransformBootstrap.INSTANCE.getTransformerManager().hookInstrumentation(Agents.getInstrumentation());
    }

    @Override
    public String name() {
        return "Preloading Tricks";
    }

    @Override
    public void bootstrap(final String[] arguments) {

    }
}
