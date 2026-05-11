package settingdust.preloading_tricks.lexforge.virtual_mod;

import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModProvider;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

public class VirtualModProvider implements IModProvider {
    @Override
    public String name() {
        return "Preloading Tricks Virtual";
    }

    @Override
    public void scanFile(final IModFile modFile, final Consumer<Path> pathConsumer) {

    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {

    }

    @Override
    public boolean isValid(final IModFile modFile) {
        return true;
    }
}
