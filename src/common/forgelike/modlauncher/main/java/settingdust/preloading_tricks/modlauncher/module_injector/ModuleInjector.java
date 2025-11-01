package settingdust.preloading_tricks.modlauncher.module_injector;

import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ConfigurationAccessor;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ModuleLayerAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.LauncherAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.ModuleClassLoaderAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.ModuleLayerHandlerAccessor;

import java.lang.module.Configuration;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Main facade for module injection operations.
 *
 * <p>Provides a unified API for injecting Java modules into ModuleClassLoader and ModuleLayer contexts.
 *
 * <p>All injected modules will have automatic mutual read relationships established.
 */
public class ModuleInjector {

    // ==================== Module Injection ====================

    /**
     * Injects modules from a configuration into target class loader and module layer.
     * All injected modules will have mutual read relationships and can read existing modules.
     * All existing modules in the target layer will also be able to read the newly injected modules.
     *
     * @param moduleConfig configuration containing modules to inject
     * @param targetClassLoader target class loader
     * @param targetLayer target module layer
     */
    public static void inject(
        Configuration moduleConfig,
        ModuleClassLoader targetClassLoader,
        ModuleLayer targetLayer
    ) {
        var targetConfiguration = ModuleClassLoaderAccessor.getConfiguration(targetClassLoader);
        ModuleOperationHelper.mergeConfigurations(targetConfiguration, moduleConfig);

        var allResolvedModules = ConfigurationAccessor.getModules(moduleConfig);

        for (var resolvedModule : allResolvedModules) {
            // Save reads before modifying cf field (which affects hashCode)
            var reads = resolvedModule.reads();

            // Create and register module
            var module = ModuleOperationHelper.createAndRegisterModule(
                resolvedModule,
                targetLayer,
                targetClassLoader,
                targetConfiguration
            );

            // Establish read relationships
            ModuleOperationHelper.setupModuleReads(module, targetLayer, reads);
        }

        ModuleLayerAccessor.clearModules(targetLayer);

        // Update class loader metadata
        ModuleOperationHelper.updatePackageLookup(targetClassLoader, allResolvedModules);
        ModuleOperationHelper.updateResolvedRoots(targetClassLoader, allResolvedModules);
    }

    /**
     * Injects a single module from JAR into target class loader and layer.
     *
     * @param jar SecureJar containing the module
     * @param targetClassLoader target class loader
     * @param targetLayer target module layer
     */
    public static void inject(SecureJar jar, ModuleClassLoader targetClassLoader, ModuleLayer targetLayer) {
        var moduleConfiguration = ModuleClassLoaderAccessor.getConfiguration(targetClassLoader);
        var jarConfiguration = ModuleConfigurationCreator.createConfiguration(jar, moduleConfiguration);
        inject(jarConfiguration, targetClassLoader, targetLayer);
    }

    /**
     * Injects modules from JAR paths into target class loader and layer.
     *
     * @param targetClassLoader target class loader
     * @param targetLayer target module layer
     * @param jarPaths JAR file paths (varargs)
     * @throws IllegalArgumentException if any path doesn't end with .jar
     */
    public static void inject(ModuleClassLoader targetClassLoader, ModuleLayer targetLayer, Path... jarPaths) {
        var jars = Arrays.stream(jarPaths)
                         .map(SecureJar::from)
                         .toArray(SecureJar[]::new);
        var moduleConfiguration = ModuleClassLoaderAccessor.getConfiguration(targetClassLoader);
        var jarConfiguration = ModuleConfigurationCreator.createConfiguration(moduleConfiguration, jars);
        inject(jarConfiguration, targetClassLoader, targetLayer);
    }

    /**
     * Injects modules from JARs into target class loader and layer.
     *
     * @param targetClassLoader target class loader
     * @param targetLayer target module layer
     * @param jars SecureJars (varargs)
     */
    public static void inject(ModuleClassLoader targetClassLoader, ModuleLayer targetLayer, SecureJar... jars) {
        var moduleConfiguration = ModuleClassLoaderAccessor.getConfiguration(targetClassLoader);
        var jarConfiguration = ModuleConfigurationCreator.createConfiguration(moduleConfiguration, jars);
        inject(jarConfiguration, targetClassLoader, targetLayer);
    }

    /**
     * Injects modules from JAR paths into a layer.
     *
     * @param targetLayer target layer
     * @param jarPaths JAR file paths (varargs)
     */
    public static void inject(IModuleLayerManager.Layer targetLayer, Path... jarPaths) {
        inject(
            ModuleLayerHandlerAccessor.getModuleClassLoader(targetLayer),
            LauncherAccessor.getModuleLayer(targetLayer),
            jarPaths
        );
    }

    /**
     * Injects modules from JARs into a layer.
     *
     * @param targetLayer target layer
     * @param jars SecureJars (varargs)
     */
    public static void inject(IModuleLayerManager.Layer targetLayer, SecureJar... jars) {
        inject(
            ModuleLayerHandlerAccessor.getModuleClassLoader(targetLayer),
            LauncherAccessor.getModuleLayer(targetLayer),
            jars
        );
    }

    /**
     * Injects modules from JAR paths into the same layer as the target class.
     *
     * @param targetClass class whose layer will receive the modules
     * @param jarPaths JAR file paths (varargs)
     */
    public static void inject(Class<?> targetClass, Path... jarPaths) {
        inject(
            (ModuleClassLoader) targetClass.getClassLoader(),
            targetClass.getModule().getLayer(),
            jarPaths
        );
    }

    /**
     * Injects modules from JARs into the same layer as the target class.
     *
     * @param targetClass class whose layer will receive the modules
     * @param jars SecureJars (varargs)
     */
    public static void inject(Class<?> targetClass, SecureJar... jars) {
        inject(
            (ModuleClassLoader) targetClass.getClassLoader(),
            targetClass.getModule().getLayer(),
            jars
        );
    }
}
