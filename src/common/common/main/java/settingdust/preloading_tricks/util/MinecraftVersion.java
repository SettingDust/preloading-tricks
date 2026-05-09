package settingdust.preloading_tricks.util;

import java.util.Arrays;

public enum MinecraftVersion {
    V1201("1.20.1"),
    V1211("1.21"),
    V261("26.1");

    private final String versionPrefix;

    MinecraftVersion(String versionPrefix) {
        this.versionPrefix = versionPrefix;
    }

    public String getVersionPrefix() {
        return versionPrefix;
    }

    public boolean matches(String versionName) {
        return versionName.startsWith(versionPrefix);
    }

    public boolean isCurrent() {
        return getCurrent() == this;
    }

    public void requireCurrent() {
        if (!isCurrent()) {
            throw new IllegalStateException(
                "Expected Minecraft version " + this + ", got " + getCurrent() + " (" + getCurrentVersionName() + ")"
            );
        }
    }

    public static String getCurrentVersionName() {
        return CurrentHolder.VERSION_NAME;
    }

    public static MinecraftVersion getCurrent() {
        return CurrentHolder.CURRENT;
    }

    public static MinecraftVersion of(String versionName) {
        return Arrays.stream(values())
            .filter(v -> v.matches(versionName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported Minecraft version: " + versionName));
    }

    private static final class CurrentHolder {
        static final String VERSION_NAME = MinecraftVersionNameProvider.getInstance().currentVersionName();
        static final MinecraftVersion CURRENT = of(VERSION_NAME);
    }
}
