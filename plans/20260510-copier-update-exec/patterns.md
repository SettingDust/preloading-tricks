# Local Learnings - 20260510-copier-update-exec

Updated: 2026-05-10

- Copier noop classification rule: treat this execution as `noop_success` when verification evidence shows no file deltas and the run artifact reports noop completion, even if copier exits with code `1` on template-current paths.
  - Evidence: `verification.md` (V2, V3, V4) and `artifacts/execution/wave2-result.yaml`.

- Canonical artifact root for this execution: `plans/20260510-copier-update-exec/artifacts`.
  - Evidence: `verification.md` scope field `Evidence source root` and runbook artifact map.

- Wave4 machine-readable closure contract: `artifacts/wave4-result.yaml` is the aggregate machine-readable result and must carry traceability marker `T7-wave4` with `source_wave: wave4`.
  - Evidence: `runbook.md` task traceability section and `artifacts/wave4-result.yaml` traceability block.
