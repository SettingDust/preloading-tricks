package settingdust.preloading_tricks.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public interface IdentifierFactory {
    Supplier<IdentifierFactory> INSTANCE = Suppliers.memoize(
        () -> ServiceLoaderUtil.findService(IdentifierFactory.class)
    );

    static IdentifierFactory getInstance() {
        return INSTANCE.get();
    }

    CommonIdentifier create(String namespace, String path);

    CommonIdentifier parse(String value);
}
