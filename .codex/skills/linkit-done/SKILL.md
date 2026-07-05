---
name: linkit-done
description: Finalize LinkIt KMP work. Use when the user says work is done, asks to finalize current changes, simplify/refine staged and unstaged changes, create a final commit, or mark matching SCC Feature UIDs done through docs/specs/scc-action.mjs.
---

# LinkIt Done

Use this skill when the user asks to finish current LinkIt KMP work.

## Workflow

1. Inspect all current changes:
   - `git status`
   - `git diff`
   - `git diff --staged`
2. Simplify changed code where appropriate:
   - Improve reuse, quality, readability, and efficiency.
   - Keep edits scoped to the user's work.
   - Preserve unrelated user changes.
3. Check changed text files for missing EOF newlines and add them where needed.
4. Run relevant formatting/tests when practical.
5. If the work maps to one or more SCC Feature UIDs, update tracking through `node docs/specs/scc-action.mjs done --feature <uid> --agent codex` and include test/screenshot status when known. If the UID is ambiguous, ask or report that SCC tracking was not updated.
6. Run `node docs/specs/scc-action.mjs audit` when SCC tracking changed.
7. Summarize the final changes.
8. Create a Korean commit message focused on why the change was made.
9. Ask before staging/committing unless the user explicitly requested the commit.
10. Commit using the repository convention:
    - `feat: ...`
    - `fix: ...`
    - `refactor: ...`
    - `chore: ...`
    - `docs: ...`
    - `style: ...`
    - `test: ...`

## Rules

- Do not revert unrelated changes.
- Do not use destructive git commands unless the user explicitly asks for them.
- Prefer a concise Korean commit message.
