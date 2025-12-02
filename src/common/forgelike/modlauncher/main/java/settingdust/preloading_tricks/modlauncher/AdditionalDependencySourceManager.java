package settingdust.preloading_tricks.modlauncher;

import java.nio.file.Path;
import java.util.Collection;

public interface AdditionalDependencySourceManager {
    void add(Path path, String name);

    void addAll(Collection<Path> paths, String name);
}
