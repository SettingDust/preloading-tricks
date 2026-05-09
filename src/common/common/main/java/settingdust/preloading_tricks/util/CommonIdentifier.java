package settingdust.preloading_tricks.util;

public interface CommonIdentifier {
    String namespace();

    String path();

    static CommonIdentifier of(String namespace, String path) {
        return IdentifierFactory.getInstance().create(namespace, path);
    }

    static CommonIdentifier parse(String value) {
        return IdentifierFactory.getInstance().parse(value);
    }
}
