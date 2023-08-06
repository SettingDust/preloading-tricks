package settingdust.preloadingtricks.util;

import org.slf4j.Logger;

import java.util.ServiceLoader;

public class ServiceLoaderUtil {
    public static <T> void loadServices(Class<T> clazz, ServiceLoader<T> serviceLoader, Logger logger) {
        final var iterator = serviceLoader.stream().iterator();
        var hasNext = false;
        var empty = false;
        do {
            try {
                hasNext = iterator.hasNext();
                if (empty) break;
                empty = true;
            } catch (Throwable t) {
                empty = false;
                logger.error("Load service of {} failed {}", clazz.getName(), t.getMessage());
                logger.debug("Load service of " + clazz.getName() + " failed", t);
            }
        } while (!hasNext);
        while (hasNext) {
            final var provider = iterator.next();
            try {
                logger.info("Loading " + provider.type().getName());
                provider.get();
            } catch (Throwable t) {
                logger.error(
                        "Loading {} failed. Stacktrace is in DEBUG level. {}",
                        provider.type().getName(),
                        t.getMessage());
                logger.debug("Loading " + provider.type().getName() + " failed.", t);
            }

            try {
                hasNext = iterator.hasNext();
            } catch (Throwable t) {
                logger.error("Load service of {} failed {}", clazz.getName(), t.getMessage());
                logger.debug("Load service of " + clazz.getName() + " failed", t);
            }
        }
    }
}
