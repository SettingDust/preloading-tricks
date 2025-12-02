package settingdust.preloading_tricks.lexforge;

import cpw.mods.niofs.union.UnionPath;
import settingdust.preloading_tricks.PreloadingTricks;
import settingdust.preloading_tricks.api.PreloadingTricksCallbacks;
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;
import settingdust.preloading_tricks.forgelike.specified_forge_variant.ForgeVariants;
import settingdust.preloading_tricks.modlauncher.PreloadingTricksTransformationService;
import settingdust.preloading_tricks.modlauncher.TransformationServiceCallback;
import settingdust.preloading_tricks.util.LoaderPredicates;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Set;

public class LexForgeTransformationServiceCallback implements TransformationServiceCallback {
    @Override
    public void init() {
        if (!LoaderPredicates.Forge.test()) {
            return;
        }

        PreloadingTricksTransformationService.init();

        ClassTransformBootstrap.INSTANCE.addConfig(
            PreloadingTricks.MOD_ID + ".lexforge.classtransform.json",
            PreloadingTricksTransformationService.class.getClassLoader()
        );

        PreloadingTricksCallbacks.SETUP_MODS.register(_manager -> {
            if (!(_manager instanceof LexForgeModManager manager)) return;

            try {
                var mod = manager.createVirtualMod(
                    PreloadingTricks.MOD_ID,
                    ((UnionPath) Path.of(
                        LexForgeTransformationServiceCallback.class
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
                    PreloadingTricks.LOGGER.debug("Avoid {} from loading for variant {}", it.getFilePath(), variant);
                return shouldRemove;
            });

            var packagesToRemove = Set.of("net.lenni0451.reflect");

            manager.removeIf(mod -> {
                var jar = mod.getSecureJar();
                var needRemove = packagesToRemove.stream().anyMatch(it -> jar.getPackages().contains(it));
                if (needRemove)
                    PreloadingTricks.LOGGER.debug(
                        "Avoid {} from loading for packages {}",
                        mod.getFilePath(),
                        packagesToRemove
                    );
                return needRemove;
            });
        });
    }
}
