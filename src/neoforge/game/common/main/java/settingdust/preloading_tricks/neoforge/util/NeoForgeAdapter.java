package settingdust.preloading_tricks.neoforge.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.neoforged.api.distmarker.Dist;
import settingdust.preloading_tricks.util.ServiceLoaderUtil;

public interface NeoForgeAdapter {
    Supplier<NeoForgeAdapter> INSTANCE = Suppliers.memoize(
        () -> ServiceLoaderUtil.findService(NeoForgeAdapter.class)
    );

    static NeoForgeAdapter getInstance() {
        return INSTANCE.get();
    }

    Dist getDist();
}
