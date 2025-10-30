package settingdust.preloading_tricks.modlauncher.module_injector;

import cpw.mods.cl.JarModuleFinder;
import cpw.mods.jarhandling.SecureJar;

import java.lang.module.Configuration;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Factory class for creating module configurations from JAR files.
 * 
 * <p>Provides various methods to generate {@link Configuration} objects from:
 * <ul>
 *   <li>Single or multiple {@link SecureJar} instances</li>
 *   <li>Single or multiple JAR file paths</li>
 *   <li>Lists of JARs or paths</li>
 * </ul>
 * 
 * <p>All generated configurations use {@link JarModuleFinder} for module resolution.
 */
public class ModuleConfigurationCreator {
    
    /**
     * Creates a module configuration from a single JAR.
     * 
     * @param jar SecureJar containing the module
     * @param parentConfig parent configuration for resolution
     * @return new Configuration with the module
     */
    public static Configuration createConfiguration(SecureJar jar, Configuration parentConfig) {
        var moduleName = jar.moduleDataProvider().name();
        return parentConfig.resolve(
            JarModuleFinder.of(jar),
            JarModuleFinder.of(),
            Set.of(moduleName)
        );
    }

    /**
     * Creates a module configuration from a JAR file path.
     * 
     * @param jarPath path to JAR file (must end with .jar)
     * @param parentConfig parent configuration for resolution
     * @return new Configuration with the module
     * @throws IllegalArgumentException if path doesn't end with .jar
     */
    public static Configuration createConfiguration(Path jarPath, Configuration parentConfig) {
        validateJarPath(jarPath);
        return createConfiguration(SecureJar.from(jarPath), parentConfig);
    }

    /**
     * Creates a single configuration from multiple JARs.
     * All modules are resolved together.
     * 
     * @param jars list of SecureJars
     * @param parentConfig parent configuration for resolution
     * @return new Configuration with all modules, or parentConfig if empty
     */
    public static Configuration createConfiguration(List<SecureJar> jars, Configuration parentConfig) {
        if (jars.isEmpty()) {
            return parentConfig;
        }
        var moduleNames = jars.stream()
            .map(jar -> jar.moduleDataProvider().name())
            .collect(Collectors.toSet());
        return parentConfig.resolve(
            JarModuleFinder.of(jars.toArray(new SecureJar[0])),
            JarModuleFinder.of(),
            moduleNames
        );
    }

    /**
     * Creates a single configuration from multiple JAR paths.
     * All modules are resolved together.
     * 
     * @param jarPaths list of JAR file paths
     * @param parentConfig parent configuration for resolution
     * @return new Configuration with all modules
     * @throws IllegalArgumentException if any path doesn't end with .jar
     */
    public static Configuration createConfigurationFromPaths(List<Path> jarPaths, Configuration parentConfig) {
        var jars = jarPaths.stream()
            .peek(ModuleConfigurationCreator::validateJarPath)
            .map(SecureJar::from)
            .toList();
        return createConfiguration(jars, parentConfig);
    }

    /**
     * Creates a single configuration from multiple JARs (varargs).
     * All modules are resolved together.
     * 
     * @param parentConfig parent configuration for resolution
     * @param jars SecureJars (varargs)
     * @return new Configuration with all modules
     */
    public static Configuration createConfiguration(Configuration parentConfig, SecureJar... jars) {
        return createConfiguration(Arrays.asList(jars), parentConfig);
    }
    
    /**
     * Validates that a path points to a JAR file.
     * 
     * @param jarPath path to validate
     * @throws IllegalArgumentException if path doesn't end with .jar
     */
    private static void validateJarPath(Path jarPath) {
        if (!jarPath.getFileName().toString().endsWith(".jar"))
            throw new IllegalArgumentException("Path must be a jar: " + jarPath);
    }
}
