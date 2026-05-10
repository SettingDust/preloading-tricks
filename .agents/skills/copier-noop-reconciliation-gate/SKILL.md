---
name: copier-noop-reconciliation-gate
description: "Validate copier noop runs with a reconciliation hash gate before declaring execution complete."
metadata:
  version: "1.0"
  confidence: high
  source: task-20260510-copier-update-exec
  usages: 0
---

## When to Apply

Use this pattern when a `copier update` run reports a template-current/no-change path, especially if copier returns a non-zero exit code that may be expected for noop scenarios.

## Steps

1. Confirm noop classification from execution artifact:
   - `classification: noop_success`
   - `status: completed`
   - allow expected context signal such as `copier_exit_code: 1` only with no-change evidence.
2. Prove no file deltas from both changed-file outputs:
   - name-status report is `none`.
   - porcelain report is `none` or empty.
3. Generate explicit high-risk merge decisions for canonical targets (all entries should be `no_merge_required` in noop case).
4. Reconcile decision log against source-of-truth target set:
   - counts match,
   - no missing/extra entries,
   - uniform decision check passes.
5. Enforce source-of-truth hash gate:
   - `source_of_truth_hash_check.status: pass`
   - expected hash equals actual hash.
6. Record final gate decision in verification matrix and keep wave traceability marker for closure (`T7-wave4`).

## Acceptance Criteria

- Blocker checks equivalent to noop + reconciliation gates pass:
  - noop success evidence is present.
  - changed-files evidence is empty in both formats.
  - reconciliation artifact exists and is complete.
  - source-of-truth hash check is `pass` with exact hash match.
- High-risk target reconciliation is exact:
  - canonical target count equals decision entry count.
  - missing and extra entries are both `none`.
  - all high-risk targets resolve to `no_merge_required` for noop path.
- Verification result is `PASS` with zero blocker failures.

## Example

Execution classified as noop (`wave2-result.yaml`) while copier exit code is `1`; reconciliation still passes because changed-files outputs are empty and hash parity is verified in `high-risk-reconciliation.yaml`.

## Common Edge Cases

- Treating copier exit code alone as failure without checking noop evidence.
- Accepting reconciliation artifact presence without enforcing hash parity.
- Allowing partial high-risk decision coverage (count mismatch or missing entries).

## Source Task References

- Plan: `20260510-copier-update-exec`
- T2 (`high-risk-source-of-truth`): `plans/20260510-copier-update-exec/artifacts/preflight/high-risk-source-of-truth.yaml`
- T4 (`merge decision log`): `plans/20260510-copier-update-exec/artifacts/merge/t4-merge-decision-log.yaml`
- T5 (`verification gate`): `plans/20260510-copier-update-exec/verification.md` and `plans/20260510-copier-update-exec/artifacts/merge/high-risk-reconciliation.yaml`
- T7 (`wave4 traceability`): `plans/20260510-copier-update-exec/runbook.md` and `plans/20260510-copier-update-exec/artifacts/wave4-result.yaml`

## References

- `plans/20260510-copier-update-exec/patterns.md`
- `plans/20260510-copier-update-exec/merge-decisions.md`
