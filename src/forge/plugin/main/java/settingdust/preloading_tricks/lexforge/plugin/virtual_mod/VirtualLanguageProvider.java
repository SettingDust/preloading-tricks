package settingdust.preloading_tricks.lexforge.plugin.virtual_mod;

import net.lenni0451.reflect.stream.RStream;
import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VirtualLanguageProvider implements IModLanguageProvider {
    @Override
    public String name() {
        return "virtual";
    }

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {
        return result -> {
            var map = new HashMap<String, VirtualLanguageLoader>();
            for (final var info : result.getIModInfoData()) {
                for (final var mod : info.getMods()) {
                    map.put(mod.getModId(), VirtualLanguageLoader.INSTANCE);
                }
            }
            result.addLanguageLoader(map);
        };
    }

    @Override
    public <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(final Supplier<R> consumeEvent) {}

    public static class VirtualLanguageLoader implements IModLanguageProvider.IModLanguageLoader {
        public static final VirtualLanguageLoader INSTANCE = new VirtualLanguageLoader();

        @Override
        public <T> T loadMod(final IModInfo info, final ModFileScanData modFileScanResults, final ModuleLayer layer) {
            try {
                var clazz = Class.forName(
                    "settingdust.preloading_tricks.lexforge.plugin.virtual_mod.VirtualModContainer",
                    true,
                    Thread.currentThread().getContextClassLoader()
                );
                var stream = RStream.of(clazz);
                var constructor = stream.constructors().by(IModInfo.class);
                return constructor.newInstance(info);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
