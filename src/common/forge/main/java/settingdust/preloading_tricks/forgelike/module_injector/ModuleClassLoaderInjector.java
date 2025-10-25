package settingdust.preloading_tricks.forgelike.module_injector;

import cpw.mods.cl.JarModuleFinder;
import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.*;

import java.lang.module.Configuration;
import java.lang.module.ResolvedModule;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ModuleClassLoaderInjector {
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
            ModuleAccessor.implAddReads(module, moduleLayer.findModule(readModule.name()).orElseThrow());
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

    public static void inject(Path path, ModuleClassLoader classLoader, ModuleLayer moduleLayer) {
        if (!path.getFileName().toString().endsWith(".jar"))
            throw new IllegalArgumentException("Path must be a jar: " + path);

        var jar = SecureJar.from(path);
        inject(jar, classLoader, moduleLayer);
    }

    public static void inject(Path path, IModuleLayerManager.Layer layer) {
        inject(
            path,
            ModuleLayerHandlerAccessor.getModuleClassLoader(layer),
            LauncherAccessor.getModuleLayer(layer)
        );
    }

    public static void inject(Path path, Class<?> clazzInTarget) {
        inject(
            path,
            (ModuleClassLoader) clazzInTarget.getClassLoader(),
            clazzInTarget.getModule().getLayer()
        );
    }

    private static void mergeConfigurations(final Configuration to, final Configuration from) {
        ConfigurationAccessor.getGraph(to).putAll(ConfigurationAccessor.getGraph(from));

        var modules = new HashSet<>(ConfigurationAccessor.getModules(to));
        modules.addAll(ConfigurationAccessor.getModules(from));
        ConfigurationAccessor.setModules(to, modules);

        var nameToModule = new HashMap<>(ConfigurationAccessor.getNameToModule(to));
        nameToModule.putAll(ConfigurationAccessor.getNameToModule(from));
        ConfigurationAccessor.setNameToModule(to, nameToModule);
    }

    public static void move(
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

        var fromPackageLookup = new HashMap<>(ModuleClassLoaderAccessor.getPackageLookup(fromModuleClassLoader));
        var fromResolvedRoots = new HashMap<>(ModuleClassLoaderAccessor.getResolvedRoots(fromModuleClassLoader));
        var fromConfiguration = ModuleClassLoaderAccessor.getConfiguration(fromModuleClassLoader);

        var toPackageLookup = new HashMap<>(ModuleClassLoaderAccessor.getPackageLookup(toModuleClassLoader));
        var toResolvedRoots = new HashMap<>(ModuleClassLoaderAccessor.getResolvedRoots(toModuleClassLoader));
        var toConfiguration = ModuleClassLoaderAccessor.getConfiguration(toModuleClassLoader);

        for (final var packageName : resolvedModule.reference().descriptor().packages()) {
            fromPackageLookup.remove(packageName);
            toPackageLookup.put(packageName, resolvedModule);
        }

        fromResolvedRoots.remove(resolvedModule.name());
        toResolvedRoots.put(resolvedModule.name(), resolvedModule.reference());

        moveConfiguration(toConfiguration, fromConfiguration, resolvedModule);

        ModuleClassLoaderAccessor.setPackageLookup(fromModuleClassLoader, fromPackageLookup);
        ModuleClassLoaderAccessor.setResolvedRoots(fromModuleClassLoader, fromResolvedRoots);
        ModuleClassLoaderAccessor.setPackageLookup(toModuleClassLoader, toPackageLookup);
        ModuleClassLoaderAccessor.setResolvedRoots(toModuleClassLoader, toResolvedRoots);
    }

    public static void move(String moduleName, IModuleLayerManager.Layer from, IModuleLayerManager.Layer to) {
        move(
            moduleName,
            LauncherAccessor.getModuleLayer(from),
            ModuleLayerHandlerAccessor.getModuleClassLoader(from),
            LauncherAccessor.getModuleLayer(to),
            ModuleLayerHandlerAccessor.getModuleClassLoader(to)
        );
    }

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

    public static void move(Class<?> classInModule, IModuleLayerManager.Layer to) {
        move(classInModule, LauncherAccessor.getModuleLayer(to), ModuleLayerHandlerAccessor.getModuleClassLoader(to));
    }

    private static void moveConfiguration(final Configuration to, final Configuration from, ResolvedModule module) {
        ConfigurationAccessor.getGraph(to).put(module, ConfigurationAccessor.getGraph(from).get(module));

        var modules = new HashSet<>(ConfigurationAccessor.getModules(to));
        modules.add(module);
        ConfigurationAccessor.setModules(to, modules);

        var nameToModule = new HashMap<>(ConfigurationAccessor.getNameToModule(to));
        nameToModule.put(module.name(), module);
        ConfigurationAccessor.setNameToModule(to, nameToModule);


        ConfigurationAccessor.getGraph(from).remove(module);

        var fromModules = new HashSet<>(ConfigurationAccessor.getModules(to));
        fromModules.remove(module);
        ConfigurationAccessor.setModules(from, fromModules);

        var fromNameToModule = new HashMap<>(ConfigurationAccessor.getNameToModule(to));
        fromNameToModule.remove(module.name());
        ConfigurationAccessor.setNameToModule(from, fromNameToModule);
    }
}
