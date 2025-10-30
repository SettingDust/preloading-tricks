package settingdust.preloading_tricks.modlauncher.module_injector;

import cpw.mods.cl.ModuleClassLoader;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ConfigurationAccessor;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ModuleAccessor;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ModuleLayerAccessor;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ResolvedModuleAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.ModuleClassLoaderAccessor;

import java.lang.module.Configuration;
import java.lang.module.ResolvedModule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Helper class providing low-level operations for module manipulation.
 * 
 * <p>This class contains utility methods for:
 * <ul>
 *   <li>Creating and registering module instances</li>
 *   <li>Setting up read relationships between modules</li>
 *   <li>Updating class loader package lookups and resolved roots</li>
 *   <li>Merging and modifying module configurations</li>
 * </ul>
 * 
 * <p><b>Note:</b> This class is intended for internal use by other module manipulation classes.
 */
public class ModuleOperationHelper {
    
    /**
     * Creates and registers a module instance in the target layer.
     * 
     * @param resolvedModule module to create
     * @param targetLayer layer to register in
     * @param targetClassLoader class loader for the module
     * @param targetConfig configuration for the module
     * @return the created Module instance
     */
    public static Module createAndRegisterModule(
        ResolvedModule resolvedModule,
        ModuleLayer targetLayer,
        ModuleClassLoader targetClassLoader,
        Configuration targetConfig
    ) {
        var module = ModuleAccessor.construct(
            targetLayer,
            targetClassLoader,
            resolvedModule.reference().descriptor(),
            resolvedModule.reference().location().orElse(null)
        );
        ModuleLayerAccessor.getNameToModule(targetLayer).put(resolvedModule.name(), module);
        ResolvedModuleAccessor.setCf(resolvedModule, targetConfig);
        return module;
    }

    /**
     * Sets up mutual read relationships between modules.
     * All modules will be able to read each other and the base module.
     * 
     * @param modulesToLink modules to establish relationships between
     * @param baseModule base module that should read all others
     */
    public static void setupMutualReads(Iterable<Module> modulesToLink, Module baseModule) {
        var moduleList = new ArrayList<Module>();
        modulesToLink.forEach(moduleList::add);
        
        for (var module : moduleList) {
            baseModule.addReads(module);
            for (var other : moduleList) {
                if (module != other) {
                    module.addReads(other);
                }
            }
        }
    }

    /**
     * Updates the class loader's package lookup map with new modules.
     * 
     * @param classLoader class loader to update
     * @param newModules modules whose packages should be registered
     */
    public static void updatePackageLookup(
        ModuleClassLoader classLoader,
        Iterable<ResolvedModule> newModules
    ) {
        var packageLookup = new HashMap<>(ModuleClassLoaderAccessor.getPackageLookup(classLoader));
        for (var module : newModules) {
            for (var packageName : module.reference().descriptor().packages()) {
                packageLookup.put(packageName, module);
            }
        }
        ModuleClassLoaderAccessor.setPackageLookup(classLoader, packageLookup);
    }

    /**
     * Updates the class loader's resolved roots map with new modules.
     * 
     * @param classLoader class loader to update
     * @param newModules modules to add to resolved roots
     */
    public static void updateResolvedRoots(
        ModuleClassLoader classLoader,
        Iterable<ResolvedModule> newModules
    ) {
        var resolvedRoots = new HashMap<>(ModuleClassLoaderAccessor.getResolvedRoots(classLoader));
        for (var module : newModules) {
            resolvedRoots.put(module.name(), module.reference());
        }
        ModuleClassLoaderAccessor.setResolvedRoots(classLoader, resolvedRoots);
    }

    /**
     * Updates the package lookup for specific packages in a module.
     * Used when replacing modules to register new packages.
     * 
     * @param classLoader class loader to update
     * @param resolvedModule resolved module containing the packages
     * @param packages packages to register
     */
    public static void updatePackageLookupForModule(
        ModuleClassLoader classLoader,
        ResolvedModule resolvedModule,
        Iterable<String> packages
    ) {
        var packageLookup = new HashMap<>(ModuleClassLoaderAccessor.getPackageLookup(classLoader));
        for (var pkg : packages) {
            packageLookup.put(pkg, resolvedModule);
        }
        ModuleClassLoaderAccessor.setPackageLookup(classLoader, packageLookup);
    }

    /**
     * Merges configuration data from source to target.
     * 
     * @param targetConfig configuration to merge into
     * @param sourceConfig configuration to merge from
     */
    public static void mergeConfigurations(final Configuration targetConfig, final Configuration sourceConfig) {
        ConfigurationAccessor.getGraph(targetConfig).putAll(ConfigurationAccessor.getGraph(sourceConfig));

        var modules = new HashSet<>(ConfigurationAccessor.getModules(targetConfig));
        modules.addAll(ConfigurationAccessor.getModules(sourceConfig));
        ConfigurationAccessor.setModules(targetConfig, modules);

        var nameToModule = new HashMap<>(ConfigurationAccessor.getNameToModule(targetConfig));
        nameToModule.putAll(ConfigurationAccessor.getNameToModule(sourceConfig));
        ConfigurationAccessor.setNameToModule(targetConfig, nameToModule);
    }

    /**
     * Adds a module to a configuration.
     * 
     * @param targetConfig configuration to add to
     * @param moduleToAdd module to add
     */
    public static void addModuleToConfiguration(
        final Configuration targetConfig,
        ResolvedModule moduleToAdd
    ) {
        ConfigurationAccessor.getGraph(targetConfig).put(moduleToAdd, new HashSet<>());

        var modules = new HashSet<>(ConfigurationAccessor.getModules(targetConfig));
        modules.add(moduleToAdd);
        ConfigurationAccessor.setModules(targetConfig, modules);

        var nameToModule = new HashMap<>(ConfigurationAccessor.getNameToModule(targetConfig));
        nameToModule.put(moduleToAdd.name(), moduleToAdd);
        ConfigurationAccessor.setNameToModule(targetConfig, nameToModule);
    }

    /**
     * Removes a module from a configuration.
     * 
     * @param config configuration to remove from
     * @param moduleToRemove module to remove
     */
    public static void removeModuleFromConfiguration(final Configuration config, ResolvedModule moduleToRemove) {
        ConfigurationAccessor.getGraph(config).remove(moduleToRemove);

        var modules = new HashSet<>(ConfigurationAccessor.getModules(config));
        modules.remove(moduleToRemove);
        ConfigurationAccessor.setModules(config, modules);

        var nameToModule = new HashMap<>(ConfigurationAccessor.getNameToModule(config));
        nameToModule.remove(moduleToRemove.name());
        ConfigurationAccessor.setNameToModule(config, nameToModule);
    }
}
