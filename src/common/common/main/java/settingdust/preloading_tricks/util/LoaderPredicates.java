package settingdust.preloading_tricks.util;

import com.google.common.base.Suppliers;

import java.util.function.BooleanSupplier;

public enum LoaderPredicates {
    Fabric(() -> {
        var result = false;
        try {
            Class.forName("net.fabricmc.loader.impl.launch.knot.Knot");
            result = true;
        } catch (ClassNotFoundException ignored) {
        }
        return result;
    }),
    Forge(() -> {
        var result = false;
        try {
            Class.forName("cpw.mods.bootstraplauncher.BootstrapLauncher");
            Class.forName("net.minecraftforge.fml.loading.FMLLoader");
            result = true;
        } catch (ClassNotFoundException ignored) {
        }
        return result;
    }),
    NeoForgeModLauncher(() -> {
        var result = false;
        try {
            Class.forName("cpw.mods.bootstraplauncher.BootstrapLauncher");
            Class.forName("net.neoforged.fml.loading.FMLLoader");
            Class.forName("cpw.mods.jarhandling.SecureJar");
            result = true;
        } catch (ClassNotFoundException ignored) {
        }
        return result;
    }),
    NeoForge(() -> {
        var result = false;
        try {
            Class.forName("net.neoforged.fml.classloading.ModuleClassLoader");
            result = true;
        } catch (ClassNotFoundException ignored) {
        }
        return result;
    });
    private final BooleanSupplier predicate;

    LoaderPredicates(BooleanSupplier predicate) {
        this.predicate = Suppliers.memoize(predicate::getAsBoolean)::get;
    }

    public boolean test() {
        return predicate.getAsBoolean();
    }

    public void throwIfNot() {
        if (!test()) {
            throw new IllegalStateException(name() + " is not for current loader");
        }
    }
}
