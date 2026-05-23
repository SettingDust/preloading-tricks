package settingdust.preloading_tricks.neoforge.fancy_mod_loader;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingEntrypoint;
import settingdust.preloading_tricks.api.PreloadingTricksCallbacks;
import settingdust.preloading_tricks.forgelike.UcpClassLoaderInjector;
import settingdust.preloading_tricks.forgelike.specified_forge_variant.ForgeVariants;
import settingdust.preloading_tricks.neoforge.NeoForgeAdapter;
import settingdust.preloading_tricks.util.LoaderPredicates;
import settingdust.preloading_tricks.util.ServiceLoaderUtil;
import settingdust.preloading_tricks.util.class_transform.ClassTransformBootstrap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class FancyModLoaderNeoForgeAdapter implements NeoForgeAdapter {
    public FancyModLoaderNeoForgeAdapter() {
        LoaderPredicates.NeoForge.throwIfNot();
    }

    @Override
    public void bootstrap(final Class<?> sourceClass) throws URISyntaxException, IOException {
        var rootPath = getRootPath(sourceClass);
        injectBootLibraries(rootPath);
        initClassTransform();
        PreloadingTricks.LOGGER.info("[{}] Installed", PreloadingTricks.NAME);
        addClassTransformConfig();
        loadEntrypoints();
        hookInstrumentation();
        registerSetupModsCallback(rootPath);
    }

    private static Path getRootPath(final Class<?> sourceClass) throws URISyntaxException {
        return Path.of(sourceClass.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    private static void injectBootLibraries(final Path rootPath) throws IOException {
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
    }

    private static void initClassTransform() throws IOException {
        ByteBuddyAgent.install();
        new ClassTransformBootstrap(ByteBuddyAgent.getInstrumentation());
    }

    private static void addClassTransformConfig() {
        ClassTransformBootstrap.INSTANCE.addConfig("preloading_tricks.neoforge.fml.classtransform.json");
    }

    private static void loadEntrypoints() {
        ServiceLoaderUtil.loadServices(PreloadingEntrypoint.class, false);
    }

    private static void hookInstrumentation() {
        ClassTransformBootstrap.INSTANCE
                .getTransformerManager()
                .hookInstrumentation(ByteBuddyAgent.getInstrumentation());
    }

    private static void registerSetupModsCallback(final Path rootPath) {
        PreloadingTricksCallbacks.SETUP_MODS.register(_manager -> {
            if (!(_manager instanceof final NeoForgeModManager manager)) return;
            var mod = manager.createVirtualMod(PreloadingTricks.ID, rootPath);
            manager.add(mod);
            manager.removeIf(it -> {
                var variantString = getVariant(it);
                if (variantString == null) return false;
                var variant = ForgeVariants.BY_NAME.get(variantString.toLowerCase());
                var shouldRemove = variant != null && variant != ForgeVariants.NeoForge;
                if (shouldRemove) {
                    PreloadingTricks.LOGGER.debug("Removing {} for variant {}", it.getFilePath(), variant);
                }
                return shouldRemove;
            });
        });
    }

    private static String getVariant(final ModFile mod) {
        var manifest = mod.getContents().getManifest();
        return manifest.getMainAttributes().getValue(ForgeVariants.MANIFEST_KEY);
    }
}
