# SCC Action Bridge

> Bridge design version: `scc-action-bridge.v0.1.0`  
> Related policy: `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/spec-command-center-policy.md`

## Goal

SCC에서 Feature 버튼을 누르면 정해진 action payload가 만들어지고, 그 payload가 CLI 명령을 통해 AI 에이전트의 스킬 또는 추적 스크립트를 실행하도록 한다.

Desired flow:

```text
SCC button
  -> action payload
  -> local bridge or copied scc-agent command
  -> selected AI agent
  -> scc-action.mjs
  -> feature-tracking.json + feature-events.jsonl
  -> SCC refresh
```

## Why A Bridge Is Needed

`index.html`을 `file://`로 열면 브라우저는 보안상 로컬 CLI를 직접 실행할 수 없다. 따라서 SCC 버튼이 바로 쉘을 실행하는 구조는 만들면 안 된다.

대신 두 가지 모드를 둔다.

| 모드 | 동작 | 사용 시점 |
|---|---|---|
| 정적 모드 | 버튼이 짧은 SCC agent 실행 명령을 클립보드에 복사 | 지금 바로 사용 가능, 안전함 |
| 브리지 모드 | 사용자가 띄운 localhost bridge에 action payload 전송 | 나중에 자동 실행, 에이전트 스킬 연동 |

## 정적 모드

SCC는 버튼 클릭 시 선택된 AI 에이전트용 wrapper 명령을 만든다.

```sh
node docs/specs/scc-agent.mjs start --feature main:MAP_PAN --agent codex
node docs/specs/scc-agent.mjs start --feature main:MAP_PAN --agent claude
```

사용자는 복사된 명령을 터미널에서 실행한다. `scc-agent.mjs`가 한국어 지시문을 생성하고 선택된 AI CLI(`codex` 또는 `claude`)를 실행한다. 이 모드는 브라우저 권한이나 별도 서버가 필요 없다.

`시작 명령 복사`는 바로 개발을 시작하라는 명령이 아니다. 복사된 지시문은 에이전트에게 먼저 작업 목록, 영향 범위, 테스트 항목, 확인 질문을 정리하게 하고, 사용자가 진행을 승인한 뒤에만 아래와 같은 tracking 명령을 실행하도록 요구한다.

```sh
node docs/specs/scc-action.mjs start --feature main:MAP_PAN --agent codex
```

`start` tracking 명령은 현재 등록된 GitHub ID를 developer로 저장한다.

## 브리지 모드

Bridge는 사용자가 명시적으로 실행한 로컬 서버다. SCC는 `http://127.0.0.1:<port>/scc/actions`로 POST 요청만 보낸다.

```http
POST /scc/actions
Content-Type: application/json

{
  "action": "start",
  "agent": "codex",
  "featureUid": "main:MAP_PAN",
  "payload": {
    "branch": "feature/#31-home_map"
  },
  "actor": "yuho",
  "source": "scc"
}
```

Bridge는 payload를 검증한 뒤 allowlist에 있는 명령만 실행한다.

```sh
node docs/specs/scc-action.mjs start --feature main:MAP_PAN --branch feature/#31-home_map --source scc --actor yuho --agent codex
```

## 에이전트 선택

SCC는 action마다 실행 에이전트를 선택할 수 있어야 한다.

| 에이전트 | 정적 모드 명령 형태 | 브리지 책임 |
|---|---|---|
| `codex` | `node docs/specs/scc-agent.mjs <action> --feature <uid> --agent codex` | Codex에게 `linkit-spec` 스킬을 사용하게 한다. |
| `claude` | `node docs/specs/scc-agent.mjs <action> --feature <uid> --agent claude` | Claude에게 같은 SCC 정책 문서와 action command를 사용하게 한다. |

에이전트가 달라도 Feature 상태 저장은 항상 `scc-action.mjs`가 담당한다. Codex/Claude는 상태 파일을 직접 편집하지 않는다.

## 보안 규칙

- Bridge는 기본적으로 꺼져 있어야 한다.
- Bridge는 `127.0.0.1`에서만 listen한다.
- Bridge는 repo root가 `/Users/yuhohyeon/Desktop/project/LinkIt-KMP`인지 확인한다.
- Bridge는 action allowlist만 실행한다.
- Bridge는 agent allowlist(`codex`, `claude`)만 허용한다.
- Bridge는 shell string을 직접 실행하지 않고 argument array로 실행한다.
- SCC에서 보낸 arbitrary command는 무시한다.
- destructive git 명령은 bridge action에 넣지 않는다.
- `done`은 필요하면 AI 에이전트가 확인 질문을 할 수 있다.

## 액션 계약

| SCC 액션 | CLI 액션 | AI 스킬 연결 |
|---|---|---|
| 작업 계획 작성 | `plan-set` | `linkit-spec` |
| 프롬프트 기록 | `prompt-log` | `scc-agent.mjs` 자동 기록 |
| 작업 시작 | `start` | `linkit-spec` |
| 구현 완료 처리 | `done` | `linkit-done` |
| PR 생성 또는 연결 | `review` | `linkit-pr` |
| Audit 실행 | `audit` | `linkit-spec` |
| 스펙 재생성 후 동기화 | `sync` | `linkit-spec` |

## 권장 UX

Feature 상세 패널에는 아래 작업 버튼을 노출한다.

- `시작 명령 복사`
- `계획 명령 복사`
- `완료 명령 복사`
- `PR 명령 복사`
- `Audit 실행`

브리지 모드가 구현되면 같은 버튼이 액션 계약을 바꾸지 않고 복사 동작에서 POST 실행 동작으로 전환될 수 있다.

`계획 명령 복사`는 에이전트에게 Feature 스펙, 이미지 근거, TBD, 영향 범위, 테스트 항목을 검토하게 하고 사용자가 승인하면 `plan-set` 명령으로 Feature 하위 작업 계획을 저장하게 한다. `scc-agent.mjs`를 통해 실행된 Feature 작업 프롬프트는 `prompt-log` 액션으로 자동 기록된다.

`start` 명령이 승인되어 실행되면 SCC는 해당 Feature를 선택된 에이전트의 현재 작업 Feature로 저장한다. 이후 Claude/Codex `UserPromptSubmit` 훅은 Feature UID를 직접 추론하지 않아도 `prompt-log --agent <agent> --prompt ...` 형태로 호출할 수 있고, SCC는 현재 작업 Feature의 `Prompt History`에 기록한다. `done`은 해당 에이전트의 현재 작업 Feature를 해제한다.

## Future Skill Shape

권장 스킬 이름: `linkit-spec`

트리거 예시:

- "Feature 상태 바꿔줘"
- "SCC tracking audit 해줘"
- "Feature를 in_progress로 변경"
- "작업 완료된 Feature를 implemented로 표시"
- "스펙 재생성 후 tracking sync"

스킬 책임:

- 이 브리지 문서와 `spec-command-center-policy.md`를 읽는다.
- `feature-tracking.json`을 직접 편집하지 않는다.
- `node docs/specs/scc-action.mjs ...`를 호출한다.
- 변경된 Feature UID, 이전 상태, 변경 후 상태, audit 결과를 보고한다.

Claude 어댑터:

- `spec-command-center-policy.md`와 이 브리지 문서를 읽는다.
- 동일한 `scc-action.mjs` 명령을 실행한다.
- tracking/event 파일을 직접 편집하지 않는다.
- Codex와 같은 형식으로 상태 전이 요약을 보고한다.
