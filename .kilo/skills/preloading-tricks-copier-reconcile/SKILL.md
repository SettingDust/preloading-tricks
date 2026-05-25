---
name: preloading-tricks-copier-reconcile
description: "Reconcile Copier template updates for Preloading Tricks with Option A: preserve repository topology and absorb only template changes that do not break custom multi-loader build behavior."
---

## Repo Context

Preloading Tricks is a multi-loader Minecraft mod/library using Gradle Kotlin DSL, cloche targets, custom `buildSrc` conventions, and a source layout split across `src/api`, `src/core`, `src/shared/*`, and `src/platform/*`.

## Use This Skill When

- Updating this repository from `SettingDust/cloche-template`.
- Reviewing Copier-generated changes that touch build logic, topology, metadata, or platform wiring.
- Reconciling upstream template churn without flattening repository-specific structure.

## Core Principle: Option A

Preserve repo topology first; selectively absorb template improvements second.

- The repository is not required to mirror current template structure.
- Accept template changes only when they fit existing topology and packaging rules.
- Reject or adapt template output that collapses source sets, containers, loader separation, or custom runtime wiring.

## High-Risk Files

- `build.gradle.kts`
- `settings.gradle.kts`
- `buildSrc/build.gradle.kts`
- `buildSrc/src/main/kotlin/**`
- `.github/workflows/**`
- `gradle.properties`
- `src/**/resources/**` metadata and service descriptors
- `.copier-answers.yml`

## Protected Topology / Build Rules

- Preserve cloche target names and source-set structure.
- Preserve `src/api`, `src/core`, `src/shared/*`, and `src/platform/*` separation.
- Preserve loader-family neutrality in shared code unless the change explicitly targets one loader.
- Preserve final jar/resource transformer behavior, including class duplicate filtering rules.
- Do not let template defaults flatten container/version/main target distinctions.

## Build-Specific Reconciliation Checks

- Keep `clocheTemplate { remappedDevVariants.set(false) }` unless there is an explicitly reviewed reason to change it.
- Preserve Forge-like `bootClasspath` registration and resource embedding under `libs/boot`.
- Do not introduce self-dependencies such as targets consuming their own produced variant in a way that creates recursive or duplicated runtime wiring.
- Preserve container wiring and embedded target relationships.
- Re-check attribute-sensitive dependency blocks, especially remapped vs non-remapped paths.

## Loader Separation Rules

- Keep Fabric wiring separate from Forge ModLauncher.
- Keep NeoForge ModLauncher separate from NeoForge Fancy Mod Loader wiring.
- Do not merge Forge-like shared logic with loader-specific metadata/resources unless already proven equivalent.
- Keep SPI, manifest, mixin, and metadata resources aligned with current package/layout conventions.

## What to Preserve During Reconciliation

- Existing package naming and resource paths.
- Multi-version target distinctions.
- Boot-layer/runtime classpath behavior.
- Publication/packaging behavior from current `buildSrc` conventions.
- Repo-owned fixes previously restored after Copier churn, especially topology restoration.

## Verification Focus

- Prioritize focused Gradle verification over syntax-only review.
- Confirm build logic still respects source-set boundaries and target graph shape.
- Check that bootClasspath artifacts are still packaged where expected.
- Check that `remappedDevVariants` behavior did not silently change.
- Confirm no loader cross-wiring was introduced.
- Re-read changed workflow/publishing files for unintended template defaults.

## Handoff

Report:

- Which template changes were accepted unchanged.
- Which were adapted to fit Option A.
- Which were rejected to protect topology/build behavior.
- Which files require manual review because they affect packaging, runtime classpath, or loader separation.
