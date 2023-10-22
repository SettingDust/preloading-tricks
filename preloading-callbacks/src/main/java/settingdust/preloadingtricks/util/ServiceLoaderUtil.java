package settingdust.preloadingtricks.util;

import org.apache.logging.log4j.Logger;

import java.util.ServiceLoader;

public class ServiceLoaderUtil {

    public static <T> void loadServices(Class<T> clazz, ServiceLoader<T> serviceLoader, Logger logger) {
        final var prefix = String.format("[%s] ", logger.getName());
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
                logger.error(prefix + "Load service of {} failed: {}", clazz.getName(), t.getMessage());
                logger.debug(prefix + "Load service of " + clazz.getName() + " failed", t);
            }
        } while (!hasNext);
        while (hasNext) {
            final var provider = iterator.next();

            String providerName = provider.type().getName();

            logger.info(prefix + "Loading " + providerName);

            try {
                provider.get();
            } catch (Throwable t) {
                if (!providerName.startsWith("settingdust.preloadingtricks.")) {
                    logger.error(prefix + "Loading " + providerName + " failed");
                }
                logger.debug(prefix + "Loading " + providerName + " failed", t);
            }

            try {
                hasNext = iterator.hasNext();
            } catch (Throwable t) {
                logger.error(prefix + "Load service of {} failed: {}", clazz.getName(), t.getMessage());
                logger.debug(prefix + "Load service of " + clazz.getName() + " failed", t);
            }
        }
    }
}
