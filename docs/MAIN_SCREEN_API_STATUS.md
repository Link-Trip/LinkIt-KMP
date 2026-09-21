# 메인·탐색 화면 API 연결 현황

- 기준: `feature/#42-main_map_screen`, 2026-09-21 작업 트리
- 디자인: [Pingo v3.0.3 메인 화면](https://www.figma.com/design/Pym5oUSWQjyWVtN86oj6lb/Pingo--v3.0.3?node-id=17789-48296)
- 범위: 메인 지도/일정 카드, 영상 일정 생성·추천, 일정·영상 요약 상세, 탐색·추천 크리에이터, develop에서 병합된 마이페이지 API
- 근거: [Swagger](https://linktrip.cloud/api/swagger-ui/index.html), [OpenAPI 원본](https://linktrip.cloud/api/v3/api-docs). 2026-09-21 명세 확인에 성공했다. 과거 DNS 오류로 보류했던 API 유무·비용 단위를 정정한다.
- 통합: `origin/develop`의 `7e9eafd`를 merge commit `f0f373b`로 병합했다. 기존 지도·분석 추적과 신규 마이페이지 연동을 함께 유지한다.

## 연결한 기능

| 화면/기능 | API | 연결 내용 |
| --- | --- | --- |
| 메인 일정 목록·지도 | `GET /trip-plans?cursor=…`, `GET /trip-plans/{tripPlanId}` | 기존 목록·좌표 연동 유지. 재진입 시 서버 데이터를 새로고침하며 현재 지도 위치와 유효한 선택을 보존 |
| 일정 카드 AI 요약·예상 비용 | `GET /video/schedule/{videoAnalysisTaskId}` | `summary`, `estimatedMinCost`, `estimatedMaxCost` 표시. 누락·조회 실패 시 대체 문구 표시 |
| 일정 카드 영상 썸네일 | YouTube `GET /oembed?url=…&format=json` | `thumbnail_url` 사용. 이미지 없는 경우 중립 플레이스홀더 표시 |
| 영상에서 일정 생성 | `POST /video/analyze` | 기존 생성 요청 유지. `PENDING`·`PROCESSING`을 진행 중으로 처리하고 분석 작업 ID를 로컬 저장 |
| 생성 진행·완료·실패 | `GET /video/schedule/{videoAnalysisTaskId}`, `GET /trip-plans` | 메인이 보이는 동안 상태 조회. 완료된 분석 ID에 대응하는 실제 일정 ID를 찾아 완료 안내의 ‘확인하기’에 연결 |
| 중복 생성 방지 | 로컬 pending ID + 기존 분석 API | 진행 중에는 새 요청 차단. 최초 요청 응답 대기 중 추천 선택·붙여넣기·직접 입력으로 제출을 취소하지 않으며, 링크 복사는 허용. 이미 저장된 같은 영상을 다시 분석할 때 기존 일정 ID를 제외하고 새 일정을 찾음 |
| 일정 상세 | `GET /trip-plans/{tripPlanId}` | 실제 일차·정렬 순서·장소명·카테고리·주소·설명·팁 표시, 일차 선택 연결 |
| 영상 요약 상세 | `GET /video/schedule/{videoAnalysisTaskId}`, 목록, oEmbed | 원본 영상 제목·썸네일·링크, AI 요약·태그·기간·비용·타임라인을 실제 데이터로 표시. 미제공 정보는 목업 값으로 채우지 않음 |
| 일정 이름 변경·삭제 | `PUT /trip-plans/{tripPlanId}`, `DELETE /trip-plans/{tripPlanId}` | 기존 API 연결 유지. 완료 후 메인 복귀 시 반영 |

메인 조회는 커서 페이지를 순회한다. 같은 분석 ID/영상 URL은 한 번의 조회 내에서 중복 요청하지 않으며 동시에 수행하는 부가 조회는 최대 4개다. 부가 정보 실패만으로 일정 목록 전체를 숨기지 않는다.

## 추가 연결한 API

| API | 연결 내용 |
| --- | --- |
| `GET /video/discover/countries` | 대표 여행지 TOP 10. `countries: [{country: String, tripPlanCount: Long}]`, 생성 일정 수 내림차순 |
| `GET /video/discover/category` | 국가·지역별 탐색 영상과 일정 생성 화면 추천 목록. `country` 또는 `region`, 둘 다 없으면 전체. 동시 전달은 400 |
| `GET /video/discover/theme` | 테마별 영상·더보기. 필수 `theme`, 선택 `cursor`, 응답 `videos[]/nextCursor/hasNext` |
| `GET /video/discover/channels` | 추천 채널·구독자 수·채널별 최신 영상 |
| `PUT /members/me/notification` | develop에서 병합된 마이페이지·알림 권한 안내 |
| `DELETE /members/me` | develop에서 병합된 앱 초기화. 회원 탈퇴·여행 계획 삭제·FCM 제거 |
| `POST /feedback` | develop에서 병합된 의견 보내기 |
| `PUT /members/me/fcm-token` | 데이터 계층 및 `RegisterFcmTokenUseCase`. 실제 플랫폼 토큰 발급·갱신 연결은 외부 설정 필요 |

앱용 API는 기존 7개와 위 8개로 총 15개다. health 진단 API 4개는 앱 연동 대상에서 제외한다. 실제 무인증 탐색 GET 4개는 모두 401이므로 앱은 인증 확보·401 재인증 1회 후 조회한다.

탐색 영상 길이는 ISO 8601(`PT35M29S`), 조회수·좋아요·구독자 수는 `Long`이다. 테마 문자열 예시는 `맛집여행`, `힐링여행`, `액티비티`이며 UI 라벨과 요청값을 구분한다. 영상 및 채널을 누르면 실제 원본 URL을 사용한다.

## 기존 API 간단 명세

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
| GET | `/video/discover/countries` | 없음 | `countries: [{country, tripPlanCount}]` |
| GET | `/video/discover/category` | 선택 `country` 또는 `region` | `videos[]` |
| GET | `/video/discover/theme` | 필수 `theme`, 선택 `cursor` | `videos[]`, `nextCursor`, `hasNext` |
| GET | `/video/discover/channels` | 없음 | `channels[]` |
| PUT | `/members/me/notification` | `{ "enabled": true }` | `enabled` |
| PUT | `/members/me/fcm-token` | `{ "fcmToken": "SDK 토큰", "platform": "IOS 또는 ANDROID" }` | 반환 데이터 없음. 플랫폼 발급 연동은 아래 보류 사항 참조 |
| DELETE | `/members/me` | 없음 | `deletedTripPlanCount` |
| POST | `/feedback` | `type`, `content`, `appVersion`, `platform`, `osVersion`, `deviceModel` | 반환 데이터 없음 |

- 목록 항목: `id`, `title`, `videoAnalysisTaskId`, `youtubeUrl`, `itemCount`, `nights`, `days`, `hashtags[]`, `createdAt`, `updatedAt`.
- 상세 `items[]`: `id`, `travelItineraryItemId`, `day`, `itemOrder`, `name`, `category`, 선택 `description`, `tips`, `place`.
- `place`: `id`, `name`, `googlePlaceId`, 선택 `address`, `latitude`, `longitude`.
- 영상 분석: `id`, `youtubeUrl`, `valid`, `status`, 선택 `summary`, `estimatedMinCost`, `estimatedMaxCost`, `costBasis`, `placeEnrichmentCompleted`, `timelines[]`, `itineraryItems[]`.
- 타임라인: `timestampSeconds`, `timestamp`, `timestampUrl`, `description`.
- 탐색 영상: `videoId`, `videoUrl`, `title`, `description`, `thumbnailUrl`, `channelId`, `channelTitle`, `duration`, `publishedAt`, `viewCount`, `likeCount`, `region`, `country`, 선택 `city`, `theme`.
- 추천 채널: `channelId`, `title`, `description`, `thumbnailUrl`, `subscriberCount`, `videoCount`, `recentVideos[]` (`videoId`, `title`, `thumbnailUrl`, `publishedAt`, `videoUrl`).
- 의견: `type`은 `SUGGESTION/BUG/ETC`, `content`는 trim 후 1~200자. KST 하루 5회 초과 시 429. 플랫폼·앱 버전·기기 정보는 자동 첨부한다.
- 일정 PUT의 `items`는 **기존 항목**의 일차·순서 변경만 지원한다(`day/itemOrder >= 1`). 생략 항목은 삭제되지 않는다. 현재 화면에서는 이름 변경에 연결했고 순서 편집 UI는 아직 없다.
- 성공 응답 공통 래퍼: `{ "status": 200, "message": "OK", "data": … }`. 분석 진행 중에는 202일 수 있다.
- 실패 응답: `{ "code": "…", "message": "…" }`. HTTP 상태와 오류 코드를 함께 처리한다.
- LinkTrip 요청에는 저장된 토큰으로 `Authorization: Bearer …`를 첨부하고 non-GET에는 `Idempotency-Key`를 자동 첨부한다. YouTube 요청에는 LinkTrip 인증 정보를 보내지 않는다.

## 현재 Swagger에 없는 기능 — 백엔드 추가 필요

‘없음’은 **2026-09-21 배포된 OpenAPI에 정의가 없음**을 뜻한다. 임의의 endpoint를 만들거나 성공한 것처럼 처리하지 않았다.

| 기능 | 필요한 계약 / 현재 처리 |
| --- | --- |
| 직접 일정 만들기 | 빈 일정 생성 및 장소를 넣는 요청. 디자인의 준비 중 안내 유지 |
| 보관함에서 일정 만들기 | 보관 항목 조회와 선택 장소로 일정 생성. 준비 중 안내 유지 |
| 장소 보관·폴더 선택·전체 담기 | 보관함/폴더 목록·생성·장소 저장 API. 기존 로컬/목업 화면 동작만 존재하며 서버 저장 미연결 |
| 일정에 새로운 장소 추가 | 장소 검색과 일정 항목 추가 계약. 현재 PUT의 `tripPlanItemId/day/itemOrder`만으로 새 장소 생성 계약을 추정할 수 없음 |
| 동일 영상으로 일정 복제 | 현재 분석 요청에는 `youtubeUrl`만 있고 복제 옵션/별도 일정 생성 API가 없음. 서버가 기존 task만 반환하는 경우 새 일정이 실제로 나타나야 완료 처리하며, 기존 일정을 새 일정으로 오인하지 않음 |
| 공유 링크 생성 | 공개 공유 URL/접근 권한/공유 토큰 계약. 원본 YouTube 열기와는 별개 |
| 장소 사진·평점·리뷰·영업시간·연락처 | 현재 `place`에 없는 필드 또는 별도 장소 상세 API. 메인 장소 카드에 남은 샘플 사진은 실제 장소 사진이 아님 |
| 장소 간 이동 시간·경로 | 해당 응답 필드/endpoint 없음 |

## FCM: API는 있으나 외부 설정이 필요함

`PUT /members/me/fcm-token`은 `{fcmToken, platform: "IOS" | "ANDROID"}`를 받으며 Bearer와 Idempotency-Key가 필수다. 응답 data는 생략 가능하다. 실제 SDK 토큰을 등록하는 데이터 계층·UseCase를 구현했고, 빈 토큰이나 기기 ID를 토큰 대신 보내지 않는다.

현재 저장소에는 Firebase SDK 초기화·토큰 갱신 콜백과 설정 파일이 없다. Android `google-services.json`, iOS `GoogleService-Info.plist` 및 APNs 설정을 받은 뒤 앱 시작/토큰 갱신 자동 등록·실기기 푸시 검증이 필요하다. 현재 분석 추적은 화면 구독 중 폴링·재진입 재조회이며 백그라운드 푸시가 아니다.

## 계약 확인·보완 사항

| 항목 | 확인 내용 |
| --- | --- |
| 비용 단위·통화 확인 완료 | 명세상 nullable int64(`Long`), 1인 기준 KRW이며 국제선 항공료 제외. 기존 `Int`를 `Long`으로 변경. `VIDEO_MENTIONED`는 영상 직접 언급 비용 ±10%, `ITEM_ESTIMATED`는 개별 항목 합산 추정 |
| 생성 완료와 일정 저장 시점 | 분석 `COMPLETED`와 일정 목록 반영 사이의 지연을 허용한다. 3초 간격 최대 100회 후에도 새 일정이 없으면 지연 안내와 재확인 제공 |
| 정규 도시 필터·최신순 | 현재 일정 목록 query는 `cursor`뿐이다. 지역은 장소 주소 기반 추정, 스타일/기간은 응답 필드 기반 로컬 필터다. 목록 순서는 서버 응답 순서를 유지하며 최신순 보장은 확인 필요 |
| 영상 작성자·조회수·길이 | 현재 oEmbed DTO에는 제목·썸네일만 있다. 작성자는 oEmbed 필드 확장 가능 여부를 확인해야 하며, 조회수·길이도 별도 데이터 계약이 필요하다. 실제 일정 상세에서는 가짜 작성자/조회수/길이를 표시하지 않음 |

대표 여행지 API는 나라별 통계이며 메인 저장 일정의 정규 도시 필터 API와는 다르다. 저장 일정 목록은 계속 서버 응답 순서를 유지한다. 분석 enum에는 PROCESSING이 빠져 있지만 endpoint 설명에는 진행 중 202 상태로 명시돼 있어 기존 지원을 유지한다.

## 검증 범위

2026-09-21 API 연결·E2E 발견 이슈·디자인 보정·제출 중 입력 회귀 수정 후 자동 검증 결과(총 236개, 실패·오류·스킵 0):

| 검증 | 결과 |
| --- | --- |
| domain 단위 테스트 | 64개 통과 |
| data 단위 테스트 | 18개 통과 |
| map 회귀·화면 테스트 | 105개 통과 |
| schedule 회귀·화면 테스트 | 33개 통과 |
| explore 회귀·화면 테스트 | 16개 통과 |
| Android `assembleDebug` / `assembleRelease` | API 연결 후 debug 통과, 최종 UI 보정 후 release 통과 |
| iOS Simulator Arm64 컴파일: app-shared, map, schedule, explore | 통과 |
| API 연동 화면 스크린샷 | 탐색 3종·크리에이터 1종·일정 생성 화면 재생성 및 육안 검증. 원격 이미지 없는 명시적 fixture 사용 |
| `git diff --check` | 통과 |

```sh
./gradlew :domain:testDebugUnitTest :data:testDebugUnitTest :feature:map:testDebugUnitTest :feature:schedule:testDebugUnitTest :feature:explore:testDebugUnitTest :app-android:assembleRelease :app-shared:compileKotlinIosSimulatorArm64 :feature:map:compileKotlinIosSimulatorArm64 :feature:schedule:compileKotlinIosSimulatorArm64 :feature:explore:compileKotlinIosSimulatorArm64
./gradlew :feature:explore:recordRoborazziDebug --tests '*ExploreScreenshotTest' --tests '*RecommendedCreatorsScreenshotTest' :feature:schedule:recordRoborazziDebug --tests '*ScheduleRecommendationsTest'
```

도메인·데이터 테스트는 가짜 저장소/인메모리 DataStore를, 화면 테스트는 Robolectric을 사용한다. 실제 서버·지도 SDK·에뮬레이터 실행 결과는 [E2E 검증 기록](API_E2E_VALIDATION.md)에 별도로 정리한다. Figma 정량 이미지 일치율은 측정하지 않았다.

이전 단계인 develop 병합 직후에는 테스트 191개와 Android debug 빌드·app-shared/map/schedule iOS Simulator Arm64 컴파일을 통과했다. 당시 실서버 확인은 명세와 무인증 탐색 GET의 401 응답까지였다. 이후 요청받은 E2E에서는 실제 인증된 탐색 조회와 영상 분석 생성 요청도 수행했다. 기존 일정 삭제·앱 데이터 초기화·의견 전송은 하지 않았다.
