---
name: challenge
description: >
  Adversarially stress-test changes in this repository — development plans,
  executions/code changes, documentation, and other decisions — to make them
  stronger by attacking them honestly before a reviewer, a user, or production
  does. Operates in four modes selected from context: (A) red-team a plan or a
  specific design decision, (B) play devil's advocate against an execution /
  code-change approach, (C) adversarial review of a document (CLAUDE.md, README,
  asciidoc, a skill), (D) challenge an arbitrary change or claim. This is a
  critique skill, not an editing skill: it finds holes and rates them; it never
  rewrites the target to "fix" them. Invoke for "/challenge", "red-team this
  plan", "poke holes in this change", "play devil's advocate", "adversarial
  review", "what's wrong with this", "steelman the opposite", "reviewer-2 this".
user-invocable: true
allowed-tools: Bash Read Grep Glob WebFetch AskUserQuestion
---

# Challenge Skill

Adversarially stress-tests the artifacts this repo produces. Its job is to make a
change *stronger* by attacking it honestly — surfacing the objections a sharp,
skeptical reviewer (or a failing build, or an unhappy plugin user) would raise,
before they do.

This is a **critique** skill, not an editing skill. It never rewrites the target
to "fix" it; it identifies weaknesses, rates them, and stops. The user decides
what to act on. For fixing bugs use `/code-review`; for quality cleanups use
`/simplify`; for updating a plan use `/plan update`. This skill only challenges.

---

## Operating Principles

1. **Attack the work, not the author.** Rigorous and direct, never sycophantic,
   never personal. "This step has no rollback" — not "you forgot rollback."
2. **Steelman before you strike.** State the strongest version of the plan /
   change / doc first, then challenge *that*. Demolishing a weak reading is
   worthless.
3. **Be specific and falsifiable.** Every objection names what would resolve it:
   a missing test, an unhandled input, a broken architectural rule, a
   counterexample. "This seems risky" is not a finding; "Step 2.1 removes the
   sequential chain but no test asserts two independent steps lack an ordering
   edge, so a regression here is invisible" is.
4. **Respect this repo's standards** (`CLAUDE.md`): hexagonal architecture
   (ports hide technology, single-`apply` use cases, `infra/gradle` `compileOnly`
   for new modules, Gradle Workers API for parallelism), coverage targets, the
   plan status lifecycle, commit conventions. A change that violates these is a
   finding regardless of whether it "works."
5. **Ground every claim in the actual artifact.** Read the plan/diff/doc first;
   quote `file:line`. Never attack from memory or from the description alone, and
   never invent a counterexample — if you assert one, either point at real code
   or label it `hypothetical`.
6. **Calibrate severity.** Not every objection is fatal. Rate each `high` /
   `medium` / `low` so the user can triage.
7. **End constructively.** Close with the single highest-leverage fix.

---

## Mode Selection

Pick the mode from the user's phrasing and the target. If genuinely ambiguous,
ask (via `AskUserQuestion`); otherwise infer and state which mode you chose in one
line before proceeding.

| Mode | Target | Trigger phrasing | Output |
|------|--------|------------------|--------|
| **A — Red-Team a Plan** | A `plans/PLAN-nnnn` plan, or one design decision / phase within it | "red-team this plan", "poke holes in PLAN-0001", "is this plan sound", "challenge this decision" | Structured counter-analysis |
| **B — Devil's Advocate (Execution)** | A code change / diff / PR / an approach about to be implemented | "should I build it this way", "play devil's advocate on this change", "argue against this refactor", "talk me out of this" | The case against + steelman of the alternative |
| **C — Adversarial Review (Docs)** | A document: `CLAUDE.md`, `README.md`, `doc/*.adoc`, a skill, a meta entry | "adversarial review", "reviewer-2 this doc", "tear this README apart" | Severity-rated reviewer report + verdict |
| **D — Challenge a Change/Claim** | Anything else: a commit, a config change, an assertion made in conversation | "challenge this", "what's wrong with this", "is this actually true" | Mode-A-style counter-analysis, scoped to the change |

Default when only a target is given with no clear verb: a whole document → C; a
plan → A; a diff/PR/approach → B; a discrete claim → D.

---

## Identifying the Target

Before critiquing, pin down exactly what is under attack, and **read it**:

1. **A plan** — read the `plans/PLAN-nnnn_*.md` file in full (Section 5 for steps,
   Section 4 for open questions/decisions, Section 7 for risks).
2. **An execution / code change** — inspect the real diff. Use
   `git diff`, `git diff <base>...HEAD`, `git show <sha>`, or `gh pr diff <n>`.
   Read the changed files around the hunks, not just the hunks.
3. **A document** — read the entire file (a reviewer who skimmed hasn't earned the
   critique).
4. **The current context** — if a plan or change is active in the session, the
   most recently discussed one.

If you cannot locate the target, ask for it. Do not invent something to attack.

---

## Mode A — Red-Team a Plan

Stress-test a plan (or one decision/phase in it). Produce:

```markdown
## Red-Team: <plan id / the decision, in one sentence>

**Steelman.** <The strongest, most charitable reading of the plan/decision.>

### Strongest objections
1. <Objection> — severity: high | medium | low
   <Why it bites; the concrete failure it leads to.>
2. ...

### Gaps & missing steps
- <A step, test, or migration the plan needs and doesn't have.>

### Hidden assumptions
- <Unstated premise the plan depends on; what breaks if it's false.>

### Architecture & repo-standard violations
- <Where the plan conflicts with CLAUDE.md: ports leaking, non-Workers threading,
  missing infra/gradle compileOnly, coverage gaps, lifecycle misuse.>

### Risk & rollback
- <Under-rated risks in Section 7; steps with no safe rollback; ordering hazards.>

### What would settle it
- <The check, spike, or answered open question that would confirm or kill it.>

### Verdict
<sound / sound with caveats / not ready as written / likely to fail>,
plus the single highest-leverage fix.
```

Omit any section that is genuinely empty — do not pad. If the plan is still
`draft` with unresolved questions, call out whether those questions are the real
blockers to acceptance.

---

## Mode B — Devil's Advocate (Execution)

Argue against an implementation approach or an actual code change, to pressure-test
it before it lands (or before more is built on it). Produce:

```markdown
## Devil's Advocate: <the change/approach>

**The change, steelmanned.** <Best case FOR doing it this way, stated fairly.>

### The case against
- <Reason not to do this / not this way> — severity: high | medium | low
- ...

### Failure modes
- <How it breaks in practice: the concrete bad build, wrong artifact, broken
  incremental cache, race under --parallel, ClassNotFoundError at runtime.>

### Correctness & test blind spots
- <Behaviour the diff changes that no test pins; edge cases silently unhandled.>

### What would have to be true
- <Assumptions that must hold for the change to be correct; flag the unverified
  ones and how to verify (delegate to build / test / e2e-test / verify).>

### Steelman of the alternative
<The strongest version of the approach *not* taken — argued as if you preferred it.>

### Net read
<proceed / proceed with these guards / reconsider — and why.>
```

Not contrarian for its own sake: if the approach is sound, say so — but only after
a genuine attempt to break it.

---

## Mode C — Adversarial Review (Docs)

A harsh peer-review pass over a full document. **Read the entire target first.**
Produce:

```markdown
## Adversarial Review: <document title / path>

**Summary of the doc (in my words).** <2–3 sentences proving you read it.>

### Major issues
| # | Issue | Severity | Location |
|---|-------|----------|----------|
| 1 | <Substantive problem> | high | <section / file:line> |

<One paragraph per major issue: the problem and what a revision must do.>

### Minor issues
- <Imprecise wording, stale references, unclear scope, dead links.>

### Accuracy & drift
- <Claims that no longer match the code/build; instructions that would fail if
  run; commands/paths/versions that have drifted; contradictions with CLAUDE.md.>

### Completeness & framing
- <What a reader needs and the doc omits; is it scoped honestly; what's missing.>

### Verdict
**<Accept / Minor revision / Major revision / Reject>** — <justification keyed to
the major issues.>

### Highest-leverage revision
<The single change that would most improve the document.>
```

For docs that contain runnable commands (README, CLAUDE.md build sections),
**verify the commands** where cheap — a documented `./gradlew` task that no longer
exists is a major issue. Delegate any real Gradle run to the `build` skill rather
than running it inline.

---

## Mode D — Challenge a Change/Claim

For anything not covered by A–C — a commit, a config/settings edit, a CI change, or
a bare assertion made in conversation. Use the Mode A structure, scoped to the
change: steelman, strongest objections (with severity), hidden assumptions,
what-would-settle-it, verdict. Keep it proportional to the change's size.

---

## Rules

1. **Read/inspect before you attack.** Always read the plan/diff/doc; never
   critique from the description or memory. For code changes, look at the real
   `git diff` / `gh pr diff`.
2. **No fabricated counter-evidence.** A counterexample is either pointed at real
   code/behaviour or labeled `hypothetical`. Non-negotiable.
3. **Steelman first, every time.** If you can't state the strong version, you're
   not ready to attack it.
4. **Severity on every substantive objection** — high / medium / low.
5. **Ground in repo standards.** Judge against `CLAUDE.md` (architecture,
   coverage, lifecycle, conventions), not just "does it work."
6. **Constructive close.** Always end with the highest-leverage fix.
7. **Critique only — never edit the target.** Surface findings and stop. Point at
   `/code-review`, `/simplify`, or `/plan update` for acting on them; don't apply
   them yourself.
8. **Honest verdicts.** If the work survives the attack, say so plainly — do not
   manufacture objections to seem rigorous.
9. **Read-only.** This skill does not modify files, commit, or run mutating tasks;
   any Gradle verification is delegated read-only to the `build` skill.

---

## Tips

| Situation | Recommendation |
|-----------|----------------|
| User names a plan by number | Read `plans/PLAN-nnnn_*.md` fully, then Mode A |
| "Challenge my current branch" | `git diff origin/develop...HEAD`, read changed files, Mode B |
| The change is actually solid | Say so after a real attempt to break it; don't invent flaws |
| Counterexample feels true but you can't ground it | Label `hypothetical`, frame as "worth verifying via test/e2e-test" |
| Doc critique would require rewriting it | Stop — describe the fix, don't apply it |
| User wants the fix applied | Point them at `/code-review --fix`, `/simplify`, or `/plan update` |
