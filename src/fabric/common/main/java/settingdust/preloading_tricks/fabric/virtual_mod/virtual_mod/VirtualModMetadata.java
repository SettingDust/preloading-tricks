package settingdust.preloading_tricks.fabric.virtual_mod.virtual_mod;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.*;
import net.fabricmc.loader.impl.metadata.EntrypointMetadata;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import net.fabricmc.loader.impl.metadata.NestedJarEntry;
import net.fabricmc.loader.impl.util.version.StringVersion;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VirtualModMetadata implements ModMetadata, LoaderModMetadata {
    private static final Version version = new StringVersion("virtual");
    private final String id;

    public VirtualModMetadata(final String id) {this.id = id;}

    @Override
    public int getSchemaVersion() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Map<String, String> getLanguageAdapterDefinitions() {
        return Map.of();
    }

    @Override
    public Collection<NestedJarEntry> getJars() {
        return List.of();
    }

    @Override
    public Collection<String> getMixinConfigs(final EnvType envType) {
        return List.of();
    }

    @Override
    public String getAccessWidener() {
        return null;
    }

    @Override
    public boolean loadsInEnvironment(final EnvType envType) {
        return true;
    }

    @Override
    public Collection<String> getOldInitializers() {
        return List.of();
    }

    @Override
    public List<EntrypointMetadata> getEntrypoints(final String s) {
        return List.of();
    }

    @Override
    public Collection<String> getEntrypointKeys() {
        return List.of();
    }

    @Override
    public void emitFormatWarnings() {

    }

    @Override
    public void setVersion(final Version version) {

    }

    @Override
    public void setDependencies(final Collection<ModDependency> collection) {

    }

    @Override
    public String getType() {
        return "preloading_tricks:virtual";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Collection<String> getProvides() {
        return List.of();
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public ModEnvironment getEnvironment() {
        return ModEnvironment.UNIVERSAL;
    }

    @Override
    public Collection<ModDependency> getDependencies() {
        return List.of();
    }

    @Override
    public String getName() {
        return id;
    }

    @Override
    public String getDescription() {
        return "Virtual mod with id " + id;
    }

    @Override
    public Collection<Person> getAuthors() {
        return List.of();
    }

    @Override
    public Collection<Person> getContributors() {
        return List.of();
    }

    @Override
    public ContactInformation getContact() {
        return ContactInformation.EMPTY;
    }

    @Override
    public Collection<String> getLicense() {
        return List.of();
    }

    @Override
    public Optional<String> getIconPath(final int i) {
        return Optional.empty();
    }

    @Override
    public boolean containsCustomValue(final String s) {
        return false;
    }

    @Override
    public CustomValue getCustomValue(final String s) {
        return null;
    }

    @Override
    public Map<String, CustomValue> getCustomValues() {
        return Map.of();
    }

    @Override
    public boolean containsCustomElement(final String s) {
        return false;
    }
}
