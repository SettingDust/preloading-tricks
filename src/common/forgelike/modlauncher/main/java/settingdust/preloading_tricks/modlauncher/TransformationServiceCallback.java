package settingdust.preloading_tricks.modlauncher;

import com.google.common.base.Suppliers;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.LauncherAccessor;
import settingdust.preloading_tricks.util.ServiceLoaderUtil;

import java.util.function.Supplier;

public interface TransformationServiceCallback {
    TransformationServiceCallback invoker = new TransformationServiceCallback() {
        private Supplier<Iterable<TransformationServiceCallback>> supplier =
            Suppliers.memoize(() -> ServiceLoaderUtil.findServices(
                TransformationServiceCallback.class,
                LauncherAccessor.getModuleLayer(IModuleLayerManager.Layer.SERVICE)
            ));

        @Override
        public void init() {
            for (final var callback : supplier.get()) {
                callback.init();
            }
        }
    };


    void init();
}
