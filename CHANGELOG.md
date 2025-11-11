fix: `setAccessible` for `setMod`

## [2.5.1] - 2025-11-11
### :bug: Bug Fixes
- [`7565c40`](https://github.com/SettingDust/preloading-tricks/commit/7565c4068071de463d0bd6e64adc3e1fe2d34223) - **fabric**: include the language adapters for mods *(commit by [@SettingDust](https://github.com/SettingDust))*


## [2.5.0] - 2025-11-11
### :sparkles: New Features
- [`241435a`](https://github.com/SettingDust/preloading-tricks/commit/241435aeb02c4942d89678dbd34eddf326bd6483) - add candidate manager api *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`c5d111e`](https://github.com/SettingDust/preloading-tricks/commit/c5d111eb200a300ba0b00629e73896f37c71244f) - **fabric**: add candidate manager *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`353e72b`](https://github.com/SettingDust/preloading-tricks/commit/353e72b304e64f448c26d797f3209e8fbddea901) - **lexforge**: add candidate manager *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`52590b4`](https://github.com/SettingDust/preloading-tricks/commit/52590b409e345f104c0884e32e74fde96aebe7c0) - **forgelike**: don't move self to boot *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`850643b`](https://github.com/SettingDust/preloading-tricks/commit/850643b17d7d0ab54a39999251afe33a30464d95) - **neoforge/modlauncher**: add candidate manager *(commit by [@SettingDust](https://github.com/SettingDust))*

### :bug: Bug Fixes
- [`2ac20b8`](https://github.com/SettingDust/preloading-tricks/commit/2ac20b8573fcbeb9ffd6fbc001924427e1832cab) - **forgelike**: modify the reads after added all the modules *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`f45146e`](https://github.com/SettingDust/preloading-tricks/commit/f45146e936096910c01ed735fe89aaaccc6dfe62) - **forgelike**: find the service in the right class loader *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`fe60811`](https://github.com/SettingDust/preloading-tricks/commit/fe60811ccc43a995485e6bc11558f91389dd7ba0) - **fabric**: filter out the exists mods correctly *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`51ef112`](https://github.com/SettingDust/preloading-tricks/commit/51ef112c4173224bce87a8bb3a93a375c4ff7e57) - **neoforge/modlauncher**: work in dev env *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`6e344d4`](https://github.com/SettingDust/preloading-tricks/commit/6e344d46890c8fd4437907e9215afe69b155d5fd) - **forgelike**: don't load manager implementation on wrong variant *(commit by [@SettingDust](https://github.com/SettingDust))*


## [2.4.1] - 2025-11-07
### :sparkles: New Features
- [`5d6b0a2`](https://github.com/SettingDust/preloading-tricks/commit/5d6b0a2f7a1ad32c126e688569ce81db4382f224) - **forgelike**: read the modules after copy *(commit by [@SettingDust](https://github.com/SettingDust))*

### :bug: Bug Fixes
- [`4a07435`](https://github.com/SettingDust/preloading-tricks/commit/4a074354f90f150656a4e4a4bfb0b14ea5a1a36e) - **forgelike**: handle the dependencies in mod setup callback *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`5f853f5`](https://github.com/SettingDust/preloading-tricks/commit/5f853f50620f2edd188d26a2a675384e302f4dde) - **neoforge**: don't load the other loaders' services *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`20422f7`](https://github.com/SettingDust/preloading-tricks/commit/20422f73c4374c924dba5e940df458c213b4d0d5) - **neoforge/fancy_mod_loader**: correct the hooks to avoid crash *(commit by [@SettingDust](https://github.com/SettingDust))*


## [2.4.0] - 2025-11-01
### :sparkles: New Features
- [`d7d8b16`](https://github.com/SettingDust/preloading-tricks/commit/d7d8b163bcef8b4d0183eb734d3b8d8b4f9b35a5) - add more helper for module class loader modify *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`46349f1`](https://github.com/SettingDust/preloading-tricks/commit/46349f1e376c95715d3b3ad43ab7e081bd3e4812) - implement the module mover *(commit by [@SettingDust](https://github.com/SettingDust))*

### :bug: Bug Fixes
- [`0e3aa97`](https://github.com/SettingDust/preloading-tricks/commit/0e3aa9702229a2403c599189c99dff16ffb9ac39) - needn't dummy mod on fabric *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`757f187`](https://github.com/SettingDust/preloading-tricks/commit/757f187c84bd95b0c09ae3052a2ec71bf344646e) - **fabric**: call the setup mods in knot class loader *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`0de454c`](https://github.com/SettingDust/preloading-tricks/commit/0de454c022dce2155595800dc3bfc2a76b9ef2cf) - remove the package lookup and resolved roots from source when move *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`0e3d033`](https://github.com/SettingDust/preloading-tricks/commit/0e3d03393d83ecfb98575fb9da356dbe19bffd8f) - don't move module if needn't *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`6db279b`](https://github.com/SettingDust/preloading-tricks/commit/6db279b3ef5e99db74a5c29570487a4258654dc2) - virtual mod module name isn't modid *(commit by [@SettingDust](https://github.com/SettingDust))*

### :recycle: Refactors
- [`f6ba3d5`](https://github.com/SettingDust/preloading-tricks/commit/f6ba3d54cb333afe456badd55f5e3c7d87ad9ba4) - **forge**: split the plugin layer to add the virtual mod container *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`f3edc4d`](https://github.com/SettingDust/preloading-tricks/commit/f3edc4d465f73e374443e9c655a217678b6c0281) - **neoforge/modlauncher**: add virtual mod container *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`aa2cedc`](https://github.com/SettingDust/preloading-tricks/commit/aa2cedc01166854ba1fa100541178bcac305e1d3) - remove unused plugin module & make the neoforge fancy mod loader virtual mod work *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`db6b5b5`](https://github.com/SettingDust/preloading-tricks/commit/db6b5b50369a37b14a3eb6dbaaf584f7ae72cf7f) - move the neoforge service into modlauncher *(commit by [@SettingDust](https://github.com/SettingDust))*


## [2.3.0] - 2025-10-29
### :sparkles: New Features
- [`a217a55`](https://github.com/SettingDust/preloading-tricks/commit/a217a550c15bf69488fa09c2b87e04874b3470bc) - add virtual mod api & add preloading tricks itself *(commit by [@SettingDust](https://github.com/SettingDust))*

### :bug: Bug Fixes
- [`d707d15`](https://github.com/SettingDust/preloading-tricks/commit/d707d1549d37c71ce31722f5e2595bfdf9730c7b) - actually work on 1.21 neoforge *(commit by [@SettingDust](https://github.com/SettingDust))*


## [2.2.2] - 2025-10-28
### :bug: Bug Fixes
- [`0e6b900`](https://github.com/SettingDust/preloading-tricks/commit/0e6b9004dd0a1405fd3b33ed070257e61333c700) - actually work on 1.20 forge and 1.21 neoforge *(commit by [@SettingDust](https://github.com/SettingDust))*


## [2.2.1] - 2025-10-26
### :bug: Bug Fixes
- [`552c8f4`](https://github.com/SettingDust/preloading-tricks/commit/552c8f429091b45d5eae1b13ca92a3b786a238de) - remove access widener *(commit by [@SettingDust](https://github.com/SettingDust))*


## [2.2.0] - 2025-10-26
### :sparkles: New Features
- [`7fbe4cd`](https://github.com/SettingDust/preloading-tricks/commit/7fbe4cde06a4eb9d8eef1520bc2c609bc2f0dd7c) - add manifest attribute to identify mods shouldn't load on current forge variant *(commit by [@SettingDust](https://github.com/SettingDust))*


## [2.1.0] - 2025-10-26
### :sparkles: New Features
- [`31678c2`](https://github.com/SettingDust/preloading-tricks/commit/31678c2797292b31e7ad86f988a0a8da45c4eac6) - prepare for add support to latest NeoForge *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`16189c8`](https://github.com/SettingDust/preloading-tricks/commit/16189c8d2020d832077a43f912e4ea5d8b4a975c) - implement mod manager for forge like modlauncher *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`d878268`](https://github.com/SettingDust/preloading-tricks/commit/d878268cee1f966b74f9af4086d31d2680b149fd) - add mod manager and hook for Fancy mod loader *(commit by [@SettingDust](https://github.com/SettingDust))*

### :bug: Bug Fixes
- [`256fad0`](https://github.com/SettingDust/preloading-tricks/commit/256fad06548460ac731652b1b36f4846380449d3) - remove params for ClassTransformLaunchPlugin *(commit by [@SettingDust](https://github.com/SettingDust))*


## [2.0.1] - 2025-10-25
### :sparkles: New Features
- [`9b3e430`](https://github.com/SettingDust/preloading-tricks/commit/9b3e4303b767b744f048d0f76e9c2d55d33c8a32) - correct the jar bundle *(commit by [@SettingDust](https://github.com/SettingDust))*

### :wrench: Chores
- [`5d73337`](https://github.com/SettingDust/preloading-tricks/commit/5d73337c55a57a564b402b03c923901655bebf01) - remove unused files *(commit by [@SettingDust](https://github.com/SettingDust))*


## [2.0.0] - 2025-10-25
### :sparkles: New Features
- [`1750a79`](https://github.com/SettingDust/preloading-tricks/commit/1750a79d722e312686be25c613495c8af0b133d0) - bootstrap ClassTransform on lexforge *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`19f0157`](https://github.com/SettingDust/preloading-tricks/commit/19f0157e417700a62b88f44a2a635441e41f320b) - implement on lexforge *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`45a06f2`](https://github.com/SettingDust/preloading-tricks/commit/45a06f26e5dc47e0acb8cb9d7b5b486826f28cbc) - implement on neoforge *(commit by [@SettingDust](https://github.com/SettingDust))*

### :bug: Bug Fixes
- [`c21e7fc`](https://github.com/SettingDust/preloading-tricks/commit/c21e7fc3fbaff0a79d3df24c3da2b11a7bbebf11) - can't inject new interface and method for loaded class *(commit by [@SettingDust](https://github.com/SettingDust))*

### :recycle: Refactors
- [`98bca78`](https://github.com/SettingDust/preloading-tricks/commit/98bca7816a49178c64a6894f0948bb1d0918cea6) - copier from template *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`58bc5a2`](https://github.com/SettingDust/preloading-tricks/commit/58bc5a22b395f6f83d47eddc48e7741102cf4e33) - rewrite the api & add fabric implementation back *(commit by [@SettingDust](https://github.com/SettingDust))*


## [1.2.3] - 2024-10-11
### :bug: Bug Fixes
- [`c3d4bbb`](https://github.com/SettingDust/preloading-tricks/commit/c3d4bbbb46001a2955169267f122bdcb9f34cc1e) - service loader load services as expected now *(commit by [@SettingDust](https://github.com/SettingDust))*


## [1.2.2] - 2024-10-07
### :bug: Bug Fixes
- [`c71a4e7`](https://github.com/SettingDust/preloading-tricks/commit/c71a4e7717cc4b9736f4a6320c87b124b5924ec3) - merge the metadata.json & fix the language provider name *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`589c07b`](https://github.com/SettingDust/preloading-tricks/commit/589c07b1b85734294f2ee6739381076c4c29d492) - the service loader crash when the last service failed to load *(commit by [@SettingDust](https://github.com/SettingDust))*

### :wrench: Chores
- [`4610e27`](https://github.com/SettingDust/preloading-tricks/commit/4610e279862c1d77eca98cc44b9b25b6a07a62dc) - remove unused catalog *(commit by [@SettingDust](https://github.com/SettingDust))*


## [1.2.1] - 2024-09-16
### :bug: Bug Fixes
- [`58356dc`](https://github.com/SettingDust/preloading-tricks/commit/58356dc40a64b31ab38b251db156a4486fd434bd) - shadow the services *(commit by [@SettingDust](https://github.com/SettingDust))*


## [1.2.0] - 2024-09-16
### :sparkles: New Features
- [`185cfb7`](https://github.com/SettingDust/preloading-tricks/commit/185cfb78dcc829ee563ab4c8980f4db858f47d14) - add back the forge and use java 17 for developing *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`a99cb0c`](https://github.com/SettingDust/preloading-tricks/commit/a99cb0cc72d0d30bc389b407bc7e32d8856898c0) - support fabric loader 0.16+ *(commit by [@SettingDust](https://github.com/SettingDust))*

### :bug: Bug Fixes
- [`da11f3b`](https://github.com/SettingDust/preloading-tricks/commit/da11f3b521d43987b4e6dd964853461a098f6a42) - make project buildable *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`347f5e2`](https://github.com/SettingDust/preloading-tricks/commit/347f5e284293f5420e4e382d5c8ef356c04b9934) - rename forges' modules to shadow both correctly *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`3fde004`](https://github.com/SettingDust/preloading-tricks/commit/3fde00402870c3b15cf255ee125db06ba158e75e) - commit the neoforge modules *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`b1edb1e`](https://github.com/SettingDust/preloading-tricks/commit/b1edb1e942d56cf58e894d2f41925547e1a7ed40) - avoid include wrong dependencies from fml projects *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`23e31fc`](https://github.com/SettingDust/preloading-tricks/commit/23e31fc4c17bf29627c9d2df4683a2aa40ce1c2e) - accept library that is jvm 21 *(commit by [@SettingDust](https://github.com/SettingDust))*

### :recycle: Refactors
- [`a182df8`](https://github.com/SettingDust/preloading-tricks/commit/a182df879a59c7109390c4d7c974fdf3ca6ea66a) - use ModDevGradle for neoforge module *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`57f2848`](https://github.com/SettingDust/preloading-tricks/commit/57f284886a2365a5be758f6293755ec98414b7ae) - use ModDevGradle for the other neoforge modules *(commit by [@SettingDust](https://github.com/SettingDust))*

### :wrench: Chores
- [`b9c0062`](https://github.com/SettingDust/preloading-tricks/commit/b9c00624141327ab92ef7c63d56443fdf80a5f2f) - cleanup code *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`4969754`](https://github.com/SettingDust/preloading-tricks/commit/4969754a224626b64eda18f135374b2e2c4cc6e9) - update fabric loom to 1.7 & gradle to 8.8 *(commit by [@SettingDust](https://github.com/SettingDust))*


## [1.1.0] - 2024-06-17
### :sparkles: New Features
- [`8b137ec`](https://github.com/SettingDust/preloading-tricks/commit/8b137ec3edf702505817d141635a0b7297f9c446) - target java 21 & use semver *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`aae69ee`](https://github.com/SettingDust/preloading-tricks/commit/aae69eef2d5513fb05dfe660707fa8c1726da02e) - **fabric**: use fabric loom for language adapter module *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`17d94e7`](https://github.com/SettingDust/preloading-tricks/commit/17d94e759f523cfefe8b98e7de992842e2214c58) - **fabric**: use fabric loom for fabric loader *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`cb8f841`](https://github.com/SettingDust/preloading-tricks/commit/cb8f841aa4c8f6b44ce8520676957e1f1cc71f79) - **fabric**: support quilt loader *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`25fafaf`](https://github.com/SettingDust/preloading-tricks/commit/25fafafd26aae8f71a8fa13edded7d910e5b44c1) - **neoforge**: port forge api and language loader to neoforge *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`10c9a2c`](https://github.com/SettingDust/preloading-tricks/commit/10c9a2c4e06a8733fd4e92f62c3de6abb148f3de) - complete the neoforge port *(commit by [@SettingDust](https://github.com/SettingDust))*

### :bug: Bug Fixes
- [`b63bd48`](https://github.com/SettingDust/preloading-tricks/commit/b63bd488496febf9c5e0381622e497bbc40a6142) - make jarJar task use shadowJar output *(commit by [@SettingDust](https://github.com/SettingDust))*

### :recycle: Refactors
- [`e27cfb5`](https://github.com/SettingDust/preloading-tricks/commit/e27cfb50f184ef4cc23928d181208a3c6831137d) - rename callbacks module to services *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`57e9094`](https://github.com/SettingDust/preloading-tricks/commit/57e909478350049bfada0d844f6136dabfef93e1) - rename forge package to neoforge *(commit by [@SettingDust](https://github.com/SettingDust))*

### :wrench: Chores
- [`fa9ec04`](https://github.com/SettingDust/preloading-tricks/commit/fa9ec040e756517004e255feccce6a12a21b135c) - bump gradle to 8.7 *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`c3eb4ca`](https://github.com/SettingDust/preloading-tricks/commit/c3eb4ca11fd34c65cf7ab82b230bdb40de99efd4) - cleanup code *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`91b9c23`](https://github.com/SettingDust/preloading-tricks/commit/91b9c235f55416062738450675b4cd64f01f7a0b) - change the version and archive name to work with modrinth maven *(commit by [@SettingDust](https://github.com/SettingDust))*

[1.1.0]: https://github.com/SettingDust/preloading-tricks/compare/1.0.6...1.1.0
[1.2.0]: https://github.com/SettingDust/preloading-tricks/compare/1.1.0...1.2.0
[1.2.1]: https://github.com/SettingDust/preloading-tricks/compare/1.2.0...1.2.1
[1.2.2]: https://github.com/SettingDust/preloading-tricks/compare/1.2.1...1.2.2
[1.2.3]: https://github.com/SettingDust/preloading-tricks/compare/1.2.2...1.2.3
[2.0.0]: https://github.com/SettingDust/preloading-tricks/compare/1.2.3...2.0.0
[2.0.1]: https://github.com/SettingDust/preloading-tricks/compare/2.0.0...2.0.1
[2.1.0]: https://github.com/SettingDust/preloading-tricks/compare/2.0.1...2.1.0
[2.2.0]: https://github.com/SettingDust/preloading-tricks/compare/2.1.0...2.2.0
[2.2.1]: https://github.com/SettingDust/preloading-tricks/compare/2.2.0...2.2.1
[2.2.2]: https://github.com/SettingDust/preloading-tricks/compare/2.2.1...2.2.2
[2.3.0]: https://github.com/SettingDust/preloading-tricks/compare/2.2.2...2.3.0
[2.4.0]: https://github.com/SettingDust/preloading-tricks/compare/2.3.0...2.4.0
[2.4.1]: https://github.com/SettingDust/preloading-tricks/compare/2.4.0...2.4.1
[2.5.0]: https://github.com/SettingDust/preloading-tricks/compare/2.4.1...2.5.0
[2.5.1]: https://github.com/SettingDust/preloading-tricks/compare/2.5.0...2.5.1
