package settingdust.preloading_tricks.neoforge.fancy_mod_loader;

import net.neoforged.fml.loading.moddiscovery.ModFile;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;
import settingdust.preloading_tricks.api.PreloadingTricksModManager;
import settingdust.preloading_tricks.forgelike.specified_forge_variant.ForgeVariants;
import settingdust.preloading_tricks.util.LoaderPredicates;

import java.net.URISyntaxException;
import java.nio.file.Path;

public class NeoForgePreloadingTricksCallback implements PreloadingTricksCallback {
    @Override
    public void onSetupMods() {
        LoaderPredicates.NeoForge.throwIfNot();

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
            var manifest = it.getContents().getManifest();
            var variantString = manifest.getMainAttributes().getValue(ForgeVariants.MANIFEST_KEY);
            if (variantString == null) return false;
            var variant =
                ForgeVariants.BY_NAME.get(variantString
                                                  .toLowerCase());
            var shouldRemove = variant != null && variant != ForgeVariants.NeoForge;
            if (shouldRemove)
                PreloadingTricks.LOGGER.debug("Removing {} for variant {}", it.getFilePath(), variant);
            return shouldRemove;
        });
    }
}
