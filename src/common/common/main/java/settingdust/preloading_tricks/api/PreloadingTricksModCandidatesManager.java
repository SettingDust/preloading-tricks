package settingdust.preloading_tricks.api;

import com.google.common.base.Suppliers;
import settingdust.preloading_tricks.util.ServiceLoaderUtil;

import java.nio.file.Path;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public interface PreloadingTricksModCandidatesManager {
    @SuppressWarnings("RedundantTypeArguments")
    Supplier<PreloadingTricksModCandidatesManager> supplier =
        Suppliers.<PreloadingTricksModCandidatesManager>memoize(() -> ServiceLoaderUtil.findService(
            PreloadingTricksModCandidatesManager.class,
            ServiceLoader.load(
                PreloadingTricksModCandidatesManager.class,
                PreloadingTricksModCandidatesManager.class.getClassLoader()
            )
        ));

    static <I extends PreloadingTricksModCandidatesManager> I get() {
        return (I) supplier.get();
    }

    void add(Path path);

    void addAll(Collection<Path> paths);
}
