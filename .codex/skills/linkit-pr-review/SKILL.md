---
name: linkit-pr-review
description: Review a GitHub Pull Request for LinkIt KMP. Use when the user asks to review a PR number, inspect PR changes, leave review comments, or verify matching SCC Feature UIDs through docs/specs/scc-action.mjs after sufficient test and screenshot validation.
---

# LinkIt PR Review

Use this skill when the user asks to review a PR for this repository.

## Workflow

1. Gather PR context:
   - `gh pr view <pr> --json title,body,state,files,headRefName,headRefOid,author`
   - `gh pr diff <pr>`
   - Fetch and check out the PR branch when direct file reads are needed.
2. Read surrounding code for changed files to understand existing module patterns.
3. Apply the repository instructions from `AGENTS.md`.
4. If the change touches architecture, navigation, Metro DI, or Compose UI, read the matching docs listed in `AGENTS.md` before judging the change.
5. Review in Codex code-review style:
   - Findings first, ordered by severity.
   - Ground every issue in file and line references.
   - Focus on correctness, regressions, missing tests, architecture violations, and meaningful maintainability risks.
   - Do not comment on trivial formatting/import churn.
6. If the user explicitly asks to post review comments, use `gh api`/`gh pr review` after confirmation as needed.
7. If review/test/screenshot verification establishes that an SCC Feature is complete, update tracking through `node docs/specs/scc-action.mjs verify --feature <uid> --agent codex` only when the Feature UID is known and the verification is actually complete.

## Review Criteria

- Correctness: bugs, edge cases, null safety, error handling.
- Design: responsibilities, coupling, dependency direction, abstraction fit.
- Consistency: module/package conventions, architecture layers, DI, Repository, UseCase, naming.
- Performance: Compose recomposition, repeated calls, memory leaks, Flow/coroutine lifecycles.
- Security: secrets, input validation, injection risks.
- KMP: expect/actual consistency, platform side effects, platform code in common modules.

## Output

Write the review in Korean. Use this order:

1. Findings with severity and file/line.
2. Open questions or assumptions.
3. Brief summary and test gaps.

Severity labels:

- `Must Fix`: bug, security issue, data loss, crash risk.
- `Should Fix`: design, performance, or structure problem.
- `Suggestion`: readability or convention improvement.
- `Good`: notable strong implementation choice.
