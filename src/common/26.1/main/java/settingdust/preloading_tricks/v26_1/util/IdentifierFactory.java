package settingdust.preloading_tricks.v26_1.util;

import net.minecraft.resources.Identifier;
import settingdust.preloading_tricks.util.CommonIdentifier;
import settingdust.preloading_tricks.util.MinecraftVersion;

public class IdentifierFactory implements settingdust.preloading_tricks.util.IdentifierFactory {
    public IdentifierFactory() {
        MinecraftVersion.V261.requireCurrent();
    }

    @Override
    public CommonIdentifier create(String namespace, String path) {
        return (CommonIdentifier) (Object) Identifier.fromNamespaceAndPath(namespace, path);
    }

    @Override
    public CommonIdentifier parse(String value) {
        return (CommonIdentifier) (Object) Identifier.parse(value);
    }
}
