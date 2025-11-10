package settingdust.preloading_tricks.lexforge;

import cpw.mods.niofs.union.UnionPath;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;
import settingdust.preloading_tricks.api.PreloadingTricksModManager;
import settingdust.preloading_tricks.forgelike.specified_forge_variant.ForgeVariants;

import java.net.URISyntaxException;
import java.nio.file.Path;

public class LexForgePreloadingTricksCallback implements PreloadingTricksCallback {
    @Override
    public void onSetupMods() {
        try {
            FMLLoader.class.getSimpleName();
        } catch (Throwable e) {
            return;
        }

        var manager = PreloadingTricksModManager.<PreloadingTricksModManager<ModFile>>get();

        try {
            var mod = manager.createVirtualMod(
                PreloadingTricks.MOD_ID,
                ((UnionPath) Path.of(this.getClass()
                                         .getProtectionDomain()
                                         .getCodeSource()
                                         .getLocation()
                                         .toURI())).getFileSystem().getPrimaryPath()
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
            var shouldRemove = variant != null && variant != ForgeVariants.LexForge;
            if (shouldRemove)
                PreloadingTricks.LOGGER.debug("Removing {} for variant {}", it.getFilePath(), variant);
            return shouldRemove;
        });
    }
}
