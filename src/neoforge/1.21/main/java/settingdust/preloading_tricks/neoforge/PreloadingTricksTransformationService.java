package settingdust.preloading_tricks.neoforge;

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
    public static ClassTransformBootstrap CLASS_TRANSFORM = null;

    static {
        try {
            var codeSource = PreloadingTricksTransformationService.class.getProtectionDomain().getCodeSource();
            var rootPath = Paths.get(codeSource.getLocation().toURI());

            for (final var path : Files.list(rootPath.resolve("libs"))
                                       .filter(it -> it.getFileName().toString().endsWith(".jar"))
                                       .toList()) {
                ModuleClassLoaderInjector.inject(path, IModuleLayerManager.Layer.BOOT);
            }

            if (PreloadingTricksTransformationService.class.getModule() ==
                PreloadingTricksTransformationService.class.getClassLoader().getUnnamedModule()) {
                PreloadingTricks.LOGGER.debug(
                    "[{}] There is error in PreloadingTricksTransformationService. And it's loaded from unnamed module at the second time. Need to address by the developer. Ignore this message",
                    PreloadingTricks.NAME
                );
            } else {
                ModuleClassLoaderInjector.move(
                    PreloadingTricksTransformationService.class,
                    IModuleLayerManager.Layer.BOOT
                );
                CLASS_TRANSFORM = new ClassTransformBootstrap();
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public PreloadingTricksTransformationService() throws ClassNotFoundException, IOException {
        if (CLASS_TRANSFORM == null) {
            PreloadingTricks.LOGGER.debug(
                "[{}] The `CLASS_TRANSFORM` is null. Looks like it's loaded from unnamed module at the second time. Need to address by the developer. Ignore this message",
                PreloadingTricks.NAME
            );
            return;
        }
        PreloadingTricks.LOGGER.info("[{}] Installed", PreloadingTricks.NAME);

        CLASS_TRANSFORM.addConfig(
            PreloadingTricks.MOD_ID + ".neoforge.classtransform.json",
            PreloadingTricksTransformationService.class.getClassLoader()
        );
        CLASS_TRANSFORM.getTransformerManager().hookInstrumentation(Agents.getInstrumentation());

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
    public List<? extends ITransformer<?>> transformers() {
        return List.of();
    }
}
