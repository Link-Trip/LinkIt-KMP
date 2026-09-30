# Data Model: 온보딩 플로우

**Date**: 2026-09-18 | **Spec**: [spec.md](spec.md) | **Research**: [research.md](research.md)

## 1. 도메인 모델 (domain/model)

### onboarding/TutorialStep
| 값 | 화면 | 안내 대상 | 다음 단계 조건 |
|---|---|---|---|
| `CREATE_BUTTON` | 지도 메인 | `일정 생성` FAB, 말풍선 `일정 생성 버튼을 선택해보세요` | FAB 탭 |
| `VIDEO_LINK_OPTION` | 지도 메인(메뉴 열림) | `영상 링크로 만들기`, 말풍선 `"영상 링크로 만들기" 를 선택해보세요` | 항목 탭 → 영상 링크 화면 이동 |
| `COPY_LINK` | 영상 링크로 만들기 | 첫 추천 영상 `링크복사`, 말풍선 `링크를 복사해요` + 포인터 | `링크복사` 탭 |
| `PASTE_LINK` | 영상 링크로 만들기 | `복사한 링크 붙여넣기` 칩, 말풍선 `붙여넣어요~` + 포인터 | 칩 탭 또는 클립보드 토스트 탭 |
| `FREE` | 영상 링크로 만들기 | 없음(자유 조작, 온보딩 규칙은 유지) | `일정 생성하기` 성공 → 분석 완료 |
| `null` | — | 튜토리얼 아님 | — |

- 값은 `OnboardingRepository.observeTutorialStep()`로 관찰하며 프로세스 메모리에만 존재한다.
- `null`이 아닌 동안 지도·일정 화면은 "온보딩 모드"(건너뛰기 노출, 추천 링크만 허용)로 동작한다.

### onboarding/OnboardingCompletion
| 값 | 기록 시점 |
|---|---|
| `SKIPPED_AT_START` | 온보딩 시작 화면 `바로 시작하기` + 약관 동의 |
| `SKIPPED_IN_TUTORIAL` | 튜토리얼 화면 `건너뛰기` |
| `TUTORIAL_FINISHED` | 분석 완료 화면 `생성된 일정 확인하기` |

`CompleteOnboardingUseCase(reason)`의 파라미터. DataStore에는 완료 여부(Boolean)만 저장하고 사유는 로그·분석용으로만 쓴다.

### terms/TermsDocumentType, terms/TermsDocument (기존, #48 도입 — 변경 없음)
| `TermsDocumentType` | 제목 | 온보딩 시트 항목 |
|---|---|---|
| `SERVICE` | 서비스 이용약관 | `[필수] 서비스 이용약관 동의` |
| `PRIVACY` | 개인정보 처리방침 | `[필수] 개인정보 수집·이용 동의` |
| `OPEN_SOURCE` | 오픈소스 라이센스 고지 | (마이페이지만) |
| `LOCATION` | 위치기반 서비스 이용약관 | (마이페이지만) |

`TermsDocument(type, title, url)`. URL은 data `TermsRepositoryImpl` 상수(운영 확정 전 자리 표시 `https://linktrip.cloud/terms/{service|privacy|oss|location}`)이되, 서버 약관 목록을 조회한 뒤에는 `SERVICE`·`PRIVACY`의 주소가 서버 `detailUrl`로 대체된다.

### terms/TermsAgreement (2026-09-22 추가 — 서버 `GET /terms`)
`TermsAgreement(type, title, required, version, detailUrl, agreed)`. 서버가 노출 대상으로 정한 약관(현재 `SERVICE`·`PRIVACY`)과 로그인한 회원의 동의 여부. 클라이언트가 모르는 `type`은 매퍼가 버린다.
- `CheckTermsAgreementUseCase()`: 로그인 → 조회 → `required && !agreed`가 하나라도 있으면 true(동의 시트 필요). 서버가 모두 동의됨인데 로컬 `terms_agreed_at`이 없으면 로컬을 맞춘다
- `AgreeTermsUseCase(types)`: 로그인 → `POST /terms/agreement` → 성공 시에만 로컬 `terms_agreed_at` 기록. 실패는 전파(시트 재활성화)
- 개정으로 서버가 `agreed=false`를 주면 온보딩 완료 회원도 인트로 위에 닫을 수 없는 재동의 시트(`pendingAction=RESUME`, `TermsSheetState.dismissible=false`)가 뜬다

### video/DiscoverVideo (기존 재사용)
추천 영상 카드 = `DiscoverVideo(videoId, videoUrl, title, thumbnailUrl, viewCount, …)`. 온보딩은 `videoUrl`(복사 대상)과 `videoId`(추천 여부 판정 키)를 쓴다.

### tripplan/TripPlanSummary (기존 재사용) + 로컬 확인 상태
`TripPlanSummary(id, title, videoAnalysisTaskId, youtubeUrl, itemCount, nights, days, hashtags, …)`. 확인 여부는 서버 필드가 아니라 `TripPlanRepository.observeUncheckedTripPlanIds()`의 집합 포함 여부로 판단한다.

### util/YouTubeUrl (신규, 기존 UseCase에서 추출)
`normalize(url): String`, `videoIdOrNull(url): String?` — `youtu.be/{id}`, `youtube.com/watch?v={id}`, `youtube.com/{shorts|live|embed}/{id}` 지원. 추천 여부 판정은 `videoIdOrNull` 동일성으로 한다(스펙 FR-026 "표기 차이는 같은 링크").

### usecase 결과 타입
```kotlin
sealed interface CreateOnboardingScheduleResult {
    data object InvalidFormat : CreateOnboardingScheduleResult      // 영상 링크 형식 아님 → "유효한 영상 링크가 아닙니다"
    data object NotRecommended : CreateOnboardingScheduleResult     // 추천 영상 아님 → "온보딩에서는 추천 영상 링크만 사용할 수 있어요"
    data object NotReady : CreateOnboardingScheduleResult           // 202 또는 일정 매칭 실패 → "미리 준비된 일정을 가져오지 못했어요"
    data class Created(val tripPlanId: String, val title: String) : CreateOnboardingScheduleResult
}
```

## 2. 로컬 저장 (DataStore Preferences, 파일 `linkit.preferences_pb`)

| 키 | 타입 | 기본값 | 쓰는 곳 | 앱 초기화 시 |
|---|---|---|---|---|
| `access_token`, `device_id` (기존) | String | 없음 | AuthLocalDataSource | 토큰만 `AuthRepository.logout()`으로 폐기, `device_id` 유지(#48) |
| `map_display_type`, `notification_prompted` (기존 #48) | String / Boolean | DEFAULT / false | `AppSettingsLocalDataSource` | 제거(`AppSettingsLocalDataSource.clearAll()`) |
| `onboarding_completed` (기존 #48 → 이관) | Boolean | false | `OnboardingLocalDataSource` — `CompleteOnboardingUseCase` (#48의 `AppSettingsLocalDataSource`에서 이관) | 제거 |
| `terms_agreed_at` | Long(epoch millis) | 없음(= 미동의) | `OnboardingLocalDataSource` — 약관 시트 `동의하고 시작하기` | 제거 |
| `unchecked_trip_plan_ids` | Set<String> | 빈 집합 | `TripPlanLocalDataSource` — 생성 시 추가, 상세 진입 시 제거 | 제거 |
| (메모리) `tutorialStep` | `TutorialStep?` | null | `OnboardingLocalDataSourceImpl.MutableStateFlow` | null |

`OnboardingLocalDataSource.clearAll()`은 `onboarding_completed`·`terms_agreed_at` 제거 + 메모리 null. `TripPlanLocalDataSource.clearUnchecked()`는 집합 제거. #48 `ResetAppUseCase`(인증 → 탈퇴 → `appSettings.clearAll()` → `logout()`)의 로컬 초기화 단계에 둘을 추가한다(research R9).

## 3. 서버 리소스 (모두 기존 API)

| 리소스 | 메서드 | 상태 | 용도 |
|---|---|---|---|
| `/video/discover/category` | GET | 기존 | 추천 영상 목록. 파라미터 없이 호출(전체 영상) 후 상위 8개 사용. 사전 분석은 운영 보장 |
| `/video/analyze` | POST | 기존 | 추천 링크 분석 요청. 사전 분석된 URL이면 200 `COMPLETED` 인라인 — [contracts/video-analyze-usage.md](contracts/video-analyze-usage.md) |
| `/video/schedule/{taskId}` | GET | 기존 | 일정 매칭 실패 시 1회 재조회 |
| `/trip-plans` | GET(cursor) | 기존 | 자동 생성된 여행 계획에서 `videoAnalysisTaskId` 매칭 |
| `/auth/login` | POST | 기존 | `ensureAuthenticated()` |

## 4. UI 상태

### 4.1 feature/intro — IntroUiState
| 필드 | 타입 | 초기값 | 근거 |
|---|---|---|---|
| `phase` | `SPLASH / START` | SPLASH | Intro 라우트 / OnboardingStart 라우트 |
| `pendingAction` | `TUTORIAL / SKIP / null` | null | 시작 화면에서 누른 버튼(약관 동의 후 목적지 결정, FR-010) |
| `termsSheet` | `TermsSheetState?` | null | null = 닫힘 (FR-005~011) |

**TermsSheetState**
| 필드 | 타입 | 초기값 | 규칙 |
|---|---|---|---|
| `serviceAgreed` | Boolean | false | 개별 토글 |
| `privacyAgreed` | Boolean | false | 개별 토글 |
| `allAgreed` (파생) | Boolean | — | `serviceAgreed && privacyAgreed` (FR-007) |
| `canStart` (파생) | Boolean | — | `allAgreed && !isSubmitting` (FR-008) |
| `isSubmitting` | Boolean | false | 저장 중 중복 탭 방지 |

**IntroIntent**: `SplashFinished`, `TapSeeHowTo`(사용법 보기), `TapStartNow`(바로 시작), `ToggleAllTerms`, `ToggleServiceTerms`, `TogglePrivacyTerms`, `OpenTermsDetail(type)`, `DismissTermsSheet`, `AgreeAndStart`

**IntroSideEffect**: `NavigateToOnboardingStart`, `NavigateToTermsDetail(type)`, `NavigateToHome`

`pendingAction`은 `TUTORIAL / SKIP / RESUME(개정 약관 재동의)`. `TermsSheetState.dismissible`(기본 true)이 false면 끌어 내리기·바깥 탭이 무시된다.

앱 초기화 완료 토스트 플래그(`showResetCompletedToast`)는 `IntroActivity`의 `EXTRA_SHOW_RESET_TOAST`(#48)에서 오는 UI 입력이라 `IntroUiState`에 두지 않고 `IntroNavDisplay` → `OnboardingStartScreen` 파라미터로 전달한다.

### 4.2 feature/map — MapUiState 추가 필드
| 필드 | 타입 | 초기값 | 근거 |
|---|---|---|---|
| `tutorialStep` | `TutorialStep?` | null | Repository 구독. `CREATE_BUTTON/VIDEO_LINK_OPTION`에서 코치마크·건너뛰기 표시, 생성 메뉴 나머지 비활성(FR-016, FR-017) |
| `isOnboardingMode` (파생) | Boolean | — | `tutorialStep != null` |
| `uncheckedScheduleIds` | Set<String> | 빈 집합 | Repository 구독. `MapScheduleUiModel.isUnchecked` 계산(FR-030) |

`MapScheduleUiModel`에 `isUnchecked: Boolean` 추가. 목록 정렬은 기존(최신순)이라 신규 일정이 맨 위에 온다(FR-029).

**MapIntent 추가**: `SkipOnboarding`, `TutorialTargetPositioned(target: TutorialTarget, bounds: Rect)`(코치마크 좌표), `RefreshSchedules`(내부/복귀용). 기존 `ToggleCreateMenu`·`onCreateFromVideo` 경로에서 단계 전이를 수행한다.

### 4.3 feature/schedule — ScheduleUiState 추가 필드
| 필드 | 타입 | 초기값 | 근거 |
|---|---|---|---|
| `recommendedVideos` | `RecommendedVideosState` = `Loading / Content(List<DiscoverVideo>) / Error` | Loading | FR-019(로딩·실패·재시도) |
| `tutorialStep` | `TutorialStep?` | null | `COPY_LINK/PASTE_LINK`에서 코치마크, null 아니면 건너뛰기·온보딩 규칙 |
| `isGuideVisible` | Boolean | false | 진입 후 `TutorialGuideDelayMillis` 지연 뒤 true(FR-020) |
| `hasClipboardText` | Boolean | false | `복사한 링크 붙여넣기` 활성 조건(FR-022) |
| `clipboardToastUrl` | String? | null | 앱 내 `링크복사` 직후만 세팅(FR-021) |
| `errorToast` | String? | null | `유효한 영상 링크가 아닙니다` / 추천 전용 안내 / 준비 실패 안내(3초 후 자동 해제) |
| `canCreate` (파생, 기존 수정) | Boolean | — | `videoLink.isNotBlank() && videoLinkError == null && !isSubmittingVideoLink` 유지. 온보딩 오류 토스트 후 `videoLinkError`를 세팅해 비활성(FR-025, FR-026) |

**ScheduleIntent 추가**: `LoadRecommendedVideos`, `CopyRecommendedLink(url)`, `ClipboardAvailabilityChanged(hasText)`, `PasteFromClipboard(text)`, `ApplyClipboardToast`, `DismissClipboardToast`, `DismissErrorToast`, `GuideDelayElapsed`, `SkipOnboarding`, `ConfirmOnboardingSchedule`

**ScheduleSideEffect 추가**: `WriteClipboard(text)`, `NavigateToAnalysisComplete`, `FinishOnboarding`

**NotificationPromptViewModel(#48 기존) 추가 필드**: `isOnboardingMode: StateFlow<Boolean>`(`observeTutorialStep() != null`). `ScheduleNavDisplay`의 알림 안내 시트 조건에 `!isOnboardingMode`를 더한다(research R8).

## 5. 상태 전이

### 앱 실행 → 목적지
```
Intro(SPLASH) ──SplashFinished──▶ isOnboardingCompleted()?
   true  → NavigateToHome (tutorialStep 유지 null)
   false → NavigateToOnboardingStart
   (앱 초기화 직후 진입: EXTRA_SHOW_RESET_TOAST=true → 시작 화면에 `앱 초기화가 완료되었습니다.` 토스트)
```

### 온보딩 시작 → 약관 → 목적지
```
START ──TapSeeHowTo──▶ pendingAction=TUTORIAL, termsSheet=초기
START ──TapStartNow──▶ pendingAction=SKIP,     termsSheet=초기
termsSheet ──DismissTermsSheet(끌어내림/바깥)──▶ termsSheet=null, pendingAction=null (기록 없음)
termsSheet ──OpenTermsDetail(type)──▶ NavigateToTermsDetail (체크 상태 유지)
termsSheet(canStart) ──AgreeAndStart──▶ setTermsAgreed(now)
   pendingAction=TUTORIAL → setTutorialStep(CREATE_BUTTON) → NavigateToHome
   pendingAction=SKIP     → CompleteOnboardingUseCase(SKIPPED_AT_START) → NavigateToHome
```

### 튜토리얼 단계 (Repository 값)
```
CREATE_BUTTON ──Map: FAB 탭──▶ VIDEO_LINK_OPTION
VIDEO_LINK_OPTION ──Map: 영상 링크로 만들기 탭──▶ COPY_LINK (+ ScheduleActivity 진입)
COPY_LINK ──Schedule: 링크복사 탭──▶ PASTE_LINK (+ 클립보드 쓰기, 토스트)
PASTE_LINK ──Schedule: 붙여넣기 칩/토스트 탭──▶ FREE (+ videoLink 채움)
FREE ──Schedule: 일정 생성하기 → Created──▶ (분석 완료 화면) ──생성된 일정 확인하기──▶ CompleteOnboardingUseCase(TUTORIAL_FINISHED) → null + finish()
any(non-null) ──건너뛰기──▶ CompleteOnboardingUseCase(SKIPPED_IN_TUTORIAL) → null (+ Schedule이면 finish())
```
프로세스 종료 시 값은 사라지고 `onboarding_completed=false`면 다음 실행에 SPLASH→START부터.

### 온보딩 일정 생성 (ScheduleViewModel, tutorialStep != null)
```
SubmitVideoLink ──▶ CreateOnboardingScheduleUseCase(videoLink, recommendedUrls)
   InvalidFormat  → videoLinkError=WRONG_FORMAT, errorToast="유효한 영상 링크가 아닙니다"
   NotRecommended → videoLinkError=INVALID_LINK, errorToast="온보딩에서는 추천 영상 링크만 사용할 수 있어요"
   NotReady       → errorToast="미리 준비된 일정을 가져오지 못했어요. 다시 시도해주세요." (버튼 활성 유지)
   Created        → NavigateToAnalysisComplete
UpdateVideoLink → videoLinkError=null (재활성)
```

### 확인전/확인후
```
Created(tripPlanId) → markTripPlanUnchecked(id) → uncheckedScheduleIds ∋ id → 카드 강조 배경
Map: SelectSchedule/onOpenSchedule(id) → markTripPlanChecked(id) → 집합에서 제거 → 일반 배경 (영구)
```

### 추천 영상 목록
```
진입 → Loading → getOnboardingVideos()   // category 전체 상위 8개
   성공(비어있지 않음) → Content + (tutorialStep==COPY_LINK면 지연 후 isGuideVisible=true)
   성공(빈 목록)/실패 → Error(안내 + 다시 시도), 코치마크 시작 안 함, 건너뛰기만 가능
```
