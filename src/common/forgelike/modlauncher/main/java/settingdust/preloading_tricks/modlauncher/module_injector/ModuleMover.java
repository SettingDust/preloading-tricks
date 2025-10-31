package settingdust.preloading_tricks.modlauncher.module_injector;

import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ModuleLayerAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.LauncherAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.ModuleLayerHandlerAccessor;

/**
 * Utility class for moving modules between class loaders and module layers.
 *
 * <p>Provides methods to transfer modules from one context to another by:
 * <ul>
 *   <li>Copying the module to the target layer (via {@link ModuleCopier})</li>
 *   <li>Removing the module from the source layer</li>
 *   <li>Cleaning up package lookup and resolved roots in source class loader</li>
 * </ul>
 */
public class ModuleMover {

    /**
     * Moves a module from source to target class loader and layer.
     *
     * @param moduleName name of module to move
     * @param sourceLayer source module layer
     * @param sourceClassLoader source class loader
     * @param targetLayer target module layer
     * @param targetClassLoader target class loader
     */
    public static void move(
        String moduleName,
        ModuleLayer sourceLayer,
        ModuleClassLoader sourceClassLoader,
        ModuleLayer targetLayer,
        ModuleClassLoader targetClassLoader
    ) {
        var resolvedModule =
            sourceLayer.configuration().findModule(moduleName)
                       .orElseThrow(() -> new RuntimeException("Module %s not found".formatted(moduleName)));

        // Copy module to target layer
        ModuleCopier.copy(moduleName, sourceLayer, targetLayer, targetClassLoader);

        // Remove from source layer's nameToModule
        ModuleLayerAccessor.getNameToModule(sourceLayer).remove(moduleName);

        // Remove from source configuration
        ModuleOperationHelper.removeModuleFromConfiguration(sourceLayer.configuration(), resolvedModule);
    }

    /**
     * Moves a module between layers.
     * Currently implemented as a copy operation.
     * 
     * @param moduleName name of module to move
     * @param sourceLayer source layer
     * @param targetLayer target layer
     */
    public static void move(String moduleName, IModuleLayerManager.Layer sourceLayer, IModuleLayerManager.Layer targetLayer) {
        move(
            moduleName,
            LauncherAccessor.getModuleLayer(sourceLayer),
            ModuleLayerHandlerAccessor.getModuleClassLoader(sourceLayer),
            LauncherAccessor.getModuleLayer(targetLayer),
            ModuleLayerHandlerAccessor.getModuleClassLoader(targetLayer)
        );
    }

    /**
     * Moves the module containing a class to target class loader and layer.
     * Currently implemented as a copy operation.
     * 
     * @param classInModule class whose module should be moved
     * @param targetLayer target module layer
     * @param targetClassLoader target class loader
     */
    public static void move(
        Class<?> classInModule,
        ModuleLayer targetLayer,
        ModuleClassLoader targetClassLoader
    ) {
        move(
            classInModule.getModule().getName(),
            classInModule.getModule().getLayer(),
            (ModuleClassLoader) classInModule.getClassLoader(),
            targetLayer,
            targetClassLoader
        );
    }

    /**
     * Moves the module containing a class to target layer.
     * Currently implemented as a copy operation.
     * 
     * @param classInModule class whose module should be moved
     * @param targetLayer target layer
     */
    public static void move(Class<?> classInModule, IModuleLayerManager.Layer targetLayer) {
        move(classInModule, LauncherAccessor.getModuleLayer(targetLayer), ModuleLayerHandlerAccessor.getModuleClassLoader(targetLayer));
    }
}
