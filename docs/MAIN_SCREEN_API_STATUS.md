# 메인 화면 API 연결 현황

- 기준: `feature/#42-main_map_screen`, 2026-09-15 작업 트리
- 디자인: [Pingo v3.0.3 메인 화면](https://www.figma.com/design/Pym5oUSWQjyWVtN86oj6lb/Pingo--v3.0.3?node-id=17789-48296)
- 범위: 메인 지도/일정 카드, 영상 일정 생성 상태, 메인에서 진입하는 일정·영상 요약 상세
- 저장소의 API·DTO 계약을 기준으로 구현했다. [Swagger](https://linktrip.cloud/api/swagger-ui/index.html)는 확인 시 DNS 해석에 실패했으므로 실서버 동작이나 아래 미정의 API의 서버 제공 여부까지 검증한 문서는 아니다.

## 연결한 기능

| 화면/기능 | API | 연결 내용 |
| --- | --- | --- |
| 메인 일정 목록·지도 | `GET /trip-plans?cursor=…`, `GET /trip-plans/{tripPlanId}` | 기존 목록·좌표 연동 유지. 재진입 시 서버 데이터를 새로고침하며 현재 지도 위치와 유효한 선택을 보존 |
| 일정 카드 AI 요약·예상 비용 | `GET /video/schedule/{videoAnalysisTaskId}` | `summary`, `estimatedMinCost`, `estimatedMaxCost` 표시. 누락·조회 실패 시 대체 문구 표시 |
| 일정 카드 영상 썸네일 | YouTube `GET /oembed?url=…&format=json` | `thumbnail_url` 사용. 이미지 없는 경우 중립 플레이스홀더 표시 |
| 영상에서 일정 생성 | `POST /video/analyze` | 기존 생성 요청 유지. `PENDING`·`PROCESSING`을 진행 중으로 처리하고 분석 작업 ID를 로컬 저장 |
| 생성 진행·완료·실패 | `GET /video/schedule/{videoAnalysisTaskId}`, `GET /trip-plans` | 메인이 보이는 동안 상태 조회. 완료된 분석 ID에 대응하는 실제 일정 ID를 찾아 완료 안내의 ‘확인하기’에 연결 |
| 중복 생성 방지 | 로컬 pending ID + 기존 분석 API | 진행 중에는 새 요청 차단. 이미 저장된 같은 영상을 다시 분석할 때 기존 일정 ID를 제외하고 새 일정을 찾음 |
| 일정 상세 | `GET /trip-plans/{tripPlanId}` | 실제 일차·정렬 순서·장소명·카테고리·주소·설명·팁 표시, 일차 선택 연결 |
| 영상 요약 상세 | `GET /video/schedule/{videoAnalysisTaskId}`, 목록, oEmbed | 원본 영상 제목·썸네일·링크, AI 요약·태그·기간·비용·타임라인을 실제 데이터로 표시. 미제공 정보는 목업 값으로 채우지 않음 |
| 일정 이름 변경·삭제 | `PUT /trip-plans/{tripPlanId}`, `DELETE /trip-plans/{tripPlanId}` | 기존 API 연결 유지. 완료 후 메인 복귀 시 반영 |

메인 조회는 커서 페이지를 순회한다. 같은 분석 ID/영상 URL은 한 번의 조회 내에서 중복 요청하지 않으며 동시에 수행하는 부가 조회는 최대 4개다. 부가 정보 실패만으로 일정 목록 전체를 숨기지 않는다.

## API 간단 명세

Base URL: `https://linktrip.cloud/api/`. 아래 경로는 이 주소 기준이다.

| Method | Path | 요청 | 주요 응답 `data` |
| --- | --- | --- | --- |
| POST | `/auth/login` | `{ "serialNumber": "…" }` | `memberId`, `accessToken` |
| GET | `/trip-plans` | 선택 query `cursor` | `tripPlans[]`, `nextCursor`, `hasNext` |
| GET | `/trip-plans/{tripPlanId}` | path ID | `id`, `title`, `videoAnalysisTaskId`, `items[]`, `createdAt`, `updatedAt` |
| PUT | `/trip-plans/{tripPlanId}` | 선택 `title`, `items: [{ tripPlanItemId, day, itemOrder }]` | 변경된 일정 상세. `null` 필드는 미전송 |
| DELETE | `/trip-plans/{tripPlanId}` | path ID | 반환 데이터 없음 |
| POST | `/video/analyze` | `{ "youtubeUrl": "…" }` | 아래 영상 분석 객체 |
| GET | `/video/schedule/{videoAnalysisTaskId}` | path 분석 작업 ID | 아래 영상 분석 객체 |

- 목록 항목: `id`, `title`, `videoAnalysisTaskId`, `youtubeUrl`, `itemCount`, `nights`, `days`, `hashtags[]`, `createdAt`, `updatedAt`.
- 상세 `items[]`: `id`, `travelItineraryItemId`, `day`, `itemOrder`, `name`, `category`, 선택 `description`, `tips`, `place`.
- `place`: `id`, `name`, `googlePlaceId`, 선택 `address`, `latitude`, `longitude`.
- 영상 분석: `id`, `youtubeUrl`, `valid`, `status`, 선택 `summary`, `estimatedMinCost`, `estimatedMaxCost`, `costBasis`, `placeEnrichmentCompleted`, `timelines[]`, `itineraryItems[]`.
- 타임라인: `timestampSeconds`, `timestamp`, `timestampUrl`, `description`.
- 성공 응답 공통 래퍼: `{ "status": 200, "message": "OK", "data": … }`. 분석 진행 중에는 202일 수 있다.
- 실패 응답: `{ "code": "…", "message": "…" }`. HTTP 상태와 오류 코드를 함께 처리한다.
- LinkTrip 요청에는 저장된 토큰으로 `Authorization: Bearer …`를 첨부하고 non-GET에는 `Idempotency-Key`를 자동 첨부한다. YouTube 요청에는 LinkTrip 인증 정보를 보내지 않는다.

## 현재 계약에 없는 기능 — 백엔드 확인/추가 필요

‘없음’은 **현재 저장소의 API·DTO에 정의가 없음**을 뜻한다. 임의의 endpoint를 만들거나 성공한 것처럼 처리하지 않았다.

| 기능 | 필요한 계약 / 현재 처리 |
| --- | --- |
| 직접 일정 만들기 | 빈 일정 생성 및 장소를 넣는 요청. 디자인의 준비 중 안내 유지 |
| 보관함에서 일정 만들기 | 보관 항목 조회와 선택 장소로 일정 생성. 준비 중 안내 유지 |
| 장소 보관·폴더 선택·전체 담기 | 보관함/폴더 목록·생성·장소 저장 API. 기존 로컬/목업 화면 동작만 존재하며 서버 저장 미연결 |
| 일정에 새로운 장소 추가 | 장소 검색과 일정 항목 추가 계약. 현재 PUT의 `tripPlanItemId/day/itemOrder`만으로 새 장소 생성 계약을 추정할 수 없음 |
| 동일 영상으로 일정 복제 | 현재 분석 요청에는 `youtubeUrl`만 있고 복제 옵션/별도 일정 생성 API가 없음. 서버가 기존 task만 반환하는 경우 새 일정이 실제로 나타나야 완료 처리하며, 기존 일정을 새 일정으로 오인하지 않음 |
| 공유 링크 생성 | 공개 공유 URL/접근 권한/공유 토큰 계약. 원본 YouTube 열기와는 별개 |
| 장소 사진·평점·리뷰·영업시간·연락처 | 현재 `place`에 없는 필드 또는 별도 장소 상세 API. 메인 장소 카드에 남은 샘플 사진은 실제 장소 사진이 아님 |
| 앱이 닫혀 있을 때 완료 푸시 | 기기 push token 등록과 서버 완료 이벤트 발송 계약. 이번 구현은 화면 구독 중 폴링 + 재진입 시 재조회이며 백그라운드 푸시가 아님 |

## 계약은 있으나 추가 확인이 필요한 항목

| 항목 | 확인 내용 |
| --- | --- |
| 비용 단위·통화 | DTO에는 정수 금액과 `costBasis`만 있고 통화/단위가 없다. 현재 화면은 Figma처럼 원(KRW)으로 표시한다. 백엔드 원 단위 숫자인지 확인 필요 |
| 생성 완료와 일정 저장 시점 | 분석 `COMPLETED`와 일정 목록 반영 사이의 지연을 허용한다. 3초 간격 최대 100회 후에도 새 일정이 없으면 지연 안내와 재확인 제공 |
| 정규 도시 필터·최신순 | 현재 일정 목록 query는 `cursor`뿐이다. 지역은 장소 주소 기반 추정, 스타일/기간은 응답 필드 기반 로컬 필터다. 목록 순서는 서버 응답 순서를 유지하며 최신순 보장은 확인 필요 |
| 영상 작성자·조회수·길이 | 현재 oEmbed DTO에는 제목·썸네일만 있다. 작성자는 oEmbed 필드 확장 가능 여부를 확인해야 하며, 조회수·길이도 별도 데이터 계약이 필요하다. 실제 일정 상세에서는 가짜 작성자/조회수/길이를 표시하지 않음 |

추천 탐색용 `GET /video/discover/theme`, `/channels`, `/category`는 이미 저장소에 존재하므로 ‘API 없음’에 포함하지 않았다. 이번 메인 지도 연동 범위에서는 탐색 화면이나 영상 입력 하단의 추천 영역을 새로 연결하지 않았다.

## 검증 범위

2026-09-15 최종 검증 결과:

| 검증 | 결과 |
| --- | --- |
| domain 단위 테스트 | 45개 통과 |
| data 단위 테스트 | 3개 통과 |
| map 회귀·화면 테스트 | 78개 통과 |
| schedule 회귀·화면 테스트 | 19개 통과 |
| Android `assembleDebug` | 통과 |
| iOS Simulator Arm64 컴파일: app-shared, map, schedule | 통과 |
| API 연동 화면 스크린샷 | 메인 3종·상세 3종 생성 및 육안 검증. 비용 줄바꿈과 선택 장소 자동 스크롤 문제 보완 |
| `git diff --check` | 통과 |

```sh
./gradlew :domain:testDebugUnitTest :data:testDebugUnitTest :feature:map:testDebugUnitTest :feature:schedule:testDebugUnitTest :app-android:assembleDebug :app-shared:compileKotlinIosSimulatorArm64 :feature:map:compileKotlinIosSimulatorArm64 :feature:schedule:compileKotlinIosSimulatorArm64
./gradlew :feature:map:recordRoborazziDebug --tests '*MapApiContentTest'
./gradlew :feature:schedule:recordRoborazziDebug --tests '*TripDetailApiContentTest'
```

도메인·데이터 테스트는 가짜 저장소/인메모리 DataStore를, 화면 테스트는 Robolectric을 사용한다. 실서버 API 응답, 실제 지도 SDK/위치 권한, 앱 종료 후 복원, 실제 기기·시뮬레이터 실행을 확인한 E2E 테스트와는 구분한다. Figma 정량 이미지 일치율은 측정하지 않았다.
