package settingdust.preloading_tricks.neoforge.v121x.specified_forge_variant;

import net.neoforged.fml.loading.moddiscovery.ModFile;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallback;
import settingdust.preloading_tricks.api.PreloadingTricksModManager;
import settingdust.preloading_tricks.forgelike.specified_forge_variant.ForgeVariants;

public class ForgeVariantHandler implements PreloadingTricksCallback {
    @Override
    public void onSetupMods() {
        var manager = PreloadingTricksModManager.<PreloadingTricksModManager<ModFile>>get();
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
