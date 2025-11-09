### Overview

**Preloading Tricks** is a developer-only mod that enables advanced operations during the very early stage of the mod loader.
It provides early hooks, Java Instrumentation-based class transformation, and SPI-driven callbacks for loader extension.

Supported environments:

* **1.20.1** – Forge & Fabric
* **1.21.x** – NeoForge & Fabric

You can test on more version since it's depends on the loader version instead of Minecraft version

---

### Features

* **Early loader entrypoints** via `PreloadingTricksCallback`

    * `onSetupLanguageAdapter` – early setup stage
    * `onSetupMods` – modify mod list (add/remove via loader API)
* **Instrumentation-powered `ClassTransform`**
  Can transform already-loaded classes, including Java core and classloader classes.

---

### ClassTransform Configuration

To enable transformations, add the following attribute to your `MANIFEST.MF`:

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

---

**For developers who need to reach into the earliest moments of Minecraft’s loading process.**
