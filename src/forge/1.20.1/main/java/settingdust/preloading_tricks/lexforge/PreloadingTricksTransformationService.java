package settingdust.preloading_tricks.lexforge;

import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.lenni0451.reflect.Agents;
import net.lenni0451.reflect.stream.RStream;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformLaunchPlugin;
import settingdust.preloading_tricks.forgelike.module_injector.ModuleClassLoaderInjector;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PreloadingTricksTransformationService implements ITransformationService {
    public static final ClassTransformBootstrap CLASS_TRANSFORM;

    static {
        try {
            var codeSource = PreloadingTricksTransformationService.class.getProtectionDomain().getCodeSource();
            var rootPath = Paths.get(codeSource.getLocation().toURI());
            for (final var path : Files.list(rootPath.resolve("libs"))
                                       .filter(it -> it.getFileName().toString().endsWith(".jar"))
                                       .toList()) {
                ModuleClassLoaderInjector.inject(path, IModuleLayerManager.Layer.SERVICE);
            }
            CLASS_TRANSFORM = new ClassTransformBootstrap();
            CLASS_TRANSFORM.getTransformerManager().hookInstrumentation(Agents.getInstrumentation());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public PreloadingTricksTransformationService() {
        PreloadingTricks.LOGGER.info("[{}] Installed", PreloadingTricks.NAME);

        injectClassTransform();
    }

    private static void injectClassTransform() {
        PreloadingTricks.LOGGER.info("[{}] Inject ClassTransformLaunchPlugin", PreloadingTricks.NAME);
        LaunchPluginHandler launchPlugins =
            RStream.of(Launcher.class).fields().by("launchPlugins").get(Launcher.INSTANCE);
        Map<String, ILaunchPluginService> plugins =
            RStream.of(LaunchPluginHandler.class).fields().by("plugins").get(launchPlugins);
        plugins.put("class_transform", new ClassTransformLaunchPlugin(CLASS_TRANSFORM));
    }

    @Override
    public String name() {
        return "Preloading Tricks";
    }

    @Override
    public void initialize(final IEnvironment environment) {

    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {

    }

    @Override
    public List<ITransformer> transformers() {
        return List.of();
    }
}
