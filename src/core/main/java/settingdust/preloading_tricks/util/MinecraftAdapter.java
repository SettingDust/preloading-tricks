package settingdust.preloading_tricks.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public interface MinecraftAdapter {
    Supplier<MinecraftAdapter> INSTANCE = Suppliers.memoize(() -> ServiceLoaderUtil.findService(MinecraftAdapter.class));

    static MinecraftAdapter getInstance() {
        return INSTANCE.get();
    }
}
