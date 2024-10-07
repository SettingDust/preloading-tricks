package settingdust.preloadingtricks.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Throwables;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

public class ServiceLoaderUtil {

    public static <T> void loadServices(Class<T> clazz, ServiceLoader<T> serviceLoader, Logger logger) {
        final var prefix = String.format("[%s] ", logger.getName());
        final var iterator = serviceLoader.stream().iterator();
        boolean hasNext = findNext(iterator, clazz, logger);
        while (hasNext) {
            final var provider = iterator.next();

            String providerName = provider.type().getName();

            logger.info("{}Loading {}", prefix, providerName);

            try {
                provider.get();
            } catch (Throwable t) {
                logger.error("{}Loading {} failed: {}", prefix, providerName, Throwables.getRootCause(t).toString());
                logger.debug("{}Loading {} failed", prefix, providerName, t);
            }

            hasNext = findNext(iterator, clazz, logger);
        }
    }

    private static <T> boolean findNext(Iterator<ServiceLoader.Provider<T>> iterator, Class<T> clazz, Logger logger) {
        final var prefix = String.format("[%s] ", logger.getName());
        boolean hasNext = false;
        do {
            try {
                hasNext = iterator.hasNext();
            } catch (Throwable t) {
                logger.error(
                    "{}Load service of {} failed: {}",
                    prefix,
                    clazz.getName(),
                    Throwables.getRootCause(t).toString()
                );
                logger.debug("{}Load service of {} failed", prefix, clazz.getName(), t);
                try {
                    iterator.next();
                } catch (NoSuchElementException ignored) {
                    return false;
                }
            }
        } while (!hasNext);
        return true;
    }
}
