package settingdust.preloading_tricks.modlauncher.module_injector;

import cpw.mods.cl.JarModuleFinder;
import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ConfigurationAccessor;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ModuleAccessor;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ModuleLayerAccessor;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ResolvedModuleAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.LauncherAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.ModuleClassLoaderAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.ModuleLayerHandlerAccessor;

import java.lang.module.Configuration;
import java.lang.module.ResolvedModule;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ModuleClassLoaderInjector {
    // ==================== Inject Methods ====================

    /**
     * Inject a module from SecureJar into the specified ClassLoader and ModuleLayer
     */
    public static void inject(SecureJar jar, ModuleClassLoader classLoader, ModuleLayer moduleLayer) {
        var moduleName = jar.moduleDataProvider().name();
        var moduleConfiguration = ModuleClassLoaderAccessor.getConfiguration(classLoader);
        var jarConfiguration = moduleConfiguration.resolve(
            JarModuleFinder.of(jar),
            JarModuleFinder.of(),
            Set.of(moduleName)
        );

        mergeConfigurations(moduleConfiguration, jarConfiguration);

        var resolvedModule = jarConfiguration.findModule(moduleName).orElseThrow();
        var module = ModuleAccessor.construct(
            moduleLayer,
            classLoader,
            resolvedModule.reference().descriptor(),
            resolvedModule.reference().location().orElse(null)
        );

        ModuleLayerAccessor.getNameToModule(moduleLayer).put(resolvedModule.name(), module);

        ModuleClassLoaderInjector.class.getModule().addReads(module);

        for (final var readModule : ConfigurationAccessor.getGraph(moduleConfiguration).get(resolvedModule)) {
            var readModuleInTarget = moduleLayer.findModule(readModule.name());
            if (readModuleInTarget.isEmpty())
                continue;
            ModuleAccessor.implAddReads(module, readModuleInTarget.get());
        }

        ResolvedModuleAccessor.setCf(resolvedModule, moduleConfiguration);

        var packageLookup = new HashMap<>(ModuleClassLoaderAccessor.getPackageLookup(classLoader));
        for (String name : resolvedModule.reference().descriptor().packages()) {
            packageLookup.put(name, resolvedModule);
        }
        ModuleClassLoaderAccessor.setPackageLookup(classLoader, packageLookup);

        var resolvedRoots = new HashMap<>(ModuleClassLoaderAccessor.getResolvedRoots(classLoader));
        resolvedRoots.put(resolvedModule.name(), resolvedModule.reference());
        ModuleClassLoaderAccessor.setResolvedRoots(classLoader, resolvedRoots);
    }

    /**
     * Inject a module from JAR file path into the specified ClassLoader and ModuleLayer
     */
    public static void inject(Path path, ModuleClassLoader classLoader, ModuleLayer moduleLayer) {
        if (!path.getFileName().toString().endsWith(".jar"))
            throw new IllegalArgumentException("Path must be a jar: " + path);

        var jar = SecureJar.from(path);
        inject(jar, classLoader, moduleLayer);
    }

    /**
     * Inject a module from JAR file path into the specified Layer
     */
    public static void inject(Path path, IModuleLayerManager.Layer layer) {
        inject(
            path,
            ModuleLayerHandlerAccessor.getModuleClassLoader(layer),
            LauncherAccessor.getModuleLayer(layer)
        );
    }

    /**
     * Inject a module from JAR file path into the same layer as the target class
     */
    public static void inject(Path path, Class<?> clazzInTarget) {
        inject(
            path,
            (ModuleClassLoader) clazzInTarget.getClassLoader(),
            clazzInTarget.getModule().getLayer()
        );
    }

    // ==================== Copy Methods ====================

    /**
     * Copy a module from source ClassLoader/ModuleLayer to target ClassLoader/ModuleLayer
     */
    public static void copy(
        String moduleName,
        ModuleLayer fromModuleLayer,
        ModuleClassLoader fromModuleClassLoader,
        ModuleLayer toModuleLayer,
        ModuleClassLoader toModuleClassLoader
    ) {
        var resolvedModule =
            fromModuleLayer.configuration().findModule(moduleName)
                           .orElseThrow(() -> new RuntimeException("Module %s not found".formatted(moduleName)));
        var module =
            fromModuleLayer.findModule(moduleName)
                           .orElseThrow(() -> new RuntimeException("Module %s not found".formatted(moduleName)));

        ModuleAccessor.setLayer(module, toModuleLayer);

        var fromConfiguration = ModuleClassLoaderAccessor.getConfiguration(fromModuleClassLoader);
        var toPackageLookup = new HashMap<>(ModuleClassLoaderAccessor.getPackageLookup(toModuleClassLoader));
        var toResolvedRoots = new HashMap<>(ModuleClassLoaderAccessor.getResolvedRoots(toModuleClassLoader));
        var toConfiguration = ModuleClassLoaderAccessor.getConfiguration(toModuleClassLoader);

        for (final var packageName : resolvedModule.reference().descriptor().packages()) {
            toPackageLookup.put(packageName, resolvedModule);
        }

        toResolvedRoots.put(resolvedModule.name(), resolvedModule.reference());

        addModuleToConfiguration(toConfiguration, fromConfiguration, resolvedModule);

        ModuleClassLoaderAccessor.setPackageLookup(toModuleClassLoader, toPackageLookup);
        ModuleClassLoaderAccessor.setResolvedRoots(toModuleClassLoader, toResolvedRoots);
    }

    /**
     * Copy a module between Layers
     */
    public static void copy(String moduleName, IModuleLayerManager.Layer from, IModuleLayerManager.Layer to) {
        copy(
            moduleName,
            LauncherAccessor.getModuleLayer(from),
            ModuleLayerHandlerAccessor.getModuleClassLoader(from),
            LauncherAccessor.getModuleLayer(to),
            ModuleLayerHandlerAccessor.getModuleClassLoader(to)
        );
    }

    /**
     * Copy the module containing the specified class to target ClassLoader/ModuleLayer
     */
    public static void copy(
        Class<?> classInModule,
        ModuleLayer toModuleLayer,
        ModuleClassLoader toModuleClassLoader
    ) {
        copy(
            classInModule.getModule().getName(),
            classInModule.getModule().getLayer(),
            (ModuleClassLoader) classInModule.getClassLoader(),
            toModuleLayer,
            toModuleClassLoader
        );
    }

    /**
     * Copy the module containing the specified class to target Layer
     */
    public static void copy(Class<?> classInModule, IModuleLayerManager.Layer to) {
        copy(classInModule, LauncherAccessor.getModuleLayer(to), ModuleLayerHandlerAccessor.getModuleClassLoader(to));
    }

    // ==================== Remove Methods ====================

    /**
     * Remove a module from the specified ClassLoader and ModuleLayer
     */
    public static void remove(
        String moduleName,
        ModuleLayer moduleLayer,
        ModuleClassLoader moduleClassLoader
    ) {
        var resolvedModule =
            moduleLayer.configuration().findModule(moduleName)
                       .orElseThrow(() -> new RuntimeException("Module %s not found".formatted(moduleName)));

        var packageLookup = new HashMap<>(ModuleClassLoaderAccessor.getPackageLookup(moduleClassLoader));
        var resolvedRoots = new HashMap<>(ModuleClassLoaderAccessor.getResolvedRoots(moduleClassLoader));
        var configuration = ModuleClassLoaderAccessor.getConfiguration(moduleClassLoader);

        for (final var packageName : resolvedModule.reference().descriptor().packages()) {
            packageLookup.remove(packageName);
        }

        resolvedRoots.remove(resolvedModule.name());

        removeModuleFromConfiguration(configuration, resolvedModule);

        ModuleClassLoaderAccessor.setPackageLookup(moduleClassLoader, packageLookup);
        ModuleClassLoaderAccessor.setResolvedRoots(moduleClassLoader, resolvedRoots);
    }

    /**
     * Remove a module from the specified Layer
     */
    public static void remove(String moduleName, IModuleLayerManager.Layer layer) {
        remove(
            moduleName,
            LauncherAccessor.getModuleLayer(layer),
            ModuleLayerHandlerAccessor.getModuleClassLoader(layer)
        );
    }

    /**
     * Remove the module containing the specified class
     */
    public static void remove(Class<?> classInModule) {
        remove(
            classInModule.getModule().getName(),
            classInModule.getModule().getLayer(),
            (ModuleClassLoader) classInModule.getClassLoader()
        );
    }

    // ==================== Move Methods ====================

    /**
     * Move a module from source ClassLoader/ModuleLayer to target ClassLoader/ModuleLayer
     */
    public static void move(
        String moduleName,
        ModuleLayer fromModuleLayer,
        ModuleClassLoader fromModuleClassLoader,
        ModuleLayer toModuleLayer,
        ModuleClassLoader toModuleClassLoader
    ) {
        copy(moduleName, fromModuleLayer, fromModuleClassLoader, toModuleLayer, toModuleClassLoader);
    }

    /**
     * Move a module between Layers
     */
    public static void move(String moduleName, IModuleLayerManager.Layer from, IModuleLayerManager.Layer to) {
        move(
            moduleName,
            LauncherAccessor.getModuleLayer(from),
            ModuleLayerHandlerAccessor.getModuleClassLoader(from),
            LauncherAccessor.getModuleLayer(to),
            ModuleLayerHandlerAccessor.getModuleClassLoader(to)
        );
    }

    /**
     * Move the module containing the specified class to target ClassLoader/ModuleLayer
     */
    public static void move(
        Class<?> classInModule,
        ModuleLayer toModuleLayer,
        ModuleClassLoader toModuleClassLoader
    ) {
        move(
            classInModule.getModule().getName(),
            classInModule.getModule().getLayer(),
            (ModuleClassLoader) classInModule.getClassLoader(),
            toModuleLayer,
            toModuleClassLoader
        );
    }

    /**
     * Move the module containing the specified class to target Layer
     */
    public static void move(Class<?> classInModule, IModuleLayerManager.Layer to) {
        move(classInModule, LauncherAccessor.getModuleLayer(to), ModuleLayerHandlerAccessor.getModuleClassLoader(to));
    }

    // ==================== Private Helper Methods ====================

    private static void mergeConfigurations(final Configuration to, final Configuration from) {
        ConfigurationAccessor.getGraph(to).putAll(ConfigurationAccessor.getGraph(from));

        var modules = new HashSet<>(ConfigurationAccessor.getModules(to));
        modules.addAll(ConfigurationAccessor.getModules(from));
        ConfigurationAccessor.setModules(to, modules);

        var nameToModule = new HashMap<>(ConfigurationAccessor.getNameToModule(to));
        nameToModule.putAll(ConfigurationAccessor.getNameToModule(from));
        ConfigurationAccessor.setNameToModule(to, nameToModule);
    }

    private static void addModuleToConfiguration(final Configuration to, final Configuration from, ResolvedModule module) {
        ConfigurationAccessor.getGraph(to).put(module, ConfigurationAccessor.getGraph(from).get(module));

        var modules = new HashSet<>(ConfigurationAccessor.getModules(to));
        modules.add(module);
        ConfigurationAccessor.setModules(to, modules);

        var nameToModule = new HashMap<>(ConfigurationAccessor.getNameToModule(to));
        nameToModule.put(module.name(), module);
        ConfigurationAccessor.setNameToModule(to, nameToModule);
    }

    private static void removeModuleFromConfiguration(final Configuration configuration, ResolvedModule module) {
        ConfigurationAccessor.getGraph(configuration).remove(module);

        var modules = new HashSet<>(ConfigurationAccessor.getModules(configuration));
        modules.remove(module);
        ConfigurationAccessor.setModules(configuration, modules);

        var nameToModule = new HashMap<>(ConfigurationAccessor.getNameToModule(configuration));
        nameToModule.remove(module.name());
        ConfigurationAccessor.setNameToModule(configuration, nameToModule);
    }
}
