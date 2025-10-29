package settingdust.preloading_tricks.neoforge.v121x.virtual_mod;

import net.neoforged.neoforgespi.language.IConfigurable;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VirtualModFileInfo implements IModFileInfo, IConfigurable {
    private final IModFile modFile;

    public VirtualModFileInfo(final IModFile modFile) {this.modFile = modFile;}

    @Override
    public List<IModInfo> getMods() {
        return List.of();
    }

    @Override
    public List<LanguageSpec> requiredLanguageLoaders() {
        return List.of();
    }

    @Override
    public boolean showAsResourcePack() {
        return false;
    }

    @Override
    public boolean showAsDataPack() {
        return false;
    }

    @Override
    public Map<String, Object> getFileProperties() {
        return Map.of();
    }

    @Override
    public String getLicense() {
        return "VIRTUAL";
    }

    @Override
    public String versionString() {
        return null;
    }

    @Override
    public List<String> usesServices() {
        return null;
    }

    @Override
    public IModFile getFile() {
        return null;
    }

    @Override
    public IConfigurable getConfig() {
        return this;
    }

    @Override
    public String toString() {
        return "VirtualModFileInfo{"
               + modFile.getFilePath()
               + '}';
    }

    @Override
    public <T> Optional<T> getConfigElement(final String... key) {
        return Optional.empty();
    }

    @Override
    public List<? extends IConfigurable> getConfigList(final String... key) {
        return List.of();
    }
}
