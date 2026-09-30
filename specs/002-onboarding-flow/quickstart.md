# Quickstart: 온보딩 플로우 검증 가이드

**Spec**: [spec.md](spec.md) | **Contracts**: [contracts/](contracts/) | **Data model**: [data-model.md](data-model.md)

## 전제

- Android Studio + JDK 17, `local.properties`에 `MAPS_API_KEY`
- 서버 `https://linktrip.cloud/api` 접근 가능. 추천 영상은 `GET /video/discover/category`(파라미터 없음 = 전체) 상위 8개이며, 그 영상들이 사전 분석되어 있어야 튜토리얼이 완주된다(아니면 `미리 준비된 일정을 가져오지 못했어요` 안내).
- 온보딩을 다시 보려면 앱 데이터를 삭제하거나(설정 → 앱 → 저장공간 삭제) 마이페이지의 앱 초기화(PR #48)를 사용한다. 앱 초기화는 실제 회원 탈퇴(`DELETE /members/me`)이므로 테스트 계정으로만 실행한다.

## 자동 검증

```bash
# 도메인: YouTubeUrl 정규화, CreateOnboardingSchedule(InvalidFormat/NotRecommended/NotReady/Created/401 재시도), CompleteOnboarding 순서, ResetApp의 clearAll·clearUnchecked 호출
./gradlew :domain:allTests

# data: DataStore 로컬 소스(온보딩 키·확인전 집합·clear), AppSettingsLocalDataSource 축소 회귀(clearAll 2키)
./gradlew :data:allTests

# 디자인 시스템 컴포넌트 골든(코치마크·체크박스·모달 시트)
./gradlew :core:designsystem:testDebugUnitTest

# intro / map / schedule ViewModel 테스트 + 스크린샷
./gradlew :feature:intro:testDebugUnitTest :feature:map:testDebugUnitTest :feature:schedule:testDebugUnitTest

# 전체 빌드 (iOS 수동 바인딩 누락 시 MissingBinding)
./gradlew :app-android:assembleDebug
./gradlew :app-shared:linkDebugFrameworkIosSimulatorArm64
```

스크린샷 골든 생성·검증은 [docs/COMPOSE_IMPLEMENTATION_GUIDE.md](../../docs/COMPOSE_IMPLEMENTATION_GUIDE.md) "스크린샷 테스트 작성법"의 `recordRoborazziDebug` / `verifyRoborazziDebug`를 따른다. 골든 대상: 온보딩 시작, 약관 시트(미체크/전체 체크), 약관 상세(로딩/실패), 지도 튜토리얼 1·2단계, 영상 링크 튜토리얼 3·4단계, 클립보드 토스트, 분석 완료, 지도 `확인전` 카드, 빈 상태.

## 수동 시나리오 (스펙 User Story 대응)

| # | 스토리 | 절차 | 기대 결과 |
|---|---|---|---|
| 1 | US1 최초 진입 | 앱 데이터 삭제 후 실행 | 지구 인트로 → 온보딩 시작 화면(배지·제목·설명·버튼 2개) |
| 2 | US1 바로 시작 | `바로 시작하기` → 약관 2개 체크 → `동의하고 시작하기` | 메인 화면, 저장 일정 없으면 빈 상태 안내(`저장된 일정이 없어요`, `일정 생성하기`, `볼만한 영상 찾아보기`) |
| 3 | US1 재실행 | 앱 강제 종료 후 실행 | 인트로 → 메인 화면 바로 진입(온보딩·약관 재노출 없음) |
| 4 | US1 뒤로가기 | 온보딩 시작 화면에서 시스템 뒤로가기 | 앱 종료(인트로로 복귀 없음) |
| 5 | US2 약관 동기화 | `30초 만에 사용법 보기` → `전체 동의` 탭/해제, 개별 2개 체크 | 전체 동의 ↔ 개별 동기화, 둘 다 체크 시에만 버튼 활성 |
| 6 | US2 상세보기 | 개별 항목 체크 후 `상세보기` → 뒤로가기 | 약관 상세(제목·웹페이지) 표시, 복귀 시 체크 유지 |
| 7 | US2 시트 닫기 | 시트를 끌어 내리거나 바깥 탭 | 시작 화면으로 복귀, 아무것도 기록 안 됨(재실행 시 여전히 온보딩) |
| 8 | US3 튜토리얼 1·2단계 | `사용법 보기` → 동의 → 지도에서 어두운 영역·하단 탭 탭 → FAB 탭 → `보관함에서 가져오기` 탭 → `영상 링크로 만들기` 탭 | 어두운 영역 무반응, FAB 후 메뉴+2단계 말풍선, 비활성 항목 무반응, 영상 링크 화면 진입 |
| 9 | US3 튜토리얼 3·4단계 | 진입 후 대기 → `링크복사` → 클립보드 토스트 확인 → `복사한 링크 붙여넣기` | 잠시 뒤 3단계 말풍선, 복사 후 토스트(제목+주소+닫기)·칩 활성·4단계 말풍선, 붙여넣기 후 입력란 채움·`일정 생성하기` 활성 |
| 10 | US3 완주 | `일정 생성하기` → `생성된 일정 확인하기` | 2초 이내 분석 완료 화면 → 메인 화면, `저장한 일정` 맨 위 새 일정이 강조 배경, 지도에 마커·이름 말풍선 |
| 11 | US3 추천 외 링크 | 4단계 후 입력란을 `https://youtu.be/dQw4w9WgXcQ`로 바꿔 `일정 생성하기` | `온보딩에서는 추천 영상 링크만 사용할 수 있어요` 토스트, 버튼 비활성, 수정 시 재활성 |
| 12 | US3 잘못된 링크 | 입력란에 `abc` → `일정 생성하기` | `유효한 영상 링크가 아닙니다` 토스트, 버튼 비활성 |
| 13 | US3 건너뛰기 | 1단계·2단계·3단계 각각에서 `건너뛰기` | 즉시 메인 화면, 재실행 시 온보딩 없음 |
| 14 | US3 뒤로가기 무시 | 3단계 말풍선 상태에서 시스템 뒤로가기 | 무반응 |
| 15 | US3 중단 후 재실행 | 3단계에서 앱 강제 종료 → 실행 | 인트로 → 온보딩 시작 화면부터 |
| 16 | US4 확인전→확인후 | #10 이후 강조 카드 탭 → 상세(여행 일정/영상 요약 탭) → 뒤로 | 카드 일반 배경, 재실행 후에도 일반 배경·일정 유지 |
| 17 | US4 완료 화면 뒤로가기 | 분석 완료 화면에서 시스템 뒤로가기 | 무반응 |
| 18 | Edge 오프라인 | 기내 모드로 `사용법 보기` 진입 → 영상 링크 화면 | 추천 영역에 안내+`다시 시도`, 말풍선 없음, `건너뛰기` 가능. 온라인 복구 후 `다시 시도`로 목록 표시 |
| 19 | Edge 앱 초기화 | 마이페이지 → `앱 초기화` → `초기화` (테스트 계정) | 온보딩 시작 화면 + `앱 초기화가 완료되었습니다.` 토스트, 뒤로가기로 마이페이지 복귀 불가, 약관 재동의 필요, 지도 설정 `기본` |
| 20 | Edge 튜토리얼 중 알림 시트 | 알림 권한 미허용 기기에서 `사용법 보기` → 영상 링크 화면 진입 | 알림 안내 시트가 뜨지 않고 3단계 말풍선만 표시. 온보딩 완료 후 다음 진입 때 시트 노출 |

## API 스모크 (curl) — 사전 분석·자동 생성 전제 검증

```bash
TOKEN=$(curl -s -X POST https://linktrip.cloud/api/auth/login \
  -H 'Content-Type: application/json' -H "Idempotency-Key: $(uuidgen)" \
  -d '{"serialNumber":"qa-onboarding-001"}' | jq -r .data.accessToken)

# 추천 영상 = category 전체 목록 상위 8개
curl -s https://linktrip.cloud/api/video/discover/category -H "Authorization: Bearer $TOKEN" | jq '.data.videos[:8] | map({videoId, videoUrl, title})'

# 사전 분석된 URL이면 200 + COMPLETED 여야 한다 (202면 사전 분석 안 됨)
curl -s -o /dev/null -w '%{http_code}\n' -X POST https://linktrip.cloud/api/video/analyze \
  -H "Authorization: Bearer $TOKEN" -H "Idempotency-Key: $(uuidgen)" -H 'Content-Type: application/json' \
  -d '{"youtubeUrl":"https://youtu.be/<videoId>"}'

# 직후 이 회원의 여행 계획에 자동 생성됐는지 (videoAnalysisTaskId 매칭)
curl -s https://linktrip.cloud/api/trip-plans -H "Authorization: Bearer $TOKEN" | jq '.data.tripPlans | map({id, title, videoAnalysisTaskId, youtubeUrl})'
```

두 번째 호출이 202이거나 세 번째 목록에 항목이 없으면 서버 측 사전 분석·자동 생성 이슈로 등록한다([contracts/video-analyze-usage.md](contracts/video-analyze-usage.md)).
