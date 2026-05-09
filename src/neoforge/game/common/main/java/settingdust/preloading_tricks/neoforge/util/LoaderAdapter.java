package settingdust.preloading_tricks.neoforge.util;

import net.neoforged.fml.loading.LoadingModList;
import settingdust.preloading_tricks.util.LoaderAdapter;

public class LoaderAdapter implements LoaderAdapter {
    @Override
    public boolean isClient() {
        return NeoForgeAdapter.getInstance().getDist().isClient();
    }

    @Override
    public java.nio.file.Path gameDir() {
        return net.neoforged.fml.loading.FMLPaths.GAMEDIR.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return LoadingModList.get().getModFileById(modId) != null;
    }
}
