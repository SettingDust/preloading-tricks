### Overview

**Preloading Tricks** is a developer-only mod that enables advanced operations during the very early stage of the mod loader.
It provides early hooks, Java Instrumentation-based class transformation, and SPI-driven callbacks for loader extension.

Supported environments:

* **1.20.1** – Forge & Fabric
* **1.21.x** – NeoForge & Fabric

You can test on more version since it depends on the loader version instead of Minecraft version

---

### Features

* **Early loader entrypoint** via `PreloadingEntrypoint` (SPI-based)

    * Register callbacks using `PreloadingTricksCallbacks` events:
        * `SETUP_LANGUAGE_ADAPTER` – early language adapter setup stage
        * `COLLECT_MOD_CANDIDATES` – dynamically add mod candidate paths before discovery
        * `SETUP_MODS` – modify mod list (add/remove/query via `ModManager` API)
* **Instrumentation-powered `ClassTransform`**
  Can transform already-loaded classes, including Java core and classloader classes.
  * **Forge-like (Forge/NeoForge)**: Configure via `MANIFEST.MF` (see below)
  * **Fabric**: Use [AsmFabricLoader](https://github.com/FlorianMichael/AsmFabricLoader) entrypoint

* **Forge variant detection** (Forge-like only)
  * Access `ForgeVariants` to detect specific Forge/NeoForge versions at runtime

---

### ClassTransform Configuration (Forge-like only)

For **Forge/NeoForge**, add the following attribute to your `MANIFEST.MF`:

```
ClassTransformConfig: xxxx.classtransform.json
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

For **Fabric**, use [AsmFabricLoader](https://github.com/FlorianMichael/AsmFabricLoader#class-transform-requires-java-9) entrypoint instead.

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
