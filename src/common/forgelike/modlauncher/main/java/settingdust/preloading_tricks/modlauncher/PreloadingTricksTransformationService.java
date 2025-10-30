package settingdust.preloading_tricks.modlauncher;

import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import cpw.mods.niofs.union.UnionPath;
import net.lenni0451.reflect.stream.RStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;
import settingdust.preloading_tricks.modlauncher.class_transform.ClassTransformLaunchPlugin;
import settingdust.preloading_tricks.modlauncher.module_injector.ModuleClassLoaderInjector;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PreloadingTricksTransformationService implements ITransformationService {
    private static final Logger LOGGER = LogManager.getLogger(PreloadingTricks.NAME);

    public static void init() {
        try {
            var codeSource = PreloadingTricksTransformationService.class.getProtectionDomain().getCodeSource();
            var rootPath = (UnionPath) Path.of(codeSource.getLocation().toURI());

            LOGGER.info("Inject jars into BOOT layer");
            for (final var path : Files.list(rootPath.resolve("libs/boot"))
                                       .filter(it -> it.getFileName().toString().endsWith(".jar"))
                                       .toList()) {
                ModuleClassLoaderInjector.inject(path, IModuleLayerManager.Layer.BOOT);
            }

            LOGGER.info("Move self to BOOT layer");
            ModuleClassLoaderInjector.move(
                PreloadingTricksTransformationService.class,
                IModuleLayerManager.Layer.BOOT
            );

            new ClassTransformBootstrap();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public PreloadingTricksTransformationService() {
        TransformationServiceCallback.invoker.init();
        if (ClassTransformBootstrap.INSTANCE == null) {
            PreloadingTricks.LOGGER.debug(
                "The `CLASS_TRANSFORM` is null. Looks like it's loaded from unnamed module at the second time. Need to address by the developer. Ignore this message");
            return;
        }
        PreloadingTricks.LOGGER.info("{} Installed", PreloadingTricks.NAME);

        injectClassTransform();
    }

    @Override
    public List<Resource> completeScan(final IModuleLayerManager moduleLayerManager) {
        try {
            LOGGER.info("Inject jars into PLUGIN layer");
            var codeSource = PreloadingTricksTransformationService.class.getProtectionDomain().getCodeSource();
            var rootPath = (UnionPath) Path.of(codeSource.getLocation().toURI());

            for (final var path : Files.list(rootPath.resolve("libs/plugin"))
                                       .filter(it -> it.getFileName().toString().endsWith(".jar"))
                                       .toList()) {
                ModuleClassLoaderInjector.inject(path, IModuleLayerManager.Layer.PLUGIN);
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        return List.of();
    }

    private static void injectClassTransform() {
        PreloadingTricks.LOGGER.info("Inject ClassTransformLaunchPlugin");
        LaunchPluginHandler launchPlugins =
            RStream.of(Launcher.class).fields().by("launchPlugins").get(Launcher.INSTANCE);
        Map<String, ILaunchPluginService> plugins =
            RStream.of(LaunchPluginHandler.class).fields().by("plugins").get(launchPlugins);
        plugins.put("class_transform", new ClassTransformLaunchPlugin());
    }

    @Override
    public String name() {
        return PreloadingTricks.NAME;
    }

    @Override
    public void initialize(final IEnvironment environment) {}

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {}

    @Override
    public List transformers() {
        return List.of();
    }
}
