package settingdust.preloading_tricks.api;

/**
 * Preloading entry point interface for executing custom logic at the earliest stage of mod loader initialization.
 * 
 * <p>This interface allows mods to register callbacks and perform initialization tasks before the normal mod loading process.
 * Implementation classes will be automatically discovered and constructed through the Java SPI (Service Provider Interface) mechanism.</p>
 * 
 * <h2>Usage</h2>
 * <p>To use this interface, mods need to:</p>
 * <ol>
 *   <li>Create a class that implements this interface</li>
 *   <li>Create a file named {@code settingdust.preloading_tricks.api.PreloadingEntrypoint} in the mod's
 *       {@code META-INF/services} directory</li>
 *   <li>Add the fully qualified name of the implementation class to that file</li>
 * </ol>
 * 
 * <h2>Platform Specific Notes</h2>
 * <h3>Forge and NeoForge</h3>
 * <p>To ensure your JAR is loaded at the service layer so that Preloading Tricks can find your implementation through SPI,
 * you need to add an empty service file for either {@code cpw.mods.modlauncher.api.ITransformationService} or 
 * {@code net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper}.</p>
 * 
 * <p>Note: There are other implementations in Forge and NeoForge that will also be loaded at the service layer,
 * but they are not listed in detail here.</p>
 * 
 * @see java.util.ServiceLoader Java SPI mechanism
 */
public interface PreloadingEntrypoint {
}
