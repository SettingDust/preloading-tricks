package settingdust.preloading_tricks.neoforge.modlauncher.legacy;

import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallbacks;
import settingdust.preloading_tricks.forgelike.specified_forge_variant.ForgeVariants;
import settingdust.preloading_tricks.modlauncher.PreloadingTricksTransformationService;
import settingdust.preloading_tricks.util.LoaderPredicates;
import settingdust.preloading_tricks.util.class_transform.ClassTransformBootstrap;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Set;

public class TransformationServiceCallback implements
                                           settingdust.preloading_tricks.modlauncher.TransformationServiceCallback {
    @Override
    public void init() {
        if (!LoaderPredicates.NeoForgeModLauncher.test()) {
            return;
        }

        PreloadingTricksTransformationService.init();

        ClassTransformBootstrap.INSTANCE.addConfig(
            PreloadingTricks.MOD_ID + ".neoforge.modlauncher.classtransform.json",
            PreloadingTricksTransformationService.class.getClassLoader()
        );

        PreloadingTricksCallbacks.SETUP_MODS.register(_manager -> {
            if (!(_manager instanceof NeoForgeModManager manager)) return;

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
        });
    }
}
