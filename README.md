# Preloading Tricks

A mod that adds hooks before setting up the main mod, providing early callbacks for mod initialization and language adapter setup. Mainly used for [ModSets](https://github.com/SettingDust/ModSets)

It only depends on the loader version. It may work on lower versions than these, but that needs testing and feedback.

* \>= Fabric 0.17
* \>= Forge 40
* \>= NeoForge (1.21+)

## Features

- **Early Callbacks**: Intercept mod loading at critical initialization points
- **Mod Management**: Dynamically add, remove, or query mods during the preloading phase
- **Virtual Mods**: Create virtual/dummy mods for dynamic injection and testing
- **Multi-Platform Support**: Works with Fabric, Forge, and NeoForge loaders
- **ServiceLoader-Based**: Uses Java's standard ServiceLoader mechanism for extensibility

## API Usage

### Implementing Callbacks

To hook into the mod loading process, implement the `PreloadingTricksCallback` interface:

```java
import settingdust.preloading_tricks.api.PreloadingTricksCallback;

public class MyCallback implements PreloadingTricksCallback {
    @Override
    public void onSetupLanguageAdapter() {
        // Called during language adapter setup phase
        // Invoked much earlier than mod setup
    }

    @Override
    public void onSetupMods() {
        // Called during mod setup phase
        // Can be used to modify the mod list, inject virtual mods, etc.
    }
}
```

Register your implementation by creating a service file:
- **File**: `META-INF/services/settingdust.preloading_tricks.api.PreloadingTricksCallback`
- **Content**: Fully qualified class name of your implementation (one per line)

### Mod Management

Use `PreloadingTricksModManager` to manage mods during the preloading phase:

```java
import settingdust.preloading_tricks.api.PreloadingTricksModManager;

public class MyCallback implements PreloadingTricksCallback {
    @Override
    public void onSetupMods() {
        var modManager = PreloadingTricksModManager.get();
        
        // Query all mods
        var allMods = modManager.all();
        
        // Remove mods by ID
        modManager.removeById("minecraft");
        modManager.removeByIds(Set.of("mod1", "mod2"));
        
        // Remove mods with predicate
        modManager.removeIf(mod -> mod.getId().startsWith("test"));
        
        // Create virtual mods
        var virtualMod = modManager.createVirtualMod("virtual_mod", Paths.get("/path/to/ref"));
        modManager.add(virtualMod);
    }
}
```

### Callbacks

#### Setup Mod

The callback is invoked just before setting up the mods. With this callback, you can control the mod loading:

* Modify the mod list (add/remove mods)
* Inject virtual mods

**Note**: It's not recommended to add real mods from files or classpath, as the mod loader handles file-based mod discovery. Use `IModLocator` (Forge) or platform-specific APIs for proper mod discovery.

#### Language Provider

The callback is invoked much earlier than mod setup, during the language adapter initialization phase. This is useful for:

* Early platform-specific initialization

## Class Transform

Preloading Tricks provides a class transformation mechanism built on the [ClassTransform](https://github.com/LennyMcLennington/ClassTransform) library. For detailed information, refer to the [official ClassTransform wiki](https://github.com/Lenni0451/ClassTransform/wiki).

### Configuration Format

#### Fabric

```json
{
  "package": "your.package.name",
  "java": [
    "path.to.YourTransformer"
  ],
  "mixins": []
}
```

#### Forge / NeoForge

```json
{
  "package": "your.package.name",
  "transformers": [
    "path.to.YourTransformer"
  ]
}
```

### Registering Configuration

#### Via Manifest

Add a `ClassTransformConfig` attribute to your JAR manifest:

```manifest
ClassTransformConfig: your.classtransform.json
```

Multiple files can be separated by commas:
```manifest
ClassTransformConfig: config1.json, config2.json
```

#### Via Code (Forge/NeoForge)

```java
import settingdust.preloading_tricks.forgelike.class_transform.ClassTransformBootstrap;

ClassTransformBootstrap.INSTANCE.addConfig("your.classtransform.json");
```

### Creating Transformers

Transformers use ClassTransform annotations. Example:

```java
import net.lenni0451.classtransform.annotations.*;

@CTransformer(TargetClass.class)
public class MyTransformer {
    @CInline
    @CInject(method = "targetMethod", target = @CTarget("HEAD"))
    private void injected() {
        // Your code here
    }
}
```

For more details, see the [ClassTransform wiki](https://github.com/Lenni0451/ClassTransform/wiki).

## ForgeVariant Configuration

For Forge-like loaders (LexForge and NeoForge), mods can specify their variant using the `ForgeVariant` manifest attribute. Preloading Tricks uses this attribute to prevent loading mods that are incompatible with the current Forge variant.

### Purpose

When using jar-in-jar packaging (bundling multiple platform-specific JARs within a single mod JAR), the `ForgeVariant` attribute is critical for preventing loader conflicts:

- **Variant Matching**: The loader will only load mods that match the current Forge variant (LexForge or NeoForge)
- **Conflict Prevention**: Without this attribute, the loader may load both LexForge and NeoForge variants simultaneously, causing duplicate class definitions
- **Class Collision Avoidance**: Since both variants contain identical package structures, loading both causes crashes due to conflicting class definitions

### Usage

Add the `ForgeVariant` attribute to your mod's manifest file (`META-INF/MANIFEST.MF`):

```manifest
ForgeVariant: lexforge
```

or

```manifest
ForgeVariant: neoforge
```

This tells Preloading Tricks which Forge variant this particular JAR is designed for, allowing it to selectively load only the appropriate version.

### Supported Variants

The following ForgeVariant values are supported:

- **`lexforge`**
- **`neoforge`**