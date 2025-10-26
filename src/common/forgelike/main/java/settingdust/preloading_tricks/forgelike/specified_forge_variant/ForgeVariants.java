package settingdust.preloading_tricks.forgelike.specified_forge_variant;

import java.util.HashMap;
import java.util.Map;

public enum ForgeVariants {
    LexForge, NeoForge;

    public static Map<String, ForgeVariants> BY_NAME = new HashMap<>();
    public static String MANIFEST_KEY = "ForgeVariant";

    static {
        for (var variant : values()) {
            BY_NAME.put(variant.name().toLowerCase(), variant);
        }
    }
}
