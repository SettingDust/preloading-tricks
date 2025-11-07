package settingdust.preloading_tricks.neoforge.fancy_mod_loader;

import net.lenni0451.reflect.Agents;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import net.neoforged.neoforgespi.locating.ModFileLoadingException;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.forgelike.JavaBypass;
import settingdust.preloading_tricks.forgelike.UnsafeHacks;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class PreloadingTricksBootstrapper implements GraphicsBootstrapper {
    private static void appendToClassLoader(URL url, ClassLoader classLoader) throws Throwable {
        Field ucpField;
        try {
            ucpField = classLoader.getClass().getDeclaredField("ucp");
        } catch (NoSuchFieldException e) {
            ucpField = classLoader.getClass().getSuperclass().getDeclaredField("ucp");
        }

        var ucp = UnsafeHacks.getField(ucpField, classLoader);
        var ucpClass = ucp.getClass();

        var addURLHandle = JavaBypass.getTrustedLookup().in(ucpClass).findVirtual(
            ucpClass,
            "addURL",
            MethodType.methodType(void.class, URL.class)
        );
        addURLHandle.invoke(ucp, url);
    }

    private static String extractEmbeddedJarFile(JarContents contents, String relativePath, Path destination) {
        try (var inStream = contents.openFile(relativePath); var outStream = Files.newOutputStream(destination)) {
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Missing default JCA algorithm SHA-256.", e);
            }

            var digestOut = new DigestOutputStream(outStream, digest);
            inStream.transferTo(digestOut);

            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException e) {
            PreloadingTricks.LOGGER.error(
                "Failed to copy nested jar file {} from file {} to {}",
                relativePath,
                contents.getPrimaryPath(),
                destination,
                e
            );
            throw new ModFileLoadingException("Failed to load mod file " + contents.getPrimaryPath().getFileName(), e);
        }
    }

    /**
     * Atomically moves the extracted embedded jar file to its final location.
     * If an atomic move is not supported, the file will be moved normally.
     */
    private static void moveExtractedFileIntoPlace(Path source, Path destination) {
        try {
            Files.createDirectories(destination.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException(
                "Failed to create parent directory for extracted nested jar file " + source + " at " + destination, e);
        }

        try {
            try {
                Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(
                "Failed to move temporary nested file " + source + " to its final location " + destination, e);
        }
    }

    public PreloadingTricksBootstrapper() throws URISyntaxException, IOException {
        if (!(Thread.currentThread().getContextClassLoader() instanceof URLClassLoader)) {
            PreloadingTricks.LOGGER.debug("Looks like we are in older neoforge fancy mod loader. Needn't to run");
            return;
        }

        var codeSource = PreloadingTricksBootstrapper.class.getProtectionDomain().getCodeSource();
        var rootPath = Path.of(codeSource.getLocation().toURI());
        var contents = JarContents.ofPath(rootPath);
        var cachePath = Path.of(".cache", PreloadingTricks.MOD_ID);
        try {Files.createDirectories(cachePath);} catch (IOException ignored) {}
        var prefix = "libs/boot";
        contents.visitContent(
            prefix, (relativePath, resource) -> {
                if (!relativePath.endsWith(".jar")) return;
                Path tempFile;
                try {
                    tempFile = Files.createTempFile(cachePath, "_nested", ".tmp");
                } catch (IOException e) {
                    throw new IllegalStateException(
                        "Failed to create a temporary file for nested jar in " + cachePath + ": " + e);
                }
                try {
                    var hash = extractEmbeddedJarFile(contents, relativePath, tempFile);
                    var filename = relativePath.substring(prefix.length() + 1);
                    Path finalPath = cachePath.resolve(hash + "/" + filename);
                    // If the file already exists, reuse it, since it might already be opened.
                    if (!Files.isRegularFile(finalPath)) {
                        moveExtractedFileIntoPlace(tempFile, finalPath);
                    }
                    try {
                        appendToClassLoader(finalPath.toUri().toURL(), FMLLoader.class.getClassLoader());
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                } finally {
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (IOException e) {
                        PreloadingTricks.LOGGER.error("Failed to remove temporary file {}: {}", tempFile, e);
                    }
                }
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
