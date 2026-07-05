---
name: linkit-pr
description: Draft, prepare, push, or create a Pull Request for the current LinkIt KMP branch. Use when the user asks for PR creation/drafting and update SCC review state through docs/specs/scc-action.mjs when matching Feature UIDs are known.
---

# LinkIt PR

Use this skill when the user asks to create or draft a PR.

## Workflow

1. Gather branch and diff context:
   - `git status`
   - `git branch --show-current`
   - Determine base branch: `develop` targets `main`; work branches target `develop`.
   - `git log <base>..HEAD --oneline`
   - `git diff <base>...HEAD --stat`
   - `git diff <base>...HEAD`
2. Read `.github/PULL_REQUEST_TEMPLATE.md`.
3. Infer the issue number from the branch name or commits. If not found, ask whether there is one.
4. Draft a Korean PR body from the template:
   - Issue Number: use `closes #N` when known.
   - Changes: summarize meaningful changes, using As-Is / To-Be when helpful.
   - Review Point: call out what reviewers should focus on.
   - Architecture / Flow: include Mermaid only when it helps explain module, navigation, async, or state flow changes.
   - Test Plan: describe executed or recommended tests.
   - Screenshot: include only for UI changes.
   - References: include only when useful.
5. Show the draft and ask for confirmation before pushing or creating the PR.
6. If approved:
   - Warn about uncommitted changes and ask whether to commit them.
   - Push with `git push -u origin <branch>`.
   - Create the PR with `gh pr create --title "<title>" --body "<body>" --base <base>`.
7. If the PR maps to one or more SCC Feature UIDs, update tracking through `node docs/specs/scc-action.mjs review --feature <uid> --agent codex --pr <pr-number-or-url>`. Run `node docs/specs/scc-action.mjs audit` after updates.
8. If the user asks for review comments after PR creation, add only useful comments for non-obvious code decisions or important side effects.

## Rules

- PR title must be Korean and 70 characters or less.
- Remove optional template sections that do not apply.
- Use at most two Mermaid diagrams.
- Do not push or create a PR before user approval unless the user explicitly requested the full action.
