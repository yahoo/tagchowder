---
name: review-change
description: Review a branch or working-tree change in tagchowder before it becomes a PR. Checks the diff against the repo's AGENTS.md and .claude/rules (never throwing on bad markup, balanced SAX events, no external fetches, hot-path cost, feature and signature compatibility, definition and test conventions, release secrets), runs the Maven gate, and reports BLOCKER/WARNING findings with file:line. Use when asked to review changes, check a branch before a PR, or self-review work just finished.
---

# Review a tagchowder change

Read-only. Don't edit files, stage or commit while reviewing. Report the findings, and fix only when asked.

## 1. Collect the change

```bash
git fetch https://github.com/yahoo/tagchowder.git master:refs/remotes/upstream-master   # or use an existing upstream remote
git diff --stat upstream-master...HEAD
git diff upstream-master          # committed + uncommitted changes as one patch
git status --short                # untracked files: ask which ones are part of the change before reading them
```

Read every changed file in full, plus `AGENTS.md` and the `.claude/rules/` page for each area the change touches.

## 2. Check, in this order

**Parser behavior and safety: BLOCKER if violated**
- No new code path throws on malformed input. Look for unchecked array indexes into the scanner buffer, `substring` on
  attacker-controlled offsets, `Integer.parseInt` without a catch, and `Error` / `RuntimeException` reachable from a `ScanHandler`
  event.
- No new `throw` of `RuntimeException`, any of its subclasses, or `Error`, in main or test code. Failures are `SAXException`
  (or its `SAXNot*` subclasses) or `IOException`; unchecked exceptions from JDK calls are caught at the call.
- SAX events stay balanced: element changes go through `rectify` / `push` / `pop` / `restartablyPop`, and every
  `startPrefixMapping` has its `endPrefixMapping`.
- Nothing fetches a URL, file or entity during a parse. The `EntityResolver` result stays unused.
- `ScanHandler` implementations copy from `buff` before returning and never keep the array.
- A new feature is wired in all places: constant, `DEFAULT_*`, field, `theFeatures`, `setFeature`. New behavior is off by
  default unless it fixes clearly wrong output.
- No public signature, feature name or property name removed or changed incompatibly.
- No Java 9+ class or method in main code or templates. No new runtime dependency.
- `AttributesImpl` changes keep `qNameIndex` pointing at the lowest index for each qName.
- Definition changes: at most 29 groups, every new STML action has a `case` in `HTMLScanner`, and generated files are untouched.

**Release and secrets: BLOCKER**
- `cd/mvnsettings.xml` still reads `${env.OSSRH_USER}` / `${env.OSSRH_TOKEN}`, with no literal credential anywhere in the diff.
- No `target/`, IDE files, `cd/deploy/`, key material or scratch files.
- A version change, if any, is consistent across `pom.xml`, `core/pom.xml` and `README.md`, and was asked for.

**Design and style: WARNING unless the build fails**
- `AGENTS.md` "Design rules" and "Comments": hot path cheap (no per-character allocation, regex, logging or boxing in
  `HTMLScanner` or the `ScanHandler` callbacks), browser semantics, the fix lives in `html.tssl` when it can, no mutable static state,
  no lambdas or streams, an enum rather than a `boolean` for a new option on a public method, no throwaway temporaries, no
  narration or history comments, no new commented-out code.
- Templates under `core/templates/` follow the house style by hand, since checkstyle never sees them.
- Logging uses `{}` placeholders, or is guarded when it builds its message.
- Nothing internal or private in code, comments or the commit message (public repository).

**Tests: BLOCKER if behavior is untested**
- Each behavior change has a test that fails without it and asserts on the output, not just that `parse` returned.
- Malformed, non-ASCII and reference-edge cases from `.claude/rules/testing.md` are covered where the change touches them.
- Tests are scoped to the change: unit tests in the touched class's `*Test`, with no tests added for unchanged code and no
  whole-stack tests where a `Parser` test would do. WARNING if not.
- `.claude/rules/testing.md`: tests are independent, assert only in `@Test` methods, use instance helpers (no static), start no
  threads, and don't depend on the platform charset.

**Hygiene**
- Commit subject is imperative and ≤ 72 chars, and the body says why. One logical change.
- `CHANGES` / `README.md` updated when users see different output or a new option.

## 3. Run the gate

JDK 17 (what CI uses):

```bash
mvn clean install -Dgpg.skip=true
```

Report the test count and pass/fail, and the checkstyle count from "There are N errors reported by Checkstyle". More than 45 fails
the build, and a count above the base branch's means the change added violations even when the build passes. If the build cannot
run, say so explicitly rather than skipping silently.

## 4. Report

```
## Review: <branch>

### Findings
- BLOCKER: <what is wrong and why it matters> (path/File.java:line)
- WARNING: <...> (path/File.java:line)

### Build
- Tests run: N, Failures: N. Checkstyle: N errors (base: 45). SpotBugs / PMD: pass | fail (<first error>)

### Verdict
READY FOR PR | NEEDS WORK (N blockers, N warnings)
```

Flag only real violations, each with a cited rule and a file:line. Don't pad the report with style nits that the build already enforces.
