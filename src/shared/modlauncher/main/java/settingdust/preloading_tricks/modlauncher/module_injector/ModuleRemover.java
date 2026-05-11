package settingdust.preloading_tricks.modlauncher.module_injector;

import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.LauncherAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.ModuleClassLoaderAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.ModuleLayerHandlerAccessor;

import java.util.HashMap;

/**
 * Utility class for removing modules from class loaders and module layers.
 *
 * <p>Provides methods to cleanly remove modules while updating:
 * <ul>
 *   <li>Module configuration</li>
 *   <li>Package lookup mappings</li>
 *   <li>Resolved module roots</li>
 * </ul>
 */
public class ModuleRemover {

    /**
     * Removes a module from class loader and layer.
     *
     * @param moduleName name of module to remove
     * @param layer module layer
     * @param classLoader class loader
     */
    public static void remove(
        String moduleName,
        ModuleLayer layer,
        ModuleClassLoader classLoader
    ) {
        var resolvedModule =
            layer.configuration().findModule(moduleName)
                 .orElseThrow(() -> new RuntimeException("Module %s not found".formatted(moduleName)));

        var packageLookup = new HashMap<>(ModuleClassLoaderAccessor.getPackageLookup(classLoader));
        var resolvedRoots = new HashMap<>(ModuleClassLoaderAccessor.getResolvedRoots(classLoader));
        var configuration = ModuleClassLoaderAccessor.getConfiguration(classLoader);

        for (final var packageName : resolvedModule.reference().descriptor().packages()) {
            packageLookup.remove(packageName);
        }

        resolvedRoots.remove(resolvedModule.name());

        ModuleOperationHelper.removeModuleFromConfiguration(configuration, resolvedModule);

        ModuleClassLoaderAccessor.setPackageLookup(classLoader, packageLookup);
        ModuleClassLoaderAccessor.setResolvedRoots(classLoader, resolvedRoots);
    }

    /**
     * Removes a module from a layer.
     *
     * @param moduleName name of module to remove
     * @param layer layer to remove from
     */
    public static void remove(String moduleName, IModuleLayerManager.Layer layer) {
        remove(
            moduleName,
            LauncherAccessor.getModuleLayer(layer),
            ModuleLayerHandlerAccessor.getModuleClassLoader(layer)
        );
    }

    /**
     * Removes the module containing the specified class.
     *
     * @param classInModule class whose module should be removed
     */
    public static void remove(Class<?> classInModule) {
        remove(
            classInModule.getModule().getName(),
            classInModule.getModule().getLayer(),
            (ModuleClassLoader) classInModule.getClassLoader()
        );
    }
}
