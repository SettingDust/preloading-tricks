package settingdust.preloading_tricks.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public interface MinecraftVersionNameProvider {
    Supplier<MinecraftVersionNameProvider> INSTANCE = Suppliers.memoize(
        () -> ServiceLoaderUtil.findService(MinecraftVersionNameProvider.class)
    );

    static MinecraftVersionNameProvider getInstance() {
        return INSTANCE.get();
    }

    String currentVersionName();
}
