package settingdust.preloading_tricks.modlauncher.module_injector;

import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ModuleAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.*;

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for replacing modules in class loaders and module layers.
 *
 * <p>Provides methods to replace existing modules with new versions from JAR files.
 * Unlike {@link ModuleRemover} + {@link ModuleInjector}, this class performs
 * true in-place replacement by updating module references without removing and re-injecting.
 *
 * <p>The replacement process:
 * <ol>
 *   <li>Validates that the new JAR's module name matches the target module</li>
 *   <li>Replaces the module's JAR reference with the new JAR</li>
 *   <li>Updates the module descriptor</li>
 *   <li>Clears package certificates to allow new packages</li>
 *   <li>Updates package lookup for new packages</li>
 *   <li>Adds read edge to unnamed module if needed</li>
 * </ol>
 *
 * <p><b>Implementation based on:</b>
 * <a href="https://github.com/Sinytra/MixinTransmogrifier/blob/crabber/src/main/java/org/sinytra/mixinbooster/InstrumentationHack.java">
 * MixinTransmogrifier/InstrumentationHack.java
 * </a>
 */
public class ModuleReplacer {

    /**
     * Replaces a module with a new version from a JAR file.
     * This performs in-place replacement by updating module references.
     *
     * @param moduleName name of module to replace
     * @param newJar SecureJar containing the replacement module
     * @param layer module layer
     * @param classLoader class loader
     * @throws IllegalArgumentException if the new JAR's module name doesn't match
     * @throws RuntimeException if module not found or replacement fails
     */
    public static void replace(
        String moduleName,
        SecureJar newJar,
        ModuleLayer layer,
        ModuleClassLoader classLoader
    ) {
        // Find the module and resolved module
        Module module = layer.findModule(moduleName)
            .orElseThrow(() -> new RuntimeException("Module not found: " + moduleName));
        ResolvedModule resolvedModule = layer.configuration().findModule(moduleName)
            .orElseThrow(() -> new RuntimeException("Resolved module not found: " + moduleName));

        // Get the module reference
        ModuleReference reference = resolvedModule.reference();
        
        if (!JarModuleReferenceAccessor.isJarModuleReference(reference)) {
            throw new RuntimeException("Module does not use a jar module reference: " + moduleName);
        }

        // Get replacement data
        SecureJar.ModuleDataProvider replacementProvider = newJar.moduleDataProvider();
        ModuleDescriptor replacementDescriptor = replacementProvider.descriptor();
        Set<String> newPackages = replacementDescriptor.packages();

        // Replace JAR reference in ModuleReference
        JarModuleReferenceAccessor.setJar(reference, replacementProvider);

        // Replace descriptor
        ModuleReferenceAccessor.setDescriptor(reference, replacementDescriptor);

        // Replace location
        ModuleReferenceAccessor.setLocation(reference, newJar.getRootPath().toUri());

        // Add readability edge to unnamed module for new classes
        ModuleAccessor.implAddReads(module, module.getClassLoader().getUnnamedModule());

        // Clear package certificates
        clearPackageCertificates(module.getClassLoader(), newPackages);

        // Update package lookup
        ModuleOperationHelper.updatePackageLookupForModule(classLoader, resolvedModule, newPackages);
    }

    /**
     * Replaces a module with a new version from a JAR file path.
     *
     * @param moduleName name of module to replace
     * @param newJarPath path to JAR file containing the replacement module
     * @param layer module layer
     * @param classLoader class loader
     * @throws IllegalArgumentException if path doesn't end with .jar
     */
    public static void replace(
        String moduleName,
        Path newJarPath,
        ModuleLayer layer,
        ModuleClassLoader classLoader
    ) {
        replace(moduleName, SecureJar.from(newJarPath), layer, classLoader);
    }

    /**
     * Replaces a module with a new version from a JAR file.
     *
     * @param moduleName name of module to replace
     * @param newJar SecureJar containing the replacement module
     * @param targetLayer target layer
     */
    public static void replace(
        String moduleName,
        SecureJar newJar,
        IModuleLayerManager.Layer targetLayer
    ) {
        replace(
            moduleName,
            newJar,
            LauncherAccessor.getModuleLayer(targetLayer),
            ModuleLayerHandlerAccessor.getModuleClassLoader(targetLayer)
        );
    }

    /**
     * Replaces a module with a new version from a JAR file path.
     *
     * @param moduleName name of module to replace
     * @param newJarPath path to JAR file containing the replacement module
     * @param targetLayer target layer
     * @throws IllegalArgumentException if path doesn't end with .jar
     */
    public static void replace(
        String moduleName,
        Path newJarPath,
        IModuleLayerManager.Layer targetLayer
    ) {
        replace(moduleName, SecureJar.from(newJarPath), targetLayer);
    }

    /**
     * Replaces the module containing a class with a new version from a JAR file.
     *
     * @param classInModule class whose module should be replaced
     * @param newJar SecureJar containing the replacement module
     */
    public static void replace(Class<?> classInModule, SecureJar newJar) {
        replace(
            classInModule.getModule().getName(),
            newJar,
            classInModule.getModule().getLayer(),
            (ModuleClassLoader) classInModule.getClassLoader()
        );
    }

    /**
     * Replaces the module containing a class with a new version from a JAR file path.
     *
     * @param classInModule class whose module should be replaced
     * @param newJarPath path to JAR file containing the replacement module
     * @throws IllegalArgumentException if path doesn't end with .jar
     */
    public static void replace(Class<?> classInModule, Path newJarPath) {
        replace(classInModule, SecureJar.from(newJarPath));
    }

    /**
     * Replaces multiple modules with new versions from JAR files.
     * Each module is replaced independently.
     *
     * @param moduleNames names of modules to replace
     * @param newJars SecureJars containing the replacement modules (must match order)
     * @param layer module layer
     * @param classLoader class loader
     * @throws IllegalArgumentException if moduleNames and newJars have different lengths or module names don't match
     */
    public static void replaceMultiple(
        String[] moduleNames,
        SecureJar[] newJars,
        ModuleLayer layer,
        ModuleClassLoader classLoader
    ) {
        if (moduleNames.length != newJars.length) {
            throw new IllegalArgumentException(
                "Module names and JARs must have the same length: " +
                moduleNames.length + " vs " + newJars.length
            );
        }

        // Replace each module
        for (int i = 0; i < moduleNames.length; i++) {
            replace(moduleNames[i], newJars[i], layer, classLoader);
        }
    }

    /**
     * Replaces multiple modules with new versions from JAR file paths.
     *
     * @param moduleNames names of modules to replace
     * @param newJarPaths paths to JAR files containing the replacement modules (must match order)
     * @param layer module layer
     * @param classLoader class loader
     * @throws IllegalArgumentException if moduleNames and newJarPaths have different lengths or any path is invalid
     */
    public static void replaceMultiple(
        String[] moduleNames,
        Path[] newJarPaths,
        ModuleLayer layer,
        ModuleClassLoader classLoader
    ) {
        if (moduleNames.length != newJarPaths.length) {
            throw new IllegalArgumentException(
                "Module names and JAR paths must have the same length: " +
                moduleNames.length + " vs " + newJarPaths.length
            );
        }

        var newJars = new SecureJar[newJarPaths.length];
        for (int i = 0; i < newJarPaths.length; i++) {
            newJars[i] = SecureJar.from(newJarPaths[i]);
        }

        replaceMultiple(moduleNames, newJars, layer, classLoader);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Clears package certificates for the given packages.
     * This is necessary to allow new packages from the replacement JAR.
     *
     * @param classLoader class loader containing the packages
     * @param packages packages to clear certificates for
     */
    private static void clearPackageCertificates(ClassLoader classLoader, Set<String> packages) {
        Map<String, ?> certs = ClassLoaderAccessor.getPackage2Certs(classLoader);
        for (String pkg : packages) {
            certs.remove(pkg);
        }
    }
}
