# Execution Log: {Feature Name}

**Exec ID**: EXEC-{nnnn}
**Plan**: [PLAN-{nnnn}](PLAN-{nnnn}_{slug}.md)
**Issue**: #{issue-number}
**Started**: {YYYY-MM-DD}
**Last Updated**: {YYYY-MM-DD}
**State**: in progress | completed | stopped

## 1. Execution Sessions

<!-- One subsection per execution run (a /execute invocation). Append; never rewrite past sessions. -->

### Session {n} — {YYYY-MM-DD}

- **Scope**: {steps selected, e.g. "all", "Phase 1", "1.1–2.3"}
- **Mode**: {per-step | per-phase | autonomous}
- **Outcome**: {completed | stopped after step N.M | blocked at step N.M}

| Step | Result | Verification | Notes |
|------|--------|--------------|-------|
| {N.M} | completed / skipped / blocked | {what was run and its outcome, e.g. ":flows:test green (139/139)"} | {commit SHA, key observations, or —} |

## 2. Deviations from Plan

<!-- Every departure from the plan as written: different files touched, changed approach,
     extra unplanned work, steps executed out of order or merged, environment/tooling fixes,
     assumptions in the plan that turned out wrong. "None" if the plan was followed exactly. -->

| # | Step | Deviation | Reason | Impact |
|---|------|-----------|--------|--------|
| 1 | {N.M or —} | {what was done differently} | {why} | {consequences for plan, code, or future work} |

## 3. Follow-ups

<!-- Work discovered during execution that is out of scope: candidate issues, future plans, cleanups. "None" if empty. -->

- {follow-up item, with issue/plan link when one exists}
