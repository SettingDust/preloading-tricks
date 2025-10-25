package settingdust.preloading_tricks.forgelike.module_injector;

import cpw.mods.cl.JarModuleFinder;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.ModuleLayerHandler;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.*;

import java.lang.module.Configuration;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ModuleClassLoaderInjector {
    public static void inject(Path path, IModuleLayerManager.Layer layer) {
        if (!path.getFileName().toString().endsWith(".jar"))
            throw new IllegalArgumentException("Path must be a jar: " + path);
        var completedLayers = ModuleLayerHandlerAccessor
            .getCompletedLayers((ModuleLayerHandler) Launcher.INSTANCE.findLayerManager().orElseThrow());
        var layerInfo = completedLayers.get(layer);
        var classLoader = LayerInfoAccessor.getModuleClassLoader(layerInfo);

        var jar = SecureJar.from(path);
        var moduleFinder = JarModuleFinder.of(jar);
        var moduleReference = moduleFinder.findAll().iterator().next();
        var moduleName = moduleReference.descriptor().name();
        var moduleConfiguration = ModuleClassLoaderAccessor.getConfiguration(classLoader);
        var jarConfiguration = moduleConfiguration.resolve(moduleFinder, JarModuleFinder.of(), Set.of(moduleName));

        mergeConfigurations(moduleConfiguration, jarConfiguration);

        var resolvedModule = jarConfiguration.findModule(moduleName).orElseThrow();
        var moduleLayer = LauncherAccessor.getModuleLayer(layer);
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

    private static void mergeConfigurations(final Configuration to, final Configuration from) {
        ConfigurationAccessor.getGraph(to).putAll(ConfigurationAccessor.getGraph(from));

        var modules = new HashSet<>(ConfigurationAccessor.getModules(to));
        modules.addAll(ConfigurationAccessor.getModules(from));
        ConfigurationAccessor.setModules(to, modules);

        var nameToModule = new HashMap<>(ConfigurationAccessor.getNameToModule(to));
        nameToModule.putAll(ConfigurationAccessor.getNameToModule(from));
        ConfigurationAccessor.setNameToModule(to, nameToModule);
    }
}
