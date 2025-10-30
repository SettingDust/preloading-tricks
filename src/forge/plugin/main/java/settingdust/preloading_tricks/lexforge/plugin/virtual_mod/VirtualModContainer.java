package settingdust.preloading_tricks.lexforge.plugin.virtual_mod;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.forgespi.language.IModInfo;

public class VirtualModContainer extends ModContainer {
    private final IModInfo info;

    public VirtualModContainer(final IModInfo info) {
        super(info);
        this.info = info;
        this.contextExtension = () -> null;
        this.extensionPoints.remove(IExtensionPoint.DisplayTest.class);
    }

    @Override
    public boolean matches(final Object object) {
        return object == info;
    }

    @Override
    public Object getMod() {
        return info;
    }
}
