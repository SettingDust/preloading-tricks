package settingdust.preloading_tricks.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.util.List;

public interface Entrypoint {
    Supplier<List<Entrypoint>> SERVICES = Suppliers.memoize(() -> ServiceLoaderUtil.findServices(Entrypoint.class, false));

    static List<Entrypoint> services() {
        return SERVICES.get();
    }

    static void construct() {
        services().forEach(Entrypoint::onConstruct);
    }

    static void init() {
        services().forEach(Entrypoint::onInit);
    }

    static void clientInit() {
        services().forEach(Entrypoint::onClientInit);
    }

    default void onConstruct() {}

    default void onInit() {}

    default void onClientInit() {}
}
