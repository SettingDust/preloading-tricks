package settingdust.preloading_tricks.neoforge.modlauncher;

import cpw.mods.jarhandling.SecureJar;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;
import settingdust.preloading_tricks.api.PreloadingTricksModManager;
import settingdust.preloading_tricks.forgelike.specified_forge_variant.ForgeVariants;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Set;

public class NeoForgePreloadingTricksCallback implements PreloadingTricksCallback {
    @Override
    public void onSetupMods() {
        try {
            ModFile.class.getSimpleName();
            SecureJar.class.getSimpleName();
        } catch (Throwable e) {
            return;
        }

        var manager = PreloadingTricksModManager.<PreloadingTricksModManager<ModFile>>get();

        try {
            var mod = manager.createVirtualMod(
                PreloadingTricks.MOD_ID,
                Path.of(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI())
            );
            manager.add(mod);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        manager.removeIf(it -> {
            var manifest = it.getSecureJar().moduleDataProvider().getManifest();
            var variantString = manifest.getMainAttributes().getValue(ForgeVariants.MANIFEST_KEY);
            if (variantString == null) return false;
            var variant = ForgeVariants.BY_NAME.get(variantString.toLowerCase());
            var shouldRemove = variant != null && variant != ForgeVariants.NeoForge;
            if (shouldRemove)
                PreloadingTricks.LOGGER.debug("Removing {} for variant {}", it.getFilePath(), variant);
            return shouldRemove;
        });

        var packagesToRemove = Set.of("net.lenni0451.reflect");

        manager.removeIf(mod -> {
            var jar = mod.getSecureJar();
            var packages = jar.moduleDataProvider().descriptor().packages();
            var needRemove = packagesToRemove.stream().anyMatch(packages::contains);
            if (needRemove)
                PreloadingTricks.LOGGER.debug("Avoid {} from loading for packages {}", mod.getFilePath(), packagesToRemove);
            return needRemove;
        });
    }
}
