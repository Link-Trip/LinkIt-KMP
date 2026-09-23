# 계약: 기존 API 사용 방식 — 온보딩 일정 생성

온보딩은 새 생성 API 없이 기존 `POST /video/analyze`와 `GET /trip-plans`를 아래 전제로 사용한다. 전제가 깨지면 클라이언트는 `NotReady`로 처리하고 사용자에게 재시도를 안내한다(스펙 Edge Case).

## POST /video/analyze (기존)

| 항목 | 값 |
|---|---|
| 요청 | `{ "youtubeUrl": "<정규화된 추천 영상 URL>" }`, 헤더 `Idempotency-Key`·`Authorization`은 `LinkTripHeaders`가 자동 첨부 |
| 기대 응답 | **200 OK**, `data.status = "COMPLETED"`, `data.id = videoAnalysisTaskId`, 결과 필드 인라인 |
| 허용하지 않는 응답 | 202(PENDING/PROCESSING) → 추천 영상이 사전 분석되지 않은 상태. 클라이언트 `NotReady` |
| 200 `INVALID` | 여행 영상 아님. 추천 영상에서는 발생하지 않아야 함. 발생 시 `NotReady` |
| 400 | URL 형식 오류(클라이언트 정규화로 사전 차단), 429는 `LinkTripApiException`으로 전파 → 실패 토스트 |

## GET /trip-plans (기존)

- 서버 문서: "영상 분석 완료 시 자동으로 여행 계획이 생성됩니다. 분석 요청 시점에 계정이 연결되어 있으면 즉시 생성".
- 클라이언트는 `analyze` 200 직후 커서 루프로 전체 목록을 받아 `tripPlans[].videoAnalysisTaskId == analysis.id`인 항목의 `id`, `title`을 얻는다.
- 매칭 실패 시 `GET /video/schedule/{analysis.id}`를 1회 호출한 뒤(문서: "분석 완료 후 해당 영상을 조회할 때 생성") 목록을 재조회한다. 그래도 없으면 `NotReady`.
- **검증 필요(리스크)**: 이미 분석 완료된 URL을 새 회원이 요청했을 때도 그 회원의 여행 계획이 생성되는지. quickstart의 curl 스모크로 확인한다.

## 제목 규칙

자동 생성 여행 계획의 `title`은 서버가 `국가/도시 + x박x일 + 여행 스타일`(여행 스타일 표기: 맛집 중심→맛집투어, 쇼핑 중심→쇼핑투어, 명소 탐방 중심→명소투어, 자연·풍경 위주→자연탐방, 문화·역사 탐방→문화탐방, 액티비티→액티비티 여행, 힐링→힐링 여행)로 만든다. 클라이언트는 가공하지 않는다.

## 클라이언트 측 `확인전` 표시

서버 필드 없음. `Created(tripPlanId)` 시 로컬 `unchecked_trip_plan_ids`에 추가하고 상세 진입 시 제거한다([data-model.md §2](../data-model.md)).
