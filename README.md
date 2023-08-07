# Preloading Tricks

A mod that adds a hook before setting up the main mod. Mainly used for [ModSets](https://github.com/SettingDust/ModSets) and [AutoModpack](https://github.com/Skidamek/AutoModpack).    
It only depends on the loader version. It may work on lower versions than these, but that needs testing and feedback.  
* Fabric 0.14
* Quilt 0.20
* Forge 45

## Callbacks

### Setup Mod
  
The callback is using Java's [`ServiceLoader`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ServiceLoader.html) feature.    
It will be invoked just before setting up the mods.  
  
Using `SetupModService` implementations for platforms to control the mod loading.  
  
* Forge: https://github.com/SettingDust/preloading-tricks/blob/main/fml-45/src/main/java/settingdust/preloadingtricks/forge/ForgeLanguageProviderCallback.java#L98
* Fabric: https://github.com/SettingDust/preloading-tricks/blob/main/fabric-loader-0.14/src/main/java/settingdust/preloadingtricks/fabric/FabricLanguageProviderCallback.java#L64
* Quilt: https://github.com/SettingDust/preloading-tricks/blob/main/quilt-loader-0.20/src/main/java/settingdust/preloadingtricks/quilt/QuiltLanguageProviderCallback.java#L62

With the callback, you can control the mod loading.   
Notice: It's not recommended to add mods since it can't load mods from a file or classpath. Using `IModLocator` is officially supported by Forge.

### Language Provider
  
The callback is using Java's [`ServiceLoader`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ServiceLoader.html).  
It's invoked much earlier than setting up the main mod.  
