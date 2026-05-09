package settingdust.preloading_tricks.v20_1.util;

import net.minecraft.resources.ResourceLocation;
import settingdust.preloading_tricks.util.CommonIdentifier;
import settingdust.preloading_tricks.util.MinecraftVersion;

public class IdentifierFactory implements settingdust.preloading_tricks.util.IdentifierFactory {
    public IdentifierFactory() {
        MinecraftVersion.V1201.requireCurrent();
    }

    @Override
    public CommonIdentifier create(String namespace, String path) {
        return (CommonIdentifier) (Object) new ResourceLocation(namespace, path);
    }

    @Override
    public CommonIdentifier parse(String value) {
        return (CommonIdentifier) (Object) new ResourceLocation(value);
    }
}
