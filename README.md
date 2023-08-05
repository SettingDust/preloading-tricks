# Preloading Tricks

A mod that add hook before setup mod. Mainly using for [ModSets](https://github.com/SettingDust/ModSets)  
It's only depends on the loader version. May working on lower version but need test and feedback
  * Fabric 0.14
  * Quilt 0.20
  * Forge 45

## Callbacks

### Setup Mod

https://github.com/SettingDust/preloading-tricks/blob/main/preloading-callbacks/src/main/java/settingdust/preloadingtricks/SetupModCallback.java
The callback is using java [`ServiceLoader`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ServiceLoader.html)  
It will be invoked at just before setup mods.   
  
Using `SetupModService` implementations for platforms to control the mod loading.  
It's not recommend to using the service to add mods. Using methods provided by loader is better.  

  * Forge: https://github.com/SettingDust/preloading-tricks/blob/main/fml-45/src/main/java/settingdust/preloadingtricks/forge/ForgeLanguageProviderCallback.java#L98
  * Fabric: https://github.com/SettingDust/preloading-tricks/blob/main/fabric-loader-0.14/src/main/java/settingdust/preloadingtricks/fabric/FabricLanguageProviderCallback.java#L64
  * Quilt: https://github.com/SettingDust/preloading-tricks/blob/main/quilt-loader-0.20/src/main/java/settingdust/preloadingtricks/quilt/QuiltLanguageProviderCallback.java#L62
  
With the callback you can control the mod loading.   
Notice it's not recommend to add mod since it can't load mod from file or classpath. Using `IModLocator` is officially supported by forge.

### Language Provider

The callback is using java [`ServiceLoader`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ServiceLoader.html).    
It's a bit earlier than setup mod.    
