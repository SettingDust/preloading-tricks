package settingdust.preloading_tricks.api;

import com.google.common.base.Suppliers;
import settingdust.preloading_tricks.util.ServiceLoaderUtil;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface PreloadingTricksModManager<M> {
    Supplier<PreloadingTricksModManager<?>> supplier =
        Suppliers.memoize(() -> ServiceLoaderUtil.findService(PreloadingTricksModManager.class));

    /**
     * @return Singleton instance
     * @param <I> should be implementation class or {@link PreloadingTricksModManager} with right {@link M}
     */
    static <I extends PreloadingTricksModManager<?>> I get() {
        return (I) supplier.get();
    }

    Collection<M> all();

    void add(M mod);

    void addAll(Collection<M> mod);

    void remove(M mod);

    void removeIf(Predicate<M> predicate);

    void removeAll(Collection<M> mods);

    void removeById(String id);

    void removeByIds(Set<String> ids);
}
