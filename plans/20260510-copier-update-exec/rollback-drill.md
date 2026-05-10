# Rollback Drill Report

- Plan ID: 20260510-copier-update-exec
- Task ID: T6-rollback-drill
- Executed At: 2026-05-10T08:41:16+08:00
- Scope: Non-destructive rollback drill in isolated lane only

## Objective
Validate rollback anchor usability without modifying primary workspace or persistent execution lane state.

## Isolation And Safety Guardrails
- Drill ran inside isolated worktree: `.tmp-copier-lane/20260510-copier-update-exec`
- Primary workspace branch/head remained untouched: `main` @ `760131e5ce150fa6419b4246f8dd48449f9fc31e`
- No remote operations (no push/fetch)
- Temporary drill branch was deleted after validation

## Anchor Verification
- Rollback tag ref: `refs/tags/rollback-anchor/20260510-copier-update-exec`
- Rollback branch ref: `refs/heads/rollback/20260510-copier-update-exec`
- Tag object SHA: `bc43fdb1cb4147c1b2275ef573e4087eeb51287f` (annotated tag object)
- Peeled tag commit (`^{} `): `760131e5ce150fa6419b4246f8dd48449f9fc31e`
- Rollback branch commit: `760131e5ce150fa6419b4246f8dd48449f9fc31e`
- Tag commit equals rollback branch commit: `true`

## Drill Procedure
1. Confirm isolated lane is clean before drill.
2. Create temporary branch `drill/T6-rollback-drill-20260510084116` from lane branch.
3. Create an empty marker commit to force divergence from rollback anchor.
4. Execute rollback simulation with `git reset --hard <rollback-anchor-commit>` on temporary branch.
5. Verify HEAD equals peeled rollback anchor commit.
6. Checkout original lane branch and delete temporary drill branch.
7. Confirm isolated lane is clean after drill.

## Drill Evidence
- Lane branch before drill: `lane/20260510-copier-update-exec`
- Lane head before drill: `396e7e3b611d414348e56387a3755d785f9b083c`
- Temp head before rollback: `a3ec4254b0892262a1db1f2490410e82e5b5cca0`
- Diverged before rollback: `true`
- Head after rollback on temp branch: `760131e5ce150fa6419b4246f8dd48449f9fc31e`
- Rollback matched peeled anchor commit: `true`
- Lane branch restored after drill: `lane/20260510-copier-update-exec`
- Lane head restored after drill: `396e7e3b611d414348e56387a3755d785f9b083c`
- Lane clean before drill: `true`
- Lane clean after drill: `true`
- Primary workspace remained dirty as expected baseline: `true`

## Result
Rollback drill completed successfully in isolated lane. Rollback anchor is valid and can restore a diverged branch head to the expected anchor commit in a non-destructive local workflow.

## Notes
The first comparison attempt used the annotated tag object SHA instead of peeled commit SHA, which can cause false mismatch. Final verification uses `refs/tags/<tag>^{}` for commit-level comparison.
