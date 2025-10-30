package settingdust.preloading_tricks.neoforge.virtual_mod;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jetbrains.annotations.Nullable;

public class VirtualModContainer extends ModContainer {
    public VirtualModContainer(final IModInfo info) {
        super(info);
    }

    @Override
    public @Nullable IEventBus getEventBus() {
        return null;
    }
}
