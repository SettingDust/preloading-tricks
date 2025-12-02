package settingdust.preloading_tricks.api;

import java.nio.file.Path;
import java.util.Collection;

public interface ModCandidatesManager {
    void add(Path path);

    void addAll(Collection<Path> paths);
}
