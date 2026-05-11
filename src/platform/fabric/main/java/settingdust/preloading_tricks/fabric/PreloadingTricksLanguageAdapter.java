package settingdust.preloading_tricks.fabric;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.lenni0451.reflect.stream.RStream;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingEntrypoint;
import settingdust.preloading_tricks.util.ServiceLoaderUtil;
import settingdust.preloading_tricks.util.class_transform.ClassTransformBootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class PreloadingTricksLanguageAdapter implements LanguageAdapter {
    static {
        unlockSystemClassloader();

        ByteBuddyAgent.install();

        try {
            new ClassTransformBootstrap(ByteBuddyAgent.getInstrumentation());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PreloadingTricks.LOGGER.info("[{}] installed.", PreloadingTricks.NAME);
        ClassTransformBootstrap.INSTANCE.addConfig("preloading_tricks.fabric.classtransform.json");

        FabricLoader.getInstance().getAllMods().stream()
                .map(it -> it.findPath(JarFile.MANIFEST_NAME).orElse(null))
                .filter(Objects::nonNull)
                .map(it -> {
                    try {
                        return new Manifest(Files.newInputStream(it));
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(it -> it.getMainAttributes().getValue(ClassTransformBootstrap.CLASS_TRANSFORM_CONFIG))
                .filter(Objects::nonNull)
                .forEach(ClassTransformBootstrap.INSTANCE::addConfig);

        ServiceLoaderUtil.loadServices(
                PreloadingEntrypoint.class,
                ServiceLoader.load(PreloadingEntrypoint.class, PreloadingEntrypoint.class.getClassLoader()),
                false
        );

        ClassTransformBootstrap.INSTANCE
                .getTransformerManager()
                .hookInstrumentation(ByteBuddyAgent.getInstrumentation());

        PreloadingTricksCallbacksInvoker.onSetupLanguageAdapter();
    }

    // https://github.com/FlorianMichael/AsmFabricLoader/blob/main/src/main/java/de/florianmichael/asmfabricloader/loader/bootstrap/AFLLanguageAdapter.java#L34C1-L39C6
    // Forces the fabric.debug.disableClassPathIsolation system property to be true which disables the classpath isolation
    // This is needed because we need to access classes loaded in the system classloader
    private static void unlockSystemClassloader() {
        ((FabricLoaderImpl) FabricLoader.getInstance()).getGameProvider().unlockClassPath(Knot.getLauncher());
        RStream.of("net.fabricmc.loader.impl.launch.knot.KnotClassDelegate").fields().by("DISABLE_ISOLATION").set(true);
    }

    @Override
    public native <T> T create(final ModContainer modContainer, final String s, final Class<T> aClass);
}
