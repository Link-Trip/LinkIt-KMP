# Spec Command Center Feature List Schema

> Schema version: `spec-command-center.feature-list.v1.1`  
> Canonical data: `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/feature-list.json`  
> Browser wrapper: `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/feature-list.js`

## Purpose

이 규격은 화면별 Markdown 스펙을 Spec Command Center에서 조회 가능한 Feature List로 정규화하기 위한 데이터 계약이다.

Markdown 문서는 제품 스펙의 원천이고, Feature List는 팀 협업/작업 분배를 위한 읽기 전용 registry다. 실제 구현 진행 상태, 개발자, Issue, PR, Feature별 작업 계획과 프롬프트 기록은 `feature-tracking.json`에서 관리한다.

생성 직후의 작업 추적 필드는 모두 초기 상태로 둔다. `status.delivery`는 `not_started`, `assignment.developer`는 `Unassigned`, Issue/PR/branch와 검증 결과는 비어 있는 상태가 기본이다. SCC에서 보여주는 실제 작업 상태는 `feature-tracking.json`이 있으면 그 값을 우선한다.

## Top-Level Shape

| Field | Type | Description |
|---|---|---|
| `schemaVersion` | string | Feature List 규격 버전 |
| `generatedAt` | ISO datetime | 생성 시각 |
| `project` | object | 프로젝트와 기준 Figma 정보 |
| `enums` | object | 상태/타입/개발자 enum |
| `specDocuments` | array | 화면별 스펙 문서 목록 |
| `imageAssets` | array | 문서별 이미지 근거 매핑 |
| `tbdItems` | array | 문서별 미정/정책 필요 항목 |
| `features` | array | Spec Command Center의 중심 Feature 목록 |

## ID Scoping

Markdown 문서에는 `IMG-01`, `TBD-01`, `VIDEO_LINK_INPUT`처럼 문서별 local ID가 존재한다. 대시보드 내부에서는 충돌을 막기 위해 반드시 화면 scope를 붙인다.

| Entity | UID Format | Example |
|---|---|---|
| Feature | `{screenSlug}:{featureId}` | `main:MAP_PAN` |
| Image | `{screenSlug}:{imageId}` | `library:IMG-04` |
| TBD | `{screenSlug}:{tbdId}` | `explore:TBD-01` |

## Feature

| Field | Type | Description |
|---|---|---|
| `uid` | string | 대시보드 내부 고유 ID |
| `featureId` | string | Markdown 상세 기능 명세의 원본 Feature ID |
| `title` | string | 기능명 |
| `behavior.trigger` | string | 사용자의 액션 또는 시스템 조건 |
| `behavior.response` | string | 화면 반응/결과 |
| `classification.screenSlug` | string | `main`, `onboarding`, `explore`, `mypage`, `library` |
| `classification.area` | string | 화면군 이름 |
| `classification.subarea` | string | 화면 내 세부 영역 |
| `classification.type` | enum | 작업 성격 |
| `classification.tags` | string[] | 검색/필터용 태그 |
| `status.spec` | enum | 스펙 확정도 |
| `status.evidence` | enum | 근거 수준 |
| `status.delivery` | enum | 개발 진행 상태. 생성 시 기본값은 `not_started` |
| `status.raw` | string | Markdown의 원문 확정 수준 |
| `assignment.developer` | string | 현재 개발 담당자. 배정 전에는 `Unassigned` |
| `assignment.issue` | string/null | 연결 Issue |
| `assignment.pr` | string/null | 연결 PR |
| `verification.testStatus` | enum/string | 테스트 상태 |
| `verification.screenshotStatus` | enum/string | 스크린샷 검증 상태 |
| `links.imageRefs` | string[] | 관련 이미지 UID 목록 |
| `links.tbdRefs` | string[] | 연결 TBD UID 목록 |
| `links.dependsOn` | string[] | 선행 의존성 |
| `links.relatedFeatureRefs` | string[] | 관련 Feature UID 목록 |
| `links.specPath` | string | 원천 Markdown 문서 |
| `links.sourceLine` | number | 원천 Markdown line |
| `links.figmaSection` | string | 기준 Figma section |
| `links.specVersion` | string | 원천 스펙 버전 |

## Status Enums

### `status.spec`

| Value | Meaning |
|---|---|
| `confirmed` | Figma/문서 근거가 충분하여 구현 가능한 상태 |
| `partial` | 일부 확정, 일부 정책 확인 필요 |
| `needs_policy` | 정책 결정 필요 |
| `inferred` | 추론 기반 |
| `variant` | A/B안 또는 후보안 |
| `out_of_scope` | 현재 범위 제외 |

### `status.evidence`

| Value | Meaning |
|---|---|
| `figma_confirmed` | Figma 화면/description 직접 근거 |
| `sitemap_based` | sitemap 기반 |
| `memo_based` | Figma 메모 기반 |
| `agent_inferred` | 에이전트 추론 |
| `mixed` | 복수 근거 혼합 |

### `status.delivery`

| Value | Meaning |
|---|---|
| `not_started` | 아직 구현 착수 전 |
| `ready` | 구현 가능 |
| `in_progress` | 구현 중 |
| `in_review` | PR/리뷰 중 |
| `implemented` | 구현 완료 |
| `verified` | 구현 및 검증 완료 |
| `blocked` | TBD/정책/대안 확정 대기 |
| `deferred` | 이번 범위 제외 |

## Feature Types

| Type | Example |
|---|---|
| `navigation` | 탭 이동, 상세 이동, 더보기 |
| `map_interaction` | 지도 이동, 줌, 마커 선택 |
| `selection` | 국가/테마/지도 타입 선택 |
| `input` | 링크 입력, 폴더명 입력 |
| `validation` | 유효하지 않은 링크, 중복 폴더명 |
| `display_state` | 초기 표시, 로딩, 완료, 카드 노출 |
| `modal_dialog` | 초기화 확인, 삭제 확인 |
| `filter_sort` | 필터, 정렬 |
| `async_process` | 일정 분석 중/완료 |
| `variant_experiment` | 보관함 A/B안 |
| `external_integration` | YouTube, Google Map, GPS, clipboard |

## Regeneration

Feature List는 화면별 Markdown 스펙에서 생성한다.

```sh
node docs/specs/generate-feature-list.mjs
```

생성 결과:

| File | Purpose |
|---|---|
| `feature-list.json` | canonical Feature List |
| `feature-list.js` | `file://` HTML에서 읽기 위한 wrapper |
| `feature-tracking.json` | 개발자/상태/Issue/PR/검증/작업 계획/프롬프트 기록 추적 source |
| `feature-tracking.js` | `file://` HTML에서 tracking을 읽기 위한 wrapper |
| `feature-events.jsonl` | 작업 상태 변경 append-only log |

## Dashboard Contract

Spec Command Center는 `feature-list.js`와 `feature-tracking.js`를 로드한다.

브라우저 화면에서 사용하는 테이블/필터/drawer 모델은 Feature registry에 tracking을 overlay한 view model이다. Feature 정의는 항상 `feature-list.json`을 기준으로 보고, 작업 진행 상태, 작업 계획, 프롬프트 기록은 `feature-tracking.json`을 기준으로 본다.
