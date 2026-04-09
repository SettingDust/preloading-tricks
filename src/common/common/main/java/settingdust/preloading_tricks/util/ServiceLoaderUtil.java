package settingdust.preloading_tricks.util;

import org.apache.logging.log4j.Logger;
import settingdust.preloading_tricks.PreloadingTricks;

import java.util.*;

public final class ServiceLoaderUtil {
    public static final Logger DEFAULT_LOGGER = PreloadingTricks.LOGGER;

    private ServiceLoaderUtil() {
    }

    public static <T> T findService(Class<T> clazz) {
        return findService(clazz, ServiceLoader.load(clazz), DEFAULT_LOGGER);
    }

    public static <T> T findService(Class<T> clazz, ServiceLoader<T> serviceLoader, Logger logger) {
        List<T> services = findServices(clazz, serviceLoader, logger, true);
        return services.get(0);
    }

    public static <T> Iterable<T> findServices(Class<T> clazz, ModuleLayer layer) {
        return findServices(clazz, ServiceLoader.load(layer, clazz), DEFAULT_LOGGER, true);
    }

    public static <T> List<T> findServices(Class<T> clazz, boolean required) {
        return findServices(clazz, ServiceLoader.load(clazz), DEFAULT_LOGGER, required);
    }

    public static <T> List<T> findServices(
            Class<T> clazz,
            ServiceLoader<T> serviceLoader,
            Logger logger,
            boolean required) {
        String prefix = String.format("[%s] ", logger.getName());
        Iterator<ServiceLoader.Provider<T>> iterator = serviceLoader.stream().iterator();
        List<Throwable> errors = new ArrayList<>();
        List<T> result = new ArrayList<>();

        ServiceLoader.Provider<T> current = findNext(iterator, errors);
        while (current != null) {
            String providerName = current.type().getName();
            logger.debug("{}Loading {}", prefix, providerName);

            try {
                result.add(current.get());
            } catch (Throwable t) {
                IllegalStateException e = new IllegalStateException(prefix + "Loading " + providerName + " failed", t);
                errors.add(e);
                logger.debug("{}", e.getMessage(), e);
            }

            current = findNext(iterator, errors);
        }

        if (!result.isEmpty() || !required) {
            return result;
        }

        IllegalStateException exception = new IllegalStateException("Load service of " + clazz + " failed");
        if (errors.isEmpty()) {
            exception.addSuppressed(new NoSuchElementException("Can't find service for " + clazz));
        }
        for (Throwable error : errors) {
            exception.addSuppressed(error);
        }
        throw exception;
    }

    private static <T> ServiceLoader.Provider<T> findNext(
            Iterator<ServiceLoader.Provider<T>> iterator,
            List<Throwable> errors) {
        ServiceLoader.Provider<T> current = null;
        do {
            try {
                current = iterator.next();
            } catch (NoSuchElementException e) {
                return null;
            } catch (Throwable t) {
                errors.add(t);
            }
        } while (current == null);
        return current;
    }

    public static <T> int loadServices(
            Class<T> clazz,
            ServiceLoader<T> serviceLoader,
            Logger logger,
            boolean required
    ) {
        int count = 0;
        for (T ignored : findServices(clazz, serviceLoader, logger, required)) {
            count++;
        }
        return count;
    }

    public static <T> int loadServices(Class<T> clazz) {
        return loadServices(clazz, ServiceLoader.load(clazz), DEFAULT_LOGGER, true);
    }

    public static <T> int loadServices(Class<T> clazz, boolean required) {
        return loadServices(clazz, ServiceLoader.load(clazz), DEFAULT_LOGGER, required);
    }

    public static <T> int loadServices(Class<T> clazz, ServiceLoader<T> serviceLoader, boolean required) {
        return loadServices(clazz, serviceLoader, DEFAULT_LOGGER, required);
    }
}
