package settingdust.preloading_tricks.neoforge.v121x;

import net.lenni0451.reflect.Agents;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.forgelike.UnsafeHacks;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PreloadingTricksBootstrapper implements GraphicsBootstrapper {
    private static void appendToCurrentClassLoader(URL url) throws
                                                            NoSuchFieldException,
                                                            NoSuchMethodException,
                                                            InvocationTargetException,
                                                            IllegalAccessException {
        // 获取 AppClassLoader 实例
        var currentClassloader = Thread.currentThread().getContextClassLoader();
        var ucpField = currentClassloader.getClass().getDeclaredField("ucp");

        // 通过 Unsafe 获取字段值
        var ucp = UnsafeHacks.getField(ucpField, currentClassloader);

        // 反射调用 URLClassPath#addURL
        var addUrlMethod = ucp.getClass().getDeclaredMethod("addURL", URL.class);
        addUrlMethod.setAccessible(true);
        addUrlMethod.invoke(ucp, url);
    }

    public PreloadingTricksBootstrapper() throws
                                          URISyntaxException,
                                          IOException,
                                          NoSuchFieldException,
                                          InvocationTargetException,
                                          NoSuchMethodException,
                                          IllegalAccessException {
        var codeSource = PreloadingTricksBootstrapper.class.getProtectionDomain().getCodeSource();
        var rootPath = Paths.get(codeSource.getLocation().toURI());

        var libsDir = rootPath.resolve("libs");
        if (Files.exists(libsDir)) {
            for (final var path : Files.list(libsDir)
                                       .filter(it -> it.getFileName().toString().endsWith(".jar"))
                                       .toList()) {
                appendToCurrentClassLoader(path.toUri().toURL());
            }
        }
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
