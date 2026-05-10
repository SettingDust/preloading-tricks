# AGENTS.md

## Copier Execution Conventions

- Artifact root: all active execution artifacts MUST live under `plans/<plan_id>/`.
- Dirty tree isolation: when workspace is dirty, isolate with stash-first policy (including residual cleanup with `stash --all`) before running copier tasks.
- Rollback anchor comparison: compare rollback tag and rollback branch using peeled tag commit `refs/tags/<tag>^{}`; do not compare annotated tag object SHA.
- Copier noop classification: classify as `noop_success` when copier exit code is `1`, changed-file outputs are empty, and transcript indicates template is already current.
- V5 reconciliation gate: V5 is a blocker and passes only when reconciliation artifact exists and `source_of_truth_hash_check.status=pass`.
- Wave4 closure artifact: require machine-readable closure at `plans/<plan_id>/artifacts/wave4-result.yaml` with `traceability.marker` and `traceability.source_wave`.