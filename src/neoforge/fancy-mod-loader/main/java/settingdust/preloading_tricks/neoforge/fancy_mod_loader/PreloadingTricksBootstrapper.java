package settingdust.preloading_tricks.neoforge.fancy_mod_loader;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingEntrypoint;
import settingdust.preloading_tricks.api.PreloadingTricksCallbacks;
import settingdust.preloading_tricks.forgelike.UcpClassLoaderInjector;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;
import settingdust.preloading_tricks.forgelike.specified_forge_variant.ForgeVariants;
import settingdust.preloading_tricks.util.LoaderPredicates;
import settingdust.preloading_tricks.util.ServiceLoaderUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ServiceLoader;

public class PreloadingTricksBootstrapper implements GraphicsBootstrapper {
    public PreloadingTricksBootstrapper() throws URISyntaxException, IOException {
        if (!LoaderPredicates.NeoForge.test()) {
            PreloadingTricks.LOGGER.debug("Looks like we are in wrong loader. Needn't to run");
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

        ByteBuddyAgent.install();

        new ClassTransformBootstrap(ByteBuddyAgent.getInstrumentation());
        PreloadingTricks.LOGGER.info("[{}] Installed", PreloadingTricks.NAME);
        ClassTransformBootstrap.INSTANCE.addConfig("preloading_tricks.neoforge.fml.classtransform.json");
        ClassTransformBootstrap.INSTANCE
            .getTransformerManager()
            .hookInstrumentation(ByteBuddyAgent.getInstrumentation());

        ServiceLoaderUtil.loadServices(
            PreloadingEntrypoint.class,
            ServiceLoader.load(PreloadingEntrypoint.class, PreloadingEntrypoint.class.getClassLoader()),
            false
        );

        PreloadingTricksCallbacks.SETUP_MODS.register(_manager -> {
            if (!(_manager instanceof final NeoForgeModManager manager)) return;

            try {
                var mod = manager.createVirtualMod(
                    PreloadingTricks.MOD_ID,
                    Path.of(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI())
                );
                manager.add(mod);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            manager.removeIf(it -> {
                var manifest = it.getContents().getManifest();
                var variantString = manifest.getMainAttributes().getValue(ForgeVariants.MANIFEST_KEY);
                if (variantString == null) return false;
                var variant = ForgeVariants.BY_NAME.get(variantString.toLowerCase());
                var shouldRemove = variant != null && variant != ForgeVariants.NeoForge;
                if (shouldRemove)
                    PreloadingTricks.LOGGER.debug("Removing {} for variant {}", it.getFilePath(), variant);
                return shouldRemove;
            });
        });
    }

    @Override
    public String name() {
        return "Preloading Tricks";
    }

    @Override
    public void bootstrap(final String[] arguments) {

    }
}
