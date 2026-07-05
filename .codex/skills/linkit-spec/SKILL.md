---
name: linkit-spec
description: LinkIt KMP repository Spec Command Center(SCC), Feature tracking, state transitions, developer assignment, spec regeneration sync, and SCC audit. Use when handling SCC feature status, Work Plan requests, Start/Done/Review/Verify actions, or any docs/specs tracking workflow. Never edit tracking JSON/JS files directly; use docs/specs/scc-action.mjs.
---

# LinkIt Spec

Use this skill for LinkIt KMP Spec Command Center(SCC) feature status lookup, updates, audit, and sync.

## Required References

Read these repository files as needed:

- `docs/specs/spec-command-center-policy.md`
- `docs/specs/scc-action-bridge.md`
- `docs/specs/feature-list.schema.md`

## Rules

- Do not directly edit `docs/specs/feature-tracking.json`, `docs/specs/feature-tracking.js`, or `docs/specs/feature-events.jsonl`.
- Treat `docs/specs/feature-list.json` and `docs/specs/feature-list.js` as generated files.
- Make every SCC tracking change through `node docs/specs/scc-action.mjs ...`.
- Preserve feature state by `feature.uid`, such as `main:MAP_PAN`.
- After a tracking change, summarize the changed Feature UID, previous state, next state, and audit result in Korean.
- When writing or updating a Work Plan, check the current code implementation level too. Distinguish implemented skeletons, placeholders, unconnected entry points, and missing SDK/dependencies.
- For an SCC `Start` request, do not immediately implement. First list required work, impact scope, test/screenshot checks, and confirmation questions. Do not edit code, commit, run `scc-action.mjs start`, or change tracking until the user approves.
- SCC buttons copy skill invocation prompts instead of terminal commands. After user approval, run the matching `scc-action.mjs` command.

## Commands

```sh
node docs/specs/scc-action.mjs sync
node docs/specs/scc-action.mjs audit
node docs/specs/scc-action.mjs assign --feature <uid> --agent codex --developer <name>
node docs/specs/scc-action.mjs start --feature <uid> --agent codex --developer <name>
node docs/specs/scc-action.mjs review --feature <uid> --agent codex --pr '#123'
node docs/specs/scc-action.mjs done --feature <uid> --agent codex --tests passed --screenshots passed
node docs/specs/scc-action.mjs verify --feature <uid> --agent codex
node docs/specs/scc-action.mjs block --feature <uid> --agent codex --reason '<reason>'
node docs/specs/scc-action.mjs unblock --feature <uid> --agent codex
```

## Workflow

1. Identify the target Feature UID. If unclear, search `docs/specs/feature-list.json`.
2. Run the appropriate `docs/specs/scc-action.mjs` command.
3. Run `node docs/specs/scc-action.mjs audit`.
4. Summarize state transitions and warnings in Korean.

## Related Skills

- `linkit-done` calls the `done` action after implementation is complete.
- `linkit-pr` calls the `review` action after PR creation or linking.
- `linkit-pr-review` calls the `verify` action only when tests and visual verification are sufficient.
