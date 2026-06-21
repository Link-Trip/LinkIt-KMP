# Spec Command Center Policy

> Policy version: `scc-policy.v0.1.0`  
> Applies to: `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs`  
> Owner: Product + KMP engineering

## Purpose

Spec Command Center(SCC)는 Pingo 스펙 드리븐 개발의 단일 작업 콘솔이다. SCC의 목표는 Figma 기반 화면 스펙, Feature 목록, 개발 진행 상태, PR/검증 결과를 한 곳에서 추적하고 AI 에이전트가 같은 규칙으로 작업하도록 만드는 것이다.

## Source Of Truth

| Artifact | Source type | Mutable | Rule |
|---|---:|---:|---|
| 화면별 `*-screen.md` | 기획/화면 스펙 원천 | Yes | Figma description, 이미지, sitemap, 리뷰 결과를 반영한다. |
| `feature-list.json` | generated registry | No | 화면별 Markdown에서 생성한다. 직접 수정하지 않는다. |
| `feature-list.js` | generated browser wrapper | No | `feature-list.json`과 항상 같은 내용을 담는다. |
| `feature-tracking.json` | work tracking source | Yes, via tool | 개발자, 상태, Issue, PR, 검증 결과, 작업 계획, 프롬프트 기록을 저장한다. |
| `feature-tracking.js` | generated browser wrapper | No | `feature-tracking.json`과 항상 같은 내용을 담는다. |
| `feature-events.jsonl` | append-only audit log | Append only | 상태 변경, 자동화 실행, 수동 보정 이벤트를 남긴다. |
| `index.html` | SCC UI | Yes | registry + tracking + events를 사람이 보기 쉽게 보여준다. |

## Core Rule

스펙과 작업 추적은 분리한다.

- 스펙 변경은 화면별 Markdown을 수정하고 `feature-list.json`을 재생성한다.
- 작업 상태, 작업 계획, 프롬프트 기록 변경은 `feature-tracking.json`만 수정한다.
- 상태 변경은 반드시 `feature-events.jsonl`에 이벤트를 남긴다.
- `feature-list.json`에 들어 있는 `status.delivery`, `assignment`, `verification` 값은 생성 직후 기본값으로만 취급한다.
- AI 에이전트, CLI, SCC 버튼은 직접 JSON을 편집하지 않고 `scc-action.mjs` 같은 허용된 명령을 통해 변경한다.

## Status Model

| Field | Values | Meaning |
|---|---|---|
| `workStatus` | `not_started`, `in_progress`, `in_review`, `implemented`, `verified`, `blocked`, `deferred` | 개발 작업 진행 상태 |
| `developer` | `Unassigned` or developer name | 개발 담당자 |
| `issue` | `null`, issue number, or issue URL | 연결 Issue |
| `pr` | `null`, PR number, or PR URL | 연결 PR |
| `branch` | `null` or branch name | 작업 브랜치 |
| `testStatus` | `not_started`, `passed`, `failed`, `not_applicable` | 테스트 검증 상태 |
| `screenshotStatus` | `not_started`, `passed`, `failed`, `not_applicable` | 스크린샷/시각 검증 상태 |
| `blockedReason` | `null` or text | blocked 이유 |
| `workPlan` | object | Feature 작업 계획. 목표, 작업 목록, 영향 파일, 테스트 계획, 리스크를 담는다. |
| `promptHistory` | array | Feature 작업을 위해 AI 에이전트에 전달한 프롬프트 기록. 최신 항목을 앞에 둔다. |
| `activeWork` | object | 에이전트별 현재 작업 중인 Feature. `start`가 설정하고 `done`/`verify`/`block`이 해제한다. |

## State Transitions

| Trigger | From | To | Required data |
|---|---|---|---|
| Assign developer | any | unchanged | `developer` |
| Create/update plan | any | unchanged | `summary`, `tasks`, `files`, `tests`, `risks` |
| Log prompt | any | unchanged | `agent`, `promptAction`, `prompt` |
| Start work | `not_started`, `blocked` | `in_progress` | `featureUid`, `developer`, `branch` |
| Open PR | `in_progress`, `implemented` | `in_review` | `pr` |
| Finish implementation | `in_progress`, `in_review` | `implemented` | tests/screenshots if available |
| Verify implementation | `implemented`, `in_review` | `verified` | passed or not-applicable verification |
| Block work | any | `blocked` | `blockedReason` |
| Unblock work | `blocked` | `not_started` or `in_progress` | developer presence decides target |
| Defer work | any | `deferred` | note or reason |

## Automation Policy

| Automation | Allowed changes | Notes |
|---|---|---|
| `linkit-spec` skill | Read/write tracking, audit, sync | Must call `scc-action.mjs`; no raw JSON editing. |
| `linkit-done` skill | `implemented`, test/screenshot status, branch, event | Should ask/derive Feature UID before changing state. |
| `linkit-pr` skill | `in_review`, PR, issue, branch, event | PR body should include affected Feature UIDs. |
| `linkit-pr-review` skill | verification notes, review warnings | Should not mark `verified` without explicit checks. |
| Feature regeneration | create new registry, sync tracking | Preserve existing tracking by Feature UID. |

## SCC Button Policy

SCC 버튼은 임의 쉘 명령을 실행하지 않는다. 버튼은 정해진 action contract만 발생시킨다.

`Start` 버튼은 예외적으로 즉시 개발을 시작시키지 않는다. 에이전트는 먼저 Feature 스펙, 이미지 근거, TBD, 영향 범위, 테스트 항목을 확인해 작업 목록을 작성하고 사용자에게 진행 여부를 확인해야 한다. 사용자가 승인하기 전에는 코드 수정, commit, `scc-action.mjs start` 실행, tracking 변경을 하지 않는다.

Allowed agents:

- `codex`
- `claude`

Allowed actions:

- `assign`
- `plan-set`
- `prompt-log`
- `start`
- `review`
- `done`
- `verify`
- `block`
- `unblock`
- `audit`
- `sync`

Static `file://` 모드에서는 보안상 브라우저가 직접 CLI를 실행할 수 없으므로 버튼은 선택된 AI 에이전트용 CLI 명령을 복사하거나 action payload를 만든다. 자동 실행은 사용자가 명시적으로 켠 localhost bridge를 통해서만 허용한다.

## CLI Contract

All tools must use this command shape:

```sh
node docs/specs/scc-action.mjs <action> --feature <screenSlug:FEATURE_ID> --agent <codex|claude> [options]
```

Examples:

```sh
node docs/specs/scc-action.mjs start --feature main:MAP_PAN --agent codex --developer yuho --branch feature/#31-home_map
node docs/specs/scc-action.mjs plan-set --feature main:MAP_PAN --agent codex --summary '지도 이동 작업 계획' --tasks 'Map 상태 확인, gesture 연결' --tests 'ViewModel 테스트, 스크린샷 테스트'
node docs/specs/scc-action.mjs prompt-log --feature main:MAP_PAN --agent codex --prompt-action plan --prompt '작업 계획을 작성해줘...'
node docs/specs/scc-action.mjs prompt-log --agent codex --prompt-action user-prompt --prompt '방금 입력한 실제 프롬프트'
node docs/specs/scc-action.mjs review --feature main:MAP_PAN --agent claude --pr '#123'
node docs/specs/scc-action.mjs done --feature main:MAP_PAN --agent codex --tests passed --screenshots passed
node docs/specs/scc-action.mjs audit
```

## Audit Requirements

Before commit or PR:

- `node docs/specs/generate-feature-list.mjs`
- `node docs/specs/scc-action.mjs sync`
- `node docs/specs/scc-action.mjs audit`

Audit must fail or warn on:

- tracking entry missing for a Feature UID
- tracking entry exists for deleted Feature UID
- invalid status enum
- invalid work plan status enum
- active work points to an unknown Feature UID
- `in_progress` with `Unassigned`
- `in_review` without PR
- `verified` without passed or not-applicable checks
- invalid JSONL event line

## Manual Override

Manual JSON edits are allowed only for emergency recovery. After a manual edit:

1. Run `node docs/specs/scc-action.mjs audit`.
2. Add a `manual_override` event to `feature-events.jsonl`.
3. Mention the override in the commit body.
