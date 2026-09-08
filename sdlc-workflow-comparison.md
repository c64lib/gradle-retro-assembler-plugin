# SDLC Workflow Comparison (reassessed): Three Systems

_Original analysis date: 2026-07-24. Reassessed same day after adding a third system._
_Author: generated with Claude Code, review before reuse. Client business specifics
(Eventory / ENGIE) are deliberately minimised — this document compares methodology, not
client content._

> Scope: comparison of three skill-driven AI-assisted setups as written in their skill /
> instruction / blueprint files. Nothing was executed; conclusions are grounded in
> `SKILL.md` / `copilot-instructions.md` / blueprint contracts.

## Executive Summary

The first version of this document framed **two** systems as opposite points on a single axis.
Adding a **third** system (a knowledge/understanding operating model) shows that framing was
wrong. The correct model is a **three-layer pipeline**, where "understanding" is a distinct
discipline that sits *upstream of* plan-and-execute and feeds it — not a missing feature of it.

| System | Platform | Role | Ships code? |
|---|---|---|---|
| **Engie Copilot Toolkit** | GitHub Copilot / VS Code | Portable, in-repo, story-centric assistant; thin slice of all three layers | No — stops at the commit line |
| **c64lib Gradle plugin harness** | Claude Code | Bespoke, stateful **plan + execute** pipeline for one product | Yes — commit, push, PR, close issue |
| **Eventory-doc** | GitHub Copilot / VS Code | **Knowledge / understanding operating model** (docs-only), theory-grounded, governed | No — emits execution briefs for downstream code repos |

## The Three-Layer Model (corrected framing)

| Layer | Concern | Reference system | Hands downstream |
|---|---|---|---|
| **1. Understanding / knowledge** | Turn scattered facts, meetings, regulations, decisions into a governed, non-contradictory baseline | **eventory-doc** | Implementation companion = bounded execution brief |
| **2. Plan** | Turn a bounded brief/issue into a reviewable plan with an acceptance gate | **c64lib `plan`** (eventory-doc also has its own `pln`/`ipl` for doc work) | Accepted plan |
| **3. Execute** | Turn an accepted plan into shipped code | **c64lib `execute`** | Merged PR |

- eventory-doc explicitly names a **"Plan and Execute"** method as the downstream consumer of
  its companions. The layers are (roughly) consecutive stages, not competitors.
- The **Engie Copilot Toolkit** is not a fourth thing — it is a **thin, portable collapse of a
  slice of all three layers into one in-repo kit** (`analyze-story` ≈ sliver of layers 1–2,
  `implement-story` ≈ layer 3), that stops at the commit line and keeps no durable layer-1
  knowledge system.
- **Plan-and-execute recurs at every layer**: eventory-doc applies it to *documentation/knowledge
  curation*; c64lib applies it to *code*. It is a generic control pattern, not a code-only one.

## What eventory-doc is

- A Markdown-only "single source of truth" repo with a Copilot harness: ~60 skills, 23 process
  records (`proc-NNNN`), 19 templates, guard instruction files, and a meta change log of 121
  entries.
- **Theory-grounded** (its defining trait). The blueprint roots every convention in research:
  Naur (programming as *theory building*), Polanyi (tacit knowledge), Wegner (transactive memory),
  Trist & Cherns (socio-technical joint optimisation). Thesis: *the primary product of software
  development is understanding, and understanding is more fragile than the artefacts it produces.*
- **Anti-hallucination governance** follows from that thesis: explicit canonical sources, the
  observation/decision distinction (append-only facts vs. rationale-bearing commitments), explicit
  `[to be provided by human]` gap markers, "do not invent," and human review gates.
- **Knowledge-element taxonomy**: canonical / derived / meta / operational / AI-configuration
  items, each with its own template, index README, naming convention, and lifecycle status set.
- **Emits the input to plan-and-execute**: `companions/` (`cmp-NNNN`) are "portable execution
  briefs" curated from the canonical baseline so downstream planning/codegen/implementation/review
  start from a bounded, reviewable package.
- Understanding/analysis skills worth noting: `knowledge-capture` (conservatively detect
  task/observation/decision/risk candidates and gate them behind user confirmation),
  `research-workflow` (source ledger → literature analysis → falsifiable hypotheses → validation →
  research-grade output), and the read-only `Query` agent (answers *only* from repo evidence, else
  the exact sentence "No answer was found in existing documents").

## Corrections to the Original Two-System Analysis

- **"c64lib has no understanding/assessment/onboarding skills → gap."** Corrected: this is
  **correct scoping, not a deficiency.** Plan-and-execute *presupposes* understanding already
  exists (an accepted plan, a bounded issue). Layer 1 is where understanding is manufactured and
  governed, and it belongs in a *separate knowledge repo*, not bolted onto a code repo.
- **"Engie is stronger at the understanding half of the SDLC."** Corrected: Engie's
  `assess-technical-debt` / `create-technical-onboarding` / `update-business-docs` /
  `check-behavior` are a **lightweight, in-repo, one-shot** version of what eventory-doc does as a
  **durable, lifecycle-governed, theory-grounded knowledge system.** Different weight class —
  generating a document on demand vs. maintaining a living, anti-drift knowledge system.
- **"c64lib should borrow evidence-discipline + a check-behavior skill from Engie."** Softened:
  for a single OSS plugin this is over-engineering. `CLAUDE.md` + arc42 + the `challenge` skill
  already cover the need at c64lib's scale.

## Shared DNA (grounded observation)

eventory-doc and the c64lib harness are **siblings of the same design lineage** (same maintainer):
both use a **meta/MET change log**, an **adversarial-challenge** skill, a
**draft → accepted → in-progress → implemented** plan lifecycle, **Haiku-subagent delegation** for
date/author acquisition, **session-start triage**, and strict **index/README** discipline.
c64lib's harness is effectively the **code-repo sibling** of eventory-doc's **knowledge-repo
parent**, with layer 1 deliberately amputated as out of scope. The Engie Copilot Toolkit (a
different author) is a convergent but independent design.

## Honest Caveat

"Eventory-doc fills the gap left by plain plan-and-execute" is true **only at the right scale**.
Its ~60 skills / 23 processes / 121 meta changes are justified for a compliance-critical,
multi-year, multi-track, multi-stakeholder program where **knowledge loss is the dominant risk**.
The same machinery on a Gradle plugin would be pure ceremony. The three-layer model is real;
whether you *instantiate* layer 1 as a full governed repo is a function of stakes and stakeholder
count.

## Bottom Line

There is no single "best" workflow. There is a **pipeline**: eventory-doc manufactures and governs
*understanding* (layer 1) and hands bounded briefs to *plan* (layer 2) and *execute* (layer 3),
which c64lib implements for code. The Engie toolkit compresses a portable slice of all three into
one in-repo kit that stops at the commit line. c64lib's deliberate omission of the understanding
layer is a sound scoping decision, not a gap — because that layer is a separate discipline with its
own repository, theory, and lifecycle.
