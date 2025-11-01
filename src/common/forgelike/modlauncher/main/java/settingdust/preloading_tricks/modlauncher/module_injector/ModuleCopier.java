package settingdust.preloading_tricks.modlauncher.module_injector;

import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ModuleAccessor;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ModuleLayerAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.LauncherAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.ModuleClassLoaderAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.ModuleLayerHandlerAccessor;

import java.util.HashMap;

/**
 * Utility class for copying modules between class loaders and module layers.
 *
 * <p>Provides methods to duplicate modules from one context to another while maintaining:
 * <ul>
 *   <li>Module configuration and metadata</li>
 *   <li>Package lookup mappings</li>
 *   <li>Resolved module roots</li>
 * </ul>
 */
public class ModuleCopier {

    /**
     * Copies a module from source to target class loader and layer.
     *
     * @param moduleName name of module to copy
     * @param sourceLayer source module layer
     * @param targetLayer target module layer
     * @param targetClassLoader target class loader
     */
    public static void copy(
        String moduleName,
        ModuleLayer sourceLayer,
        ModuleLayer targetLayer,
        ModuleClassLoader targetClassLoader
    ) {
        var resolvedModule =
            sourceLayer.configuration().findModule(moduleName)
                       .orElseThrow(() -> new RuntimeException("Module %s not found".formatted(moduleName)));
        var module =
            sourceLayer.findModule(moduleName)
                       .orElseThrow(() -> new RuntimeException("Module %s not found".formatted(moduleName)));

        // Save reads before modifying configuration (which affects hashCode)
        var reads = resolvedModule.reads();

        ModuleAccessor.setLayer(module, targetLayer);

        var toPackageLookup = new HashMap<>(ModuleClassLoaderAccessor.getPackageLookup(targetClassLoader));
        var toResolvedRoots = new HashMap<>(ModuleClassLoaderAccessor.getResolvedRoots(targetClassLoader));
        var toConfiguration = targetLayer.configuration();

        for (final var packageName : resolvedModule.reference().descriptor().packages()) {
            toPackageLookup.put(packageName, resolvedModule);
        }

        toResolvedRoots.put(resolvedModule.name(), resolvedModule.reference());

        ModuleLayerAccessor.getNameToModule(targetLayer).put(resolvedModule.name(), module);
        ModuleOperationHelper.addModuleToConfiguration(toConfiguration, resolvedModule);

        // Establish read relationships
        ModuleOperationHelper.setupModuleReads(module, targetLayer, reads);

        ModuleClassLoaderAccessor.setPackageLookup(targetClassLoader, toPackageLookup);
        ModuleClassLoaderAccessor.setResolvedRoots(targetClassLoader, toResolvedRoots);
    }

    /**
     * Copies a module between layers.
     *
     * @param moduleName name of module to copy
     * @param sourceLayer source layer
     * @param targetLayer target layer
     */
    public static void copy(
        String moduleName,
        IModuleLayerManager.Layer sourceLayer,
        IModuleLayerManager.Layer targetLayer
    ) {
        copy(
            moduleName,
            LauncherAccessor.getModuleLayer(sourceLayer),
            LauncherAccessor.getModuleLayer(targetLayer),
            ModuleLayerHandlerAccessor.getModuleClassLoader(targetLayer)
        );
    }

    /**
     * Copies the module containing a class to target class loader and layer.
     *
     * @param classInModule class whose module should be copied
     * @param targetLayer target module layer
     * @param targetClassLoader target class loader
     */
    public static void copy(
        Class<?> classInModule,
        ModuleLayer targetLayer,
        ModuleClassLoader targetClassLoader
    ) {
        copy(
            classInModule.getModule().getName(),
            classInModule.getModule().getLayer(),
            targetLayer,
            targetClassLoader
        );
    }

    /**
     * Copies the module containing a class to target layer.
     *
     * @param classInModule class whose module should be copied
     * @param targetLayer target layer
     */
    public static void copy(Class<?> classInModule, IModuleLayerManager.Layer targetLayer) {
        copy(
            classInModule,
            LauncherAccessor.getModuleLayer(targetLayer),
            ModuleLayerHandlerAccessor.getModuleClassLoader(targetLayer)
        );
    }
}
