package settingdust.preloading_tricks.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public interface LoaderAdapter {
    Supplier<LoaderAdapter> INSTANCE = Suppliers.memoize(() -> ServiceLoaderUtil.findService(LoaderAdapter.class));

    static LoaderAdapter getInstance() {
        return INSTANCE.get();
    }

    boolean isClient();

    boolean isModLoaded(String modId);
}
