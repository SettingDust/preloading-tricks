package settingdust.preloading_tricks.lexforge.virtual_mod;

import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.IConfigurable;
import settingdust.preloading_tricks.PreloadingTricks;

import java.util.List;
import java.util.Optional;

public class VirtualModFileInfo extends ModFileInfo implements IConfigurable {
    private final String modId;

    private static IConfigurable createConfig(String modId) {
        final IConfigurable modConfig = new IConfigurable() {
            @Override
            public <T> Optional<T> getConfigElement(String... key) {
                if (key.length == 1 && key[0].equals("modId")) {
                    return Optional.of((T) modId);
                }
                return Optional.empty();
            }

            @Override
            public List<? extends IConfigurable> getConfigList(String... key) {
                return List.of();
            }
        };

        return new IConfigurable() {
            @Override
            public <T> Optional<T> getConfigElement(String... key) {
                if (key.length == 1) {
                    return switch (key[0]) {
                        case "modLoader" -> Optional.of((T) "virtual");
                        case "loaderVersion" -> Optional.of((T) "[0,)");
                        case "license" -> Optional.of((T) "Virtual");
                        default -> Optional.empty();
                    };
                }
                return Optional.empty();
            }

            @Override
            public List<? extends IConfigurable> getConfigList(String... key) {
                if (key.length == 1 && key[0].equals("mods")) {
                    return List.of(modConfig);
                }
                return List.of();
            }
        };
    }

    public VirtualModFileInfo(final String modId, final ModFile modFile) {
        super(modFile, createConfig(modId), fileInfo -> {}, List.of());
        this.modId = modId;
    }

    @Override
    public String getLicense() {
        return "Virtual mod by " + PreloadingTricks.NAME;
    }

    @Override
    public String moduleName() {
        return modId;
    }

    @Override
    public String versionString() {
        return "0.0.0";
    }

    @Override
    public String toString() {
        return "VirtualModFileInfo{" + modId + "}";
    }
}
