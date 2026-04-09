package settingdust.preloading_tricks.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.resources.ResourceLocation;

public interface MinecraftAdapter {
    Supplier<MinecraftAdapter> INSTANCE = Suppliers.memoize(() -> ServiceLoaderUtil.findService(MinecraftAdapter.class));

    static MinecraftAdapter getInstance() {
        return INSTANCE.get();
    }

    ResourceLocation id(String namespace, String path);
}
