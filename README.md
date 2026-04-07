### Overview

**Preloading Tricks** is a developer-only mod that enables advanced operations during the very early stage of the mod loader.
It provides early hooks, Java Instrumentation-based class transformation, and SPI-driven callbacks for loader extension.

Supported environments:

* **Fabric** – 1.20.1, 1.21.x
* **Forge (LexForge)** – 1.20.1
* **NeoForge** – 1.20.6, 1.21.1, 1.21.10+

You can test on more versions since it depends on the loader version instead of the Minecraft version.

---

### Features

* **Early loader entrypoint** via `PreloadingEntrypoint` (SPI-based)

    * Register callbacks using `PreloadingTricksCallbacks` events:
        * `SETUP_LANGUAGE_ADAPTER` – early language adapter setup stage
        * `COLLECT_MOD_CANDIDATES` – dynamically add mod candidate paths before discovery
        * `SETUP_MODS` – modify mod list (add/remove/query via `ModManager` API)
* **Instrumentation-powered `ClassTransform`**
  Can transform already-loaded classes, including Java core and classloader classes.
  Configure via `MANIFEST.MF` on all platforms (see below).

* **Forge variant detection** (Forge-like only)
  * Access `ForgeVariants` to detect specific Forge/NeoForge versions at runtime

---

### Usage (Gradle)

Add the Maven repository and dependency to your build:

**Kotlin DSL:**

```kotlin
repositories {
    maven("https://raw.githubusercontent.com/settingdust/maven/main/repository/")
}

dependencies {
  // Recommended: use the base artifact (no classifier).
  // Gradle variant matching / cloche will resolve the correct platform artifact automatically.
  implementation("settingdust.preloading_tricks:PreloadingTricks:VERSION")

  // Optional: pin to a specific classifier when you want to avoid accidentally using APIs
  // from other platforms.
  // implementation("settingdust.preloading_tricks:PreloadingTricks:VERSION:fabric")
  // implementation("settingdust.preloading_tricks:PreloadingTricks:VERSION:forge-service")
  // implementation("settingdust.preloading_tricks:PreloadingTricks:VERSION:neoforge-modlauncher")
  // implementation("settingdust.preloading_tricks:PreloadingTricks:VERSION:neoforge-fancy-mod-loader")
}
```

Replace `VERSION` with the latest release (e.g. `3.5.9`).

If you are using [cloche](https://github.com/terrarium-earth/cloche), the corresponding variant is selected automatically based on loader/minecraft attributes.

---

### ClassTransform Configuration

For all platforms, add the following attribute to your `MANIFEST.MF`:

```
ClassTransformConfig: xxxx.classtransform.json
```

Multiple configs can be specified as comma-separated values:

```
ClassTransformConfig: first.classtransform.json,second.classtransform.json
```

Example config (`xxxx.classtransform.json`):

```json
{
  "package": "settingdust.preloading_tricks.neoforge.transformer",
  "transformers": [
    "mod_setup_hook.FMLLoaderTransformer"
  ]
}
```

---

### Forge Variant Detection (Forge-like only)

Allows a mod to load only on a specific Forge-like loader variant. This enables bundling both LexForge and NeoForge JARs together via jar-in-jar in a single mod file.

**Configuration:**

Specify your mod's target variant in `MANIFEST.MF`:
```
ForgeVariant: LexForge
```
or
```
ForgeVariant: NeoForge
```

Mods with a `ForgeVariant` specified will only load when running on the matching loader variant, allowing safe coexistence of variant-specific implementations.

---

**For developers who need to reach into the earliest moments of Minecraft’s loading process.**
