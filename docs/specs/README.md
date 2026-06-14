# 스펙 문서 인덱스

> 기준 Figma: [Pingo v3.0.3](https://www.figma.com/design/Pym5oUSWQjyWVtN86oj6lb/Pingo--v3.0.3?node-id=13008-1945&m=dev)  
> 기준 노드: `13008:1945`  
> 작성일: 2026-06-14

## 화면별 문서

| 화면군 | 문서 | Figma section | 상태 |
|---|---|---|---|
| 온보딩/최초진입/일정 생성 | [onboarding-screen.md](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/onboarding-screen.md) | `15091:142439` 인트로 / 최초진입 | `v0.1.2` |
| 메인 지도 | [main-screen.md](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/main-screen.md) | `15091:144859` 메인화면 / 지도 | `v0.2.0` |
| 탐색 & 피드 | [explore-feed-screen.md](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/explore-feed-screen.md) | `15091:151607` 탐색 & 피드 | `v0.1.2` |
| 마이페이지 | [mypage-screen.md](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/mypage-screen.md) | `15091:151966` 마이페이지 | `v0.1.2` |
| 보관함 | [library-screen.md](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/library-screen.md) | `15091:152408` 보관함 | `v0.1.3` |

## 문서 작성/검토 파이프라인

| 문서 | 용도 |
|---|---|
| [spec-document-pipeline.md](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/agents/spec-document-pipeline.md) | Figma 기반 스펙 작성, 서브에이전트 리뷰, 수정, 재검토 절차 |
| [spec-document-reviewer.md](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/agents/spec-document-reviewer.md) | 스펙 문서 리뷰 서브에이전트 지침 |

## Spec Command Center

| 문서 | 용도 |
|---|---|
| [index.html](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/index.html) | Feature List를 화면군/상태/개발자/정책 기준으로 탐색하는 HTML 대시보드 |
| [spec-command-center-policy.md](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/spec-command-center-policy.md) | SCC 운영 정책, source of truth, 상태 전이, 자동화 기준 |
| [scc-action-bridge.md](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/scc-action-bridge.md) | SCC 버튼에서 CLI/AI 스킬로 이어지는 action bridge 설계 |
| [feature-list.schema.md](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/feature-list.schema.md) | Spec Command Center Feature List 규격 |
| [feature-list.json](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/feature-list.json) | 화면별 스펙에서 생성한 canonical Feature List |
| [feature-list.js](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/feature-list.js) | `file://` HTML에서 Feature List를 읽기 위한 wrapper |
| [feature-tracking.json](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/feature-tracking.json) | Feature 개발자/상태/Issue/PR/검증 추적 source |
| [feature-tracking.js](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/feature-tracking.js) | `file://` HTML에서 tracking을 읽기 위한 wrapper |
| [feature-events.jsonl](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/feature-events.jsonl) | SCC 상태 변경 append-only event log |
| [generate-feature-list.mjs](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/generate-feature-list.mjs) | Feature List 재생성 스크립트 |
| [scc-action.mjs](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/scc-action.mjs) | SCC/AI 스킬/CLI가 공통으로 사용하는 Feature tracking action runner |

## 공통 기준

| 기준 | 내용 |
|---|---|
| 이미지 경로 | Markdown 이미지에는 절대 경로를 사용한다. |
| Figma 근거 | description, 화면 텍스트, 참조 이미지, sitemap 메모를 근거로 작성한다. |
| 미정 처리 | Figma에서 질문형/미정으로 남은 내용은 `TBD`로 분리한다. |
| 변경 이력 | 스펙 변경 시 버전, 날짜, 변경 사항, 근거를 기록한다. |
