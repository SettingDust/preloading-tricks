fix: `setAccessible` for `setMod`

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
