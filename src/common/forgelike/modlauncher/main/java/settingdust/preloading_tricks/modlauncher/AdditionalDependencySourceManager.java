package settingdust.preloading_tricks.modlauncher;

import com.google.common.base.Suppliers;
import settingdust.preloading_tricks.util.ServiceLoaderUtil;

import java.nio.file.Path;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public interface AdditionalDependencySourceManager {
    @SuppressWarnings("RedundantTypeArguments")
    Supplier<AdditionalDependencySourceManager> supplier =
        Suppliers.<AdditionalDependencySourceManager>memoize(() -> ServiceLoaderUtil.findService(
            AdditionalDependencySourceManager.class,
            ServiceLoader.load(
                AdditionalDependencySourceManager.class,
                AdditionalDependencySourceManager.class.getClassLoader()
            )
        ));

    static <I extends AdditionalDependencySourceManager> I get() {
        return (I) supplier.get();
    }

    void add(Path path, String name);

    void addAll(Collection<Path> paths, String name);
}
