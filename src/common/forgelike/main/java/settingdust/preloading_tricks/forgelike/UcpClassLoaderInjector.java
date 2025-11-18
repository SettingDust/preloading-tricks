package settingdust.preloading_tricks.forgelike;

import settingdust.preloading_tricks.PreloadingTricks;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.function.Supplier;

public class UcpClassLoaderInjector {
    public static void inject(
        Path primaryPath,
        String prefix,
        String relativePath,
        Supplier<InputStream> fileSupplier,
        ClassLoader classLoader
    ) {
        var cachePath = Path.of(".cache", PreloadingTricks.MOD_ID);
        try {Files.createDirectories(cachePath);} catch (IOException ignored) {}
        Path tempFile;
        try {
            tempFile = Files.createTempFile(cachePath, "_nested", ".tmp");
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to create a temporary file for nested jar in " + cachePath + ": " + e);
        }
        try {
            var hash = extractEmbeddedJarFile(primaryPath, fileSupplier.get(), relativePath, tempFile);
            var filename = relativePath.substring(prefix.length() + 1);
            Path finalPath = cachePath.resolve(hash + "/" + filename);
            // If the file already exists, reuse it, since it might already be opened.
            if (!Files.isRegularFile(finalPath)) {
                moveExtractedFileIntoPlace(tempFile, finalPath);
            }
            try {
                appendToClassLoader(finalPath.toUri().toURL(), classLoader);
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

    public static void appendToClassLoader(URL url, ClassLoader classLoader) throws Throwable {
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

    private static String extractEmbeddedJarFile(
        Path primaryPath,
        InputStream fileStream,
        String relativePath,
        Path destination
    ) {
        try (var inStream = fileStream; var outStream = Files.newOutputStream(destination)) {
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
                primaryPath,
                destination,
                e
            );
            throw new IllegalStateException("Failed to load mod file " + primaryPath.getFileName(), e);
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
}
