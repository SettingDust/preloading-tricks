package settingdust.preloading_tricks.util;

import org.apache.logging.log4j.Logger;
import settingdust.preloading_tricks.PreloadingTricks;

import java.util.*;

public final class ServiceLoaderUtil {
    public static Logger defaultLogger = PreloadingTricks.LOGGER;

    private ServiceLoaderUtil() {
    }

    public static <T> ServiceLoader<T> load(Class<T> clazz, ModuleLayer layer) {
        return ServiceLoader.load(layer, clazz);
    }

    public static <T> ServiceLoader<T> load(Class<T> clazz) {
        return ServiceLoader.load(clazz);
    }

    public static <T> ServiceLoader<T> load(Class<T> clazz, ClassLoader cl) {
        return ServiceLoader.load(clazz, cl);
    }

    public static <T> T findService(Class<T> clazz, ServiceLoader<T> serviceLoader) {
        return findService(clazz, serviceLoader, defaultLogger);
    }

    public static <T> T findService(Class<T> clazz, ServiceLoader<T> serviceLoader, Logger logger) {
        Iterator<T> it = findServices(clazz, serviceLoader, logger, true).iterator();
        if (it.hasNext()) {
            return it.next();
        }
        throw new NoSuchElementException("No service found for " + clazz);
    }

    public static <T> Iterable<T> findServices(Class<T> clazz, ModuleLayer layer) {
        return findServices(clazz, load(clazz, layer), defaultLogger, true);
    }

    public static <T> Iterable<T> findServices(Class<T> clazz, ServiceLoader<T> serviceLoader, boolean required) {
        return findServices(clazz, serviceLoader, defaultLogger, required);
    }

    public static <T> Iterable<T> findServices(
        Class<T> clazz,
        ServiceLoader<T> serviceLoader,
        Logger logger,
        boolean required
    ) {
        List<T> results = new ArrayList<>();
        String prefix = "[" + logger.getName() + "] ";
        Iterator<ServiceLoader.Provider<T>> iterator = serviceLoader.stream().iterator();
        List<Throwable> errors = new ArrayList<>();
        Optional<ServiceLoader.Provider<T>> current = findNext(iterator, errors);
        boolean found = false;

        while (current.isPresent()) {
            ServiceLoader.Provider<T> provider = current.get();
            String providerName = provider.type().getName();

            logger.debug("{}Loading {}", prefix, providerName);
            try {
                results.add(provider.get());
                found = true;
            } catch (Throwable t) {
                IllegalStateException e = new IllegalStateException(prefix + "Loading " + providerName + " failed", t);
                errors.add(e);
                logger.debug(e);
            }

            current = findNext(iterator, errors);
        }

        if (!found && required) {
            IllegalStateException exception = new IllegalStateException("Load service of " + clazz + " failed");
            if (errors.isEmpty()) {
                exception.addSuppressed(new NoSuchElementException("Can't find service for " + clazz));
            }
            for (Throwable t : errors) {
                exception.addSuppressed(t);
            }
            throw exception;
        }

        return results;
    }

    private static <T> Optional<ServiceLoader.Provider<T>> findNext(
        Iterator<ServiceLoader.Provider<T>> iterator,
        List<Throwable> errors
    ) {
        ServiceLoader.Provider<T> current = null;
        while (true) {
            try {
                if (!iterator.hasNext()) {
                    return Optional.empty();
                }
                current = iterator.next();
                break;
            } catch (NoSuchElementException e) {
                return Optional.empty();
            } catch (Throwable t) {
                errors.add(t);
            }
        }
        return Optional.ofNullable(current);
    }

    // Convenience overloads
    public static <T> T findService(Class<T> clazz) {
        return findService(clazz, load(clazz), defaultLogger);
    }

    public static <T> Iterable<T> findServices(Class<T> clazz) {
        return findServices(clazz, load(clazz), defaultLogger, true);
    }

    public static <T> Iterable<T> findServices(Class<T> clazz, boolean required) {
        return findServices(clazz, load(clazz), defaultLogger, required);
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
        return loadServices(clazz, load(clazz), defaultLogger, true);
    }

    public static <T> int loadServices(Class<T> clazz, boolean required) {
        return loadServices(clazz, load(clazz), defaultLogger, required);
    }

    public static <T> int loadServices(Class<T> clazz, ServiceLoader<T> serviceLoader,boolean required) {
        return loadServices(clazz, serviceLoader, defaultLogger, required);
    }
}