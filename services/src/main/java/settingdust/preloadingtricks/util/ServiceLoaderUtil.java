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
        var current = findNext(iterator, clazz, logger);
        while (current != null) {
            String providerName = current.type().getName();

            logger.info("{}Loading {}", prefix, providerName);

            try {
                current.get();
            } catch (Throwable t) {
                logger.error("{}Loading {} failed: {}", prefix, providerName, Throwables.getRootCause(t).toString());
                logger.debug("{}Loading {} failed", prefix, providerName, new IllegalStateException(t));
            }

            current = findNext(iterator, clazz, logger);
        }
    }

    private static <T> ServiceLoader.Provider<T> findNext(Iterator<ServiceLoader.Provider<T>> iterator, Class<T> clazz, Logger logger) {
        final var prefix = String.format("[%s] ", logger.getName());
        ServiceLoader.Provider<T> current = null;
        do {
            try {
                current = iterator.next();
            } catch (NoSuchElementException ignored) {
                return null;
            } catch (Throwable t) {
                logger.error(
                    "{}Load service of {} failed: {}",
                    prefix,
                    clazz.getName(),
                    Throwables.getRootCause(t).toString()
                );
                logger.debug("{}Load service of {} failed", prefix, clazz.getName(), new IllegalStateException(t));
            }
        } while (current == null);
        return current;
    }
}
