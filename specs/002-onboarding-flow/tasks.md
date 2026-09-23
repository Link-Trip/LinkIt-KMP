# Tasks: 온보딩 플로우

**Input**: Design documents from `/specs/002-onboarding-flow/`

**Prerequisites**: plan.md, spec.md, research.md (R1~R12), data-model.md, contracts/domain-contracts.md, contracts/video-analyze-usage.md, quickstart.md

**Tests**: 스펙 SC와 quickstart 자동 검증에 따라 UseCase 단위 테스트(domain commonTest), data DataStore·Repository 테스트, ViewModel 테스트(Robolectric), Roborazzi 스크린샷 테스트를 포함한다.

**Organization**: 유저 스토리별 Phase로 묶되, 각 태스크에 `[UI]`(UI 구현) 또는 `[API]`(API·데이터 연동) 그룹을 표기한다. 두 그룹은 GitHub 하위 이슈로 나뉘어 관리된다(부모 이슈 #47). 추천 영상은 기존 `GET /video/discover/category`(파라미터 없음 = 전체) 상위 8개를 사용하며 신규 서버 API는 없다.

**전제(2026-09-21)**: 001 마이페이지 데이터 연동(PR #48)이 develop에 머지되어 `AppSettingsRepository`(`onboarding_completed` 포함)·`TermsRepository`·`Terms`/`TermsDetail` 라우트·`TermsDetailScreen`·`PlatformWebView`·`IntroNavigator`·`ResetAppUseCase`가 이미 있다. 겹치는 항목은 research R9에 따라 이관·이동·확장한다(신규 작성 아님).

> **2026-09-21 UI 구현(#49) 진행 기록**: `[UI]` 태스크는 모두 완료([x]). `[API]` 태스크는 착수하지 않았으며, UI가 컴파일·동작하도록 MVI 계약 타입과 ViewModel 스켈레톤만 다음과 같이 선반영했다(해당 API 태스크에서 저장소·UseCase로 대체, 코드의 `TODO(#50 …)` 참조).
> - T001 일부: `domain/model/onboarding/TutorialStep.kt`(enum)만 추가. `OnboardingCompletion`·`CreateOnboardingScheduleResult`는 미작성
> - T018 전체: `IntroUiState/TermsSheetState/IntroIntent/IntroSideEffect` 작성. T019·T026 중 화면 상태 전이(시트 열기·토글·닫기·`isSubmitting`)만 `IntroViewModel`에 구현, `SplashFinished`는 항상 온보딩 시작으로 이동
> - T031·T035 중 상태·Intent·SideEffect 추가분 작성(`MapUiState.tutorialStep/uncheckedScheduleIds`, `MapScheduleUiModel.isUnchecked`, `ScheduleUiState.recommendedVideos/…`, `ScheduleSideEffect.WriteClipboard/NavigateToAnalysisComplete/FinishOnboarding`). `TutorialTargetPositioned` Intent는 두지 않고 코치마크 좌표를 화면 로컬 상태로 처리
> - `MapViewModel`·`ScheduleViewModel`은 새 Intent를 로컬 상태 전이로만 처리(단계 전이·`FinishOnboarding` 발행). `LoadRecommendedVideos`는 Loading 유지
> - T042 중 `NotificationPromptViewModel.isOnboardingMode`(OnboardingRepository 구독)는 API 태스크로 이관
> - 분석 완료 화면 뒤로가기 차단은 `core/ui/platform/BlockSystemBack` expect/actual로 구현(schedule은 designsystem 내부 `CoachMarkBackHandler`를 쓸 수 없음)
> - T054(기기 수동 검증)·T055(신규 문구 디자인 확인)는 코드 외 작업으로 미완료

> **2026-09-21 API·데이터 연동(#50) 진행 기록**: `[API]` 태스크를 같은 브랜치에서 이어서 반영했다. 위 선반영 스켈레톤(`TODO(#50 …)`)은 모두 저장소·UseCase 호출로 대체됐다.
> - domain: `OnboardingRepository`, `TripPlanRepository` 확인전 4메서드, `VideoRepository.getOnboardingVideos()`, `util/YouTubeUrl`, `OnboardingCompletion`, `CreateOnboardingScheduleResult`, `CompleteOnboardingUseCase`, `CreateOnboardingScheduleUseCase`, `ResetAppUseCase` 확장. `AppSettingsRepository`에서 온보딩 메서드 제거
> - data: `OnboardingLocalDataSource(Impl, @SingleIn)`, `TripPlanLocalDataSource(Impl)`, `OnboardingRepositoryImpl`, `TripPlanRepositoryImpl`(local 조합, 삭제 시 확인전 집합에서도 제거), `VideoRepositoryImpl.getOnboardingVideos()`. DataStore 테스트는 임시 파일이 필요해 `data/src/androidUnitTest`에 두었다
> - feature: `IntroViewModel`(완료 여부 분기·약관 동의 시각·단계 세팅), `MapViewModel`(단계·확인전 집합 구독, `SelectCreateFromVideo`·`ScheduleOpened` Intent 추가, 단계 null 전이·새 id 유입 시 목록 재조회), `ScheduleViewModel`(추천 영상 로드, 단계 전이, 온보딩 생성 분기, 완료 기록), `NotificationPromptViewModel.isOnboardingMode`(`Boolean?`, 읽기 전 시트 미표시). `MapIntent.TutorialTargetPositioned`는 UI 단계 결정대로 두지 않았다
> - iOS: `IosAppGraph`에 `OnboardingLocalDataSourceImpl`·`TripPlanLocalDataSourceImpl`·`OnboardingRepositoryImpl` `@Binds` 추가
> - T051: `:app-shared:linkDebugFrameworkIosSimulatorArm64` 통과(`DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer` 지정). ViewModel MissingBinding 없음, `HomeViewController` 시그니처 변경 없음. Swift 진입(`iOSApp.swift`) 배선은 별도 이슈로 남긴다
> - T000(리베이스)은 현재 브랜치가 `feature/#49-onboarding_ui`라 수행하지 않았다. T053의 `domain/README.md` UseCase 목록은 문서에 해당 목록이 없어 생략했다
> - 2026-09-21 Figma 대조 결과는 [figma-diff.md](./figma-diff.md)에 정리. 우선순위 높음 1건(튜토리얼 지도 화면 바텀시트 접힘), 중간 5건(건너뛰기 버튼·코치마크 타이포·링크복사 버튼·빈 상태 링크·확인전 그라데이션), 낮음 항목을 같은 날 반영하고 골든을 갱신했다. 공용 `건너뛰기`는 `core/ui/.../onboarding/OnboardingSkipButton.kt`. 디자인 확인 항목은 문서 하단 참조

> **2026-09-22 약관 API 연동 기록**: 서버에 `GET /terms`(약관 목록+동의 여부, bearer 필수)·`POST /terms/agreement`(`{types}`, Idempotency-Key 필수, 멱등)가 추가되어 온보딩 약관 흐름을 서버 기준으로 바꿨다.
> - domain: `model/terms/TermsAgreement`, `TermsRepository.getTermsAgreements()/agreeTerms(types)`, `CheckTermsAgreementUseCase`(로그인 → 조회 → 필수 미동의 여부, 서버 모두 동의면 로컬 `terms_agreed_at` 보정), `AgreeTermsUseCase`(로그인 → 서버 기록 → 성공 시 로컬 기록), `LinkTripErrorCode`에 `BAD_REQUEST_TERMS_REQUIRED`·`BAD_REQUEST_TERMS_TYPE`
> - data: `api/TermsApi`, `dto/terms/{TermsResponse,TermsListResponse,AgreeTermsRequest}`, `mapper/TermsMapper`(미지원 type 제외), `datasource/terms/TermsRemoteDataSource(Impl)`, `TermsRepositoryImpl`(`@SingleIn`, 서버 `detailUrl`로 상수 주소 덮어쓰기). iOS `IosAppGraph`에 `TermsRemoteDataSourceImpl` `@Binds`
> - feature/intro: `SplashFinished`가 로그인(디바이스 ID 전송) → `GET /terms` 순으로 진행한다. **약관 동의보다 로그인이 먼저**가 됐다(서버 설계 전제, 2026-09-22 합의). 온보딩 미완료 → 시작 화면(서버가 이미 모두 동의면 버튼 탭 시 시트 생략), 완료 + 필수 미동의(개정) → 인트로 위 닫을 수 없는 재동의 시트(`pendingAction=RESUME`) → 동의 후 Home, 완료 + 동의됨 → Home. 서버 확인 실패(오프라인)는 로컬 `terms_agreed_at`으로 폴백. `AgreeAndStart`는 `AgreeTermsUseCase`로 서버 기록, 실패 시 시트 재활성화(기존 동작)
> - 시트 항목은 여전히 고정 2행(`SERVICE`·`PRIVACY`)이다. 서버 목록 기반 동적 렌더링은 Figma 시안이 고정 2행이라 보류했고, 서버가 필수 약관을 추가하면 `BAD_REQUEST_TERMS_REQUIRED`로 드러난다
> - 검증: `:app-android:assembleDebug`, domain·data·intro·schedule·map 단위 테스트 261건 통과, `:app-shared:linkDebugFrameworkIosSimulatorArm64` 통과

## Format: `[ID] [P?] [Story] [Group] Description`

- **[P]**: 다른 파일·의존성 없음, 병렬 가능
- **[Story]**: US1 최초 진입·시작·건너뛰기 / US2 약관 동의 / US3 체험형 튜토리얼 / US4 생성된 첫 일정 확인
- **[Group]**: `[UI]` = UI 구현 하위 이슈(화면·컴포넌트·라우트·스크린샷), `[API]` = API·데이터 연동 하위 이슈(domain·data·ViewModel 로직·DI)

## Path Conventions

- domain: `domain/src/commonMain/kotlin/com/linkit/company/domain/` (테스트 `domain/src/commonTest/...`)
- data: `data/src/commonMain/kotlin/com/linkit/company/data/` (테스트 `data/src/commonTest/...`), iOS 그래프 `app-shared/src/iosMain/kotlin/com/linkit/company/IosAppGraph.kt`
- designsystem: `core/designsystem/src/{commonMain,androidMain,androidUnitTest}/kotlin/com/linkit/company/core/designsystem/`
- core/ui: `core/ui/src/{commonMain,androidMain,iosMain}/kotlin/com/linkit/company/core/ui/`
- 라우트: `core/navigation/src/commonMain/kotlin/com/linkit/company/core/navigation/LinkItRoute.kt`
- feature/intro: `feature/intro/src/{commonMain,androidMain,iosMain,androidUnitTest}/kotlin/com/linkit/company/feature/intro/`
- feature/map: `feature/map/src/{commonMain,androidUnitTest}/kotlin/com/linkit/company/feature/map/main/`
- feature/schedule: `feature/schedule/src/{commonMain,androidMain,androidUnitTest}/kotlin/com/linkit/company/feature/schedule/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 모든 스토리가 공유하는 도메인 타입·유틸·라우트·모듈 의존성

- [ ] T000 [API] 작업 브랜치 `feature/#47-onboarding_flow`를 develop(`7e9eafd`, PR #48 머지)에 리베이스하고 `./gradlew :app-android:assembleDebug` 통과 확인. 이후 모든 태스크는 #48 코드를 전제로 한다
- [x] T001 [API] domain 모델 추가: `model/onboarding/TutorialStep.kt`(enum `CREATE_BUTTON, VIDEO_LINK_OPTION, COPY_LINK, PASTE_LINK, FREE`), `model/onboarding/OnboardingCompletion.kt`(enum `SKIPPED_AT_START, SKIPPED_IN_TUTORIAL, TUTORIAL_FINISHED`), (`model/terms/TermsDocument(Type)`은 #48 기존 재사용, 생성 없음), `usecase/CreateOnboardingScheduleResult.kt`(`InvalidFormat / NotRecommended / NotReady / Created(tripPlanId, title)`) (data-model.md §1)
- [x] T002 [P] [API] `domain/util/YouTubeUrl.kt` 신설(`normalize(url)`, `videoIdOrNull(url)`): `usecase/StartVideoScheduleCreationUseCase.kt`의 private 정규화 로직을 이동해 공유하고 `domain/src/commonTest/.../util/YouTubeUrlTest.kt`(짧은 주소·추가 매개변수·잘못된 형식) 작성
- [x] T003 [P] [UI] `LinkItRoute.kt`에 `LinkItNavKey.OnboardingStart`(data object) 추가 및 `LinkItSavedStateConfiguration` polymorphic serializer 등록. `Terms`·`TermsDetail(type)`은 #48에 존재하므로 건드리지 않음 (contracts/domain-contracts.md §5)
- [x] T004 [P] [UI] 모듈 의존성: `core/ui/build.gradle.kts`에 `core:designsystem`·`domain`·metrox viewmodel(+compose) 추가, `feature/intro/build.gradle.kts`에 metrox viewmodel(+compose)·`jetbrainsNavigation3` 번들·`core:ui`·`core:navigation` 추가, `feature/map`·`feature/schedule`이 `core:ui`를 참조하도록 추가 (plan.md Source Code)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 온보딩 상태 저장소·약관 저장소·확인전 집합·추천 영상 조회·디자인 시스템 컴포넌트·공유 약관 화면. US1~US4가 의존한다

- [x] T005 [API] `domain/repository/OnboardingRepository.kt` 인터페이스: `isOnboardingCompleted()`(기본 false), `setOnboardingCompleted(completed)`, `isTermsAgreed()`(`terms_agreed_at` 존재 여부), `setTermsAgreed(agreedAtEpochMillis: Long)`, `observeTutorialStep(): Flow<TutorialStep?>`(메모리, 초기 null), `setTutorialStep(step)`, `clearAll()`. 동시에 `AppSettingsRepository.kt`(#48)에서 `isOnboardingCompleted/setOnboardingCompleted`를 제거하고 KDoc·`clearAll` 설명을 지도 설정·알림 안내 이력으로 축소 (contracts §1, research R9)
- [x] T006 [API] `data/datasource/onboarding/OnboardingLocalDataSource.kt` + `OnboardingLocalDataSourceImpl.kt`: DataStore 키 `booleanPreferencesKey("onboarding_completed")`, `longPreferencesKey("terms_agreed_at")` + 튜토리얼 단계 `MutableStateFlow<String?>`(enum name). `@Inject @ContributesBinding(DataScope::class) @SingleIn(DataScope::class)` 필수(두 Activity가 같은 인스턴스 공유). `clearAll()`은 키 2개 제거 + 단계 null. `AppSettingsLocalDataSource(Impl).kt`(#48)에서 `isOnboardingCompleted/saveOnboardingCompleted`·`KEY_ONBOARDING_COMPLETED`를 제거하고 `clearAll()`은 `map_display_type`·`notification_prompted` 2키만 제거
- [x] T007 [API] `data/repository/OnboardingRepositoryImpl.kt`(`@ContributesBinding(DataScope::class)`, String↔`TutorialStep` 변환) + `data/src/commonTest/.../OnboardingLocalDataSourceTest.kt`(DataStore 임시 파일: 저장·읽기·clearAll·단계 Flow 전이). `AppSettingsRepositoryImpl.kt`에서 온보딩 메서드 제거, `domain/src/commonTest/.../fake/MyPageFakes.kt`·`feature/map/src/androidUnitTest/.../testing/FakeAppSettingsRepository.kt`의 온보딩 메서드 삭제
- [x] T008 [P] [API] `domain/repository/TripPlanRepository.kt`에 `observeUncheckedTripPlanIds(): Flow<Set<String>>`, `markTripPlanUnchecked(id)`, `markTripPlanChecked(id)`, `clearUncheckedTripPlans()` 추가. `data/datasource/tripplan/TripPlanLocalDataSource.kt`+`Impl`(DataStore `stringSetPreferencesKey("unchecked_trip_plan_ids")`, `@ContributesBinding(DataScope::class)`), `data/repository/TripPlanRepositoryImpl.kt`에 local 주입·조합, `data/src/commonTest/.../TripPlanLocalDataSourceTest.kt`(add/remove/clear)
- [x] T009 [API] ~~`TermsRepository`·`TermsRepositoryImpl` 도입~~ — #48로 이미 존재(`getTermsDocuments()` 고정 순서 4종, URL 자리 표시 `https://linktrip.cloud/terms/{service|privacy|oss|location}`). 변경 없이 재사용, `getTermsDocument(type)`는 추가하지 않음 (research R7·R9)
- [x] T010 [P] [API] `domain/repository/VideoRepository.kt`에 `getOnboardingVideos(): List<DiscoverVideo>` 추가, `data/repository/VideoRepositoryImpl.kt`에서 `videoRemoteDataSource.getDiscoverVideosByCategory(country = null, region = null)`(전체 목록) 결과를 `take(ONBOARDING_VIDEO_COUNT /* = 8 */)`로 잘라 도메인 매핑. `VideoApi`·`VideoRemoteDataSource`·DTO 변경 없음. `data/src/commonTest/.../VideoRepositoryImplTest.kt`(10개 → 8개 절단, 8개 미만은 그대로, 파라미터 null 전달 검증) (research R3)
- [x] T011 [API] `IosAppGraph.kt`에 `OnboardingLocalDataSourceImpl`, `TripPlanLocalDataSourceImpl`, `OnboardingRepositoryImpl` `@Binds` 3개 추가(`TermsRepositoryImpl`·`AppSettings*`는 #48에 등록됨) (contracts §8, docs/METRO_INSTRUCTION.md)
- [x] T056 [API] `domain/usecase/ResetAppUseCase.kt`(#48)에 `OnboardingRepository`·`TripPlanRepository` 주입, 로컬 초기화 단계를 `appSettingsRepository.clearAll()` → `onboardingRepository.clearAll()` → `tripPlanRepository.clearUncheckedTripPlans()` → `authRepository.logout()`로 확장(원격 실패 시 로컬 미변경 유지) + `domain/src/commonTest/.../usecase/ResetAppUseCaseTest.kt`에 호출 순서·원격 실패 시 미호출 케이스 추가, `MyPageFakes.kt` Fake 확장 (research R9, contracts §2)
- [x] T012 [P] [UI] `core/designsystem/.../component/coachmark/LinkItCoachMark.kt`: 전체 화면 오버레이, 반투명 스크림(`Canvas` + `BlendMode.Clear` 대상 컷아웃), 대상 위·아래 자동 배치 말풍선(semantic 색·radius 토큰), 선택적 `CoachMarkPointer`, 대상 밖 터치 소비·대상 영역 터치는 `onTargetClick` 위임, `targetBounds == null`이면 스크림만. `component/coachmark/CoachMarkBackHandler.kt` expect + androidMain(`BackHandler(enabled = true) {}`) + iosMain(no-op) (research R6, contracts §6)
- [x] T013 [P] [UI] `core/designsystem/.../component/sheet/LinkItModalBottomSheet.kt`(Material3 `ModalBottomSheet` 래퍼: 드래그 핸들·상단 radius·배경 토큰, `onDismissRequest`) + `component/checkbox/LinkItCheckbox.kt`(원형, `Utility.CircleCheckFill` 아이콘 토글, `enabled`) (research R7, contracts §6)
- [x] T014 [UI] designsystem 프리뷰·골든: `androidMain/.../component/preview/CoachMarkPreviews.kt`, `ModalBottomSheetPreviews.kt`, `CheckboxPreviews.kt` + `androidUnitTest/.../screenshot/CoachMarkScreenshotTest.kt`(컷아웃+말풍선 위/아래, 포인터), `CheckboxScreenshotTest.kt`(checked/unchecked/disabled) — `docs/COMPOSE_IMPLEMENTATION_GUIDE.md` 스크린샷 규칙
- [x] T015 [P] [UI] `feature/map/src/{commonMain,androidMain,iosMain}/.../mypage/platform/PlatformWebView*.kt`(#48)를 `core/ui/src/{commonMain,androidMain,iosMain}/.../terms/`로 이동하고 `internal` 제거. 시그니처 `(url, reloadToken, onLoadingChanged, onError, modifier)`와 외부 도메인 브라우저 위임 동작 유지 (contracts §4·§7)
- [x] T016 [UI] `feature/map/.../mypage/terms/TermsDetailScreen.kt`(`TermsDetailContent`·`TermsLoadState`)·`TermsViewModel.kt`(#48)를 `core/ui/.../terms/`로 이동하고 `MyPageStrings.TermsLoadError/TermsRetry`를 `core/ui/.../terms/TermsStrings.kt`로 이관. `feature/map`의 `TermsListScreen.kt`·`MapEntry.kt`·`MyPageStrings.kt` 참조 갱신, `TermsScreenshotTest.kt`의 상세 케이스를 `core/ui/src/androidUnitTest/.../terms/TermsDetailScreenshotTest.kt`로 이동(골든 재기록). 신규 `TermsDetailViewModel`은 만들지 않음 (research R7)

**Checkpoint**: 온보딩·약관·확인전 저장소가 Android/iOS 그래프에서 주입 가능, 컴포넌트 3종 골든 생성, 빌드 통과

---

## Phase 3: User Story 1 - 최초 진입과 온보딩 시작·건너뛰기 (Priority: P1) 🎯 MVP

**Goal**: 매 실행 인트로 애니메이션 뒤 온보딩 미완료면 온보딩 시작 화면, 완료면 메인 화면. `바로 시작하기`(약관 동의 후) → 메인 화면 + 완료 기록, 재실행 시 온보딩 재노출 없음 (FR-001, FR-002, FR-004, FR-010 일부)

**Independent Test**: quickstart 시나리오 1·2·3·4

- [x] T017 [US1] [API] `domain/usecase/CompleteOnboardingUseCase.kt`(`invoke(reason: OnboardingCompletion)`: `setOnboardingCompleted(true)` → `setTutorialStep(null)` 순서 고정) + `domain/src/commonTest/.../usecase/CompleteOnboardingUseCaseTest.kt`(호출 순서·세 가지 reason) (contracts §2)
- [x] T018 [US1] [API] `feature/intro/.../IntroUiState.kt`(`phase: SPLASH/START`, `pendingAction: TUTORIAL/SKIP/null`, `termsSheet: TermsSheetState?`), `IntroIntent.kt`(`SplashFinished, TapSeeHowTo, TapStartNow, ToggleAllTerms, ToggleServiceTerms, TogglePrivacyTerms, OpenTermsDetail(type), DismissTermsSheet, AgreeAndStart`), `IntroSideEffect.kt`(`NavigateToOnboardingStart, NavigateToTermsDetail(type), NavigateToHome`) (data-model.md §4.1)
- [x] T019 [US1] [API] `feature/intro/.../IntroViewModel.kt`(`@ContributesIntoMap`, `OnboardingRepository`·`CompleteOnboardingUseCase` 주입): `SplashFinished` → `isOnboardingCompleted()` true면 `NavigateToHome`, false면 `NavigateToOnboardingStart`; `TapStartNow`/`TapSeeHowTo` → `pendingAction` 세팅 + `termsSheet` 초기화; `AgreeAndStart`(SKIP) → `setTermsAgreed(now)` → `CompleteOnboardingUseCase(SKIPPED_AT_START)` → `NavigateToHome`; (TUTORIAL) → `setTermsAgreed(now)` → `setTutorialStep(CREATE_BUTTON)` → `NavigateToHome` (data-model.md §5)
- [x] T020 [US1] [UI] `feature/intro/.../OnboardingStartScreen.kt`: 캐릭터 일러스트, `LinkItBadge` `여행 영상, 이제 저장만 하지 마세요`, 제목 `보고있던 여행 영상을 내 일정으로 만들어보세요!`, 설명 `영상을 분석해 마커와 일정으로 정리해드릴게요 ⭐`, 강조 `LinkItButton` `30초 만에 사용법 보기`, 보조 버튼 `바로 시작하기`. 작은 화면에서 본문 스크롤·하단 버튼 고정. `uiState: IntroUiState, onIntent: (IntroIntent) -> Unit, showResetCompletedToast: Boolean = false` 시그니처. 앱 초기화 완료 토스트(`ResetCompletedToastMessage` = `앱 초기화가 완료되었습니다.`, `LinkItToast(Positive)`)를 하단에 표시 — #48 `IntroScreen`에서 이동 (FR-004, contracts §4·§9, research R5)
- [x] T021 [US1] [UI] `feature/intro/.../navigation/IntroNavDisplay.kt`(Navigation3 단일 백스택 `startRoute = Intro`, `entry<Intro>`·`entry<OnboardingStart>`·`entry<TermsDetail>`, `showResetCompletedToast`·`onNavigateToHome` 파라미터, SideEffect 수신으로 push/pop) + `IntroScreen.kt` 수정(`delay(IntroSplashDurationMillis)` 후 `onSplashFinished()`만 호출, 목적지 판단 제거, 백그라운드 복귀 시 중복 이동 방지, `showResetCompletedToast`·`ResetCompletedToastMessage`는 `OnboardingStartScreen`으로 이동) + `IntroConstants.kt`(`IntroSplashDurationMillis = 1500`) (research R5·R11)
- [x] T022 [US1] [UI] `feature/intro/.../IntroActivity.kt`를 METRO_INSTRUCTION 3단계로 개편(`MetroViewModelFactory` 주입, `LocalMetroViewModelFactory` 제공, `IntroNavDisplay(showResetCompletedToast = intent.getBooleanExtra(EXTRA_SHOW_RESET_TOAST, false), onNavigateToHome = { HomeNavigator.navigate(this); finish() })` — #48 extra 읽기 유지), 온보딩 시작 화면 시스템 뒤로가기 시 앱 종료(백스택 루트). `iosMain/.../IntroViewController.kt`를 `IntroViewController(appGraph: AppGraph, onComplete: () -> Unit, showResetCompletedToast: Boolean = false)`로 변경 (contracts §7)
- [x] T023 [US1] [API] `feature/intro/src/androidUnitTest/.../IntroViewModelTest.kt` 신규(Fake `OnboardingRepository`): 완료 여부별 `SplashFinished` 분기, `TapStartNow` → 시트 열림 + `pendingAction=SKIP`, `AgreeAndStart`(SKIP) 시 `setTermsAgreed` → 완료 기록 → `NavigateToHome`, (TUTORIAL) 시 단계 `CREATE_BUTTON` 세팅
- [x] T024 [P] [US1] [UI] `feature/intro/src/androidUnitTest/.../OnboardingStartScreenshotTest.kt`(시작 화면 골든) + 기존 `IntroScreenshotTest.kt`가 새 시그니처로 통과하도록 수정(초기화 토스트 골든은 `OnboardingStartScreenshotTest`로 이동)
- [x] T025 [P] [US1] [UI] `feature/map/.../MapScreen.kt` 저장 일정 빈 상태 문구 교체: `저장된 일정이 없어요`, `여행 영상 하나면 핑고가 일정으로 만들어드려요!`, `일정 생성하기` 버튼, `볼만한 영상 찾아보기` 링크 + `MapScreenshotTest.kt` 빈 상태 골든 (스펙 US1-4)

**Checkpoint**: 새로 설치 → 인트로 → 시작 화면 → `바로 시작하기` → (약관 시트는 US2에서 완성, 이 시점엔 임시로 즉시 통과 가능) → 메인 화면, 재실행 시 메인 화면 직행

---

## Phase 4: User Story 2 - 약관 동의 (Priority: P1)

**Goal**: 두 버튼 모두 약관 동의 바텀시트를 먼저 띄우고, 필수 2종 모두 체크 시에만 `동의하고 시작하기` 활성, `상세보기`로 약관 전문 열람 후 체크 상태 유지, 시트 닫기 시 기록 없음 (FR-005~FR-011)

**Independent Test**: quickstart 시나리오 5·6·7

- [x] T026 [US2] [API] `IntroViewModel` 약관 로직: `TermsSheetState(serviceAgreed=false, privacyAgreed=false, isSubmitting=false)` 초기 모두 해제, `ToggleAllTerms`는 두 항목 일괄 체크·해제, 개별 토글 시 파생 `allAgreed = serviceAgreed && privacyAgreed`, `canStart = allAgreed && !isSubmitting`, `DismissTermsSheet` → `termsSheet=null, pendingAction=null`(기록 없음), `OpenTermsDetail(type)` → `NavigateToTermsDetail(type)`(체크 상태 유지), `AgreeAndStart` 중복 탭 방지(`isSubmitting`) (FR-007, FR-008, FR-011, data-model.md §4.1)
- [x] T027 [US2] [UI] `feature/intro/.../TermsConsentSheet.kt`: `LinkItModalBottomSheet` + 제목 `핑고를 시작하려면 동의가 필요해요`, 설명 `필수 항목에 동의해야 서비스를 이용할 수 있어요`, `전체 동의` 행(`LinkItCheckbox`), `[필수] 서비스 이용약관 동의`·`[필수] 개인정보 수집·이용 동의` 행(각각 `LinkItCheckbox` + `상세보기` 텍스트 버튼), `동의하고 시작하기` `LinkItButton(enabled = canStart)`. 끌어 내리기·바깥 탭 → `DismissTermsSheet`. `OnboardingStartScreen`에서 `uiState.termsSheet != null`일 때 표시 (FR-006, contracts §4·§9)
- [x] T028 [US2] [UI] `IntroNavDisplay`에 `NavigateToTermsDetail(type)` → `TermsDetail(type)` push, `TermsDetailScreen(type, onBack = pop)` 연결. 상세에서 뒤로가기 시 시트·체크 상태가 유지되는지 확인(시트 상태는 ViewModel에 있으므로 재구성돼도 유지) (FR-009)
- [x] T029 [US2] [API] `IntroViewModelTest.kt`에 케이스 추가: 초기 전체 해제·버튼 비활성, `ToggleAllTerms` 왕복, 개별 2개 체크 시 `allAgreed` true·하나 해제 시 false, `DismissTermsSheet` 후 `pendingAction=null`·저장 호출 없음, `AgreeAndStart` 중복 호출 1회만 처리
- [x] T030 [P] [US2] [UI] `feature/intro/src/androidUnitTest/.../TermsConsentSheetScreenshotTest.kt`(미체크/전체 체크 골든, `TermsConsentSheetContent` 직접 호출)

**Checkpoint**: 시트 동기화·활성 조건·상세보기 왕복·닫기 무기록이 동작. 약관 동의 후 US1 경로 완성

---

## Phase 5: User Story 3 - 체험형 튜토리얼로 첫 일정 만들기 (Priority: P1)

**Goal**: 지도 코치마크 1·2단계 → 영상 링크 화면 코치마크 3·4단계(추천 영상 상위 8개, 클립보드 복사·붙여넣기) → 추천 링크로 `일정 생성하기` → 즉시 분석 완료 화면. 어느 단계든 `건너뛰기`, 어두운 영역·뒤로가기 무시 (FR-012~FR-027)

**Independent Test**: quickstart 시나리오 8~15·18

### 튜토리얼 단계 공유·지도 화면 (1·2단계)

- [x] T031 [US3] [API] `feature/map/.../MapUiState.kt`에 `tutorialStep: TutorialStep?`(초기 null), 파생 `isOnboardingMode`, `MapIntent.kt`에 `SkipOnboarding`, `TutorialTargetPositioned(target: TutorialTarget, bounds: Rect)`, `RefreshSchedules` 추가. `MapViewModel.kt`: `OnboardingRepository.observeTutorialStep()` 구독 → `tutorialStep`(#48 `observeMapDisplayType()`과 같은 `init` 구독 패턴), `ToggleCreateMenu`(FAB) 시 단계 `CREATE_BUTTON`이면 `setTutorialStep(VIDEO_LINK_OPTION)`, `영상 링크로 만들기` 선택 시 `setTutorialStep(COPY_LINK)` 후 기존 `navigateToScheduleEdit`, `SkipOnboarding` → `CompleteOnboardingUseCase(SKIPPED_IN_TUTORIAL)` (research R8, data-model.md §4.2)
- [x] T032 [US3] [UI] `feature/map/.../MapScreen.kt` 튜토리얼 모드: 단계 `CREATE_BUTTON`이면 FAB `boundsInRoot`를 `LinkItCoachMark(message = "일정 생성 버튼을 선택해보세요")`에 전달, `VIDEO_LINK_OPTION`이면 생성 메뉴 열린 상태에서 `영상 링크로 만들기` 항목 bounds + 말풍선 `"영상 링크로 만들기" 를 선택해보세요`, `보관함에서 가져오기`·`직접 만들기`·닫기 버튼 `enabled=false`(ComingSoon 다이얼로그 차단). 우상단 프로필 버튼 자리에 `건너뛰기` `LinkItButton(size=Small, color=Assistive)`를 코치마크 위 레이어에 배치하고 프로필 버튼 숨김. 대상·건너뛰기 외 터치와 시스템 뒤로가기 무시 (FR-012, FR-013, FR-016, FR-017)
- [x] T033 [US3] [API] `feature/map/src/androidUnitTest/.../MapViewModelTest.kt` 확장: 단계 `CREATE_BUTTON`에서 FAB 탭 → `VIDEO_LINK_OPTION`, `영상 링크로 만들기` → `COPY_LINK` + 이동, `SkipOnboarding` → 완료 기록·단계 null, 단계 null이면 기존 동작 불변
- [x] T034 [P] [US3] [UI] `MapScreenshotTest.kt`에 튜토리얼 1단계(FAB 코치마크)·2단계(메뉴 + 코치마크, 비활성 항목) 골든 추가

### 영상 링크로 만들기 화면 (3·4단계)

- [x] T035 [US3] [API] `feature/schedule/.../ScheduleUiState.kt`에 `recommendedVideos: RecommendedVideosState(Loading / Content(List<DiscoverVideo>) / Error)`(초기 Loading), `tutorialStep`, `isGuideVisible`(초기 false), `hasClipboardText`(초기 false), `clipboardToastUrl: String?`, `errorToast: String?` 추가. `ScheduleIntent.kt`에 `LoadRecommendedVideos, CopyRecommendedLink(url), ClipboardAvailabilityChanged(hasText), PasteFromClipboard(text), ApplyClipboardToast, DismissClipboardToast, DismissErrorToast, GuideDelayElapsed, SkipOnboarding, ConfirmOnboardingSchedule`, `ScheduleSideEffect.kt`에 `WriteClipboard(text), NavigateToAnalysisComplete, FinishOnboarding` 추가 (data-model.md §4.3)
- [x] T036 [US3] [API] `ScheduleViewModel.kt` 추천 영상·클립보드·단계: `OnboardingRepository.observeTutorialStep()` 구독, `LoadRecommendedVideos` → `videoRepository.getOnboardingVideos()`(Loading → Content/빈 목록·실패는 Error), `GuideDelayElapsed` → 단계 `COPY_LINK`이고 Content면 `isGuideVisible=true`, `CopyRecommendedLink(url)` → `WriteClipboard(url)` + `clipboardToastUrl=url` + `hasClipboardText=true` + `setTutorialStep(PASTE_LINK)`, `PasteFromClipboard`/`ApplyClipboardToast` → `videoLink` 채움 + `clipboardToastUrl=null` + `setTutorialStep(FREE)`, `ClipboardAvailabilityChanged` → `hasClipboardText`, `SkipOnboarding` → `CompleteOnboardingUseCase(SKIPPED_IN_TUTORIAL)` → `FinishOnboarding`. 토스트 3초 자동 해제(`ScheduleActionFeedbackDurationMillis` 재사용) (research R8·R10)
- [x] T037 [US3] [UI] `feature/schedule/.../ScheduleEditScreen.kt`: 하드코딩 추천 영상 3건 제거 → `recommendedVideos` 상태별 렌더링(Loading 표시 / 가로 목록 카드: 썸네일(Coil)·`링크복사` 버튼·제목 한 줄 말줄임·조회수 / Error: `추천 영상을 불러오지 못했어요` + `다시 시도`). `복사한 링크 붙여넣기` 칩 `enabled = hasClipboardText`, 탭 시 `LocalClipboardManager.getText()` → `PasteFromClipboard`. 화면 진입·포커스 복귀 시 `clipboardManager.hasText()` → `ClipboardAvailabilityChanged`. `onSkipOnboarding` 콜백으로 TopNav 우측 `건너뛰기`(단계 != null일 때만) (FR-018, FR-019, FR-022)
- [x] T038 [US3] [UI] `ScheduleEditScreen.kt` 코치마크·토스트: 진입 후 `LaunchedEffect { delay(TutorialGuideDelayMillis = 600); GuideDelayElapsed }`, 단계 `COPY_LINK` + `isGuideVisible`이면 첫 카드 `링크복사` bounds에 `LinkItCoachMark("링크를 복사해요", pointer)`, 단계 `PASTE_LINK`이면 붙여넣기 칩 bounds에 `LinkItCoachMark("붙여넣어요~", pointer)`, `FREE`이면 오버레이 해제. 하단 클립보드 토스트(`클립보드에 복사한 링크` 제목 + URL + 닫기 아이콘, 본문 탭 → `ApplyClipboardToast`, 닫기 → `DismissClipboardToast`), `errorToast`는 `LinkItToast(Error)`. 지연 전에 대상을 이미 눌렀으면 다음 단계로 (FR-020, FR-021, FR-023, Edge Case)
- [x] T039 [US3] [API] `domain/usecase/CreateOnboardingScheduleUseCase.kt`(`invoke(youtubeUrl, recommendedVideoUrls): CreateOnboardingScheduleResult`): `YouTubeUrl.videoIdOrNull` 실패 → `InvalidFormat`; 추천 videoId 집합에 없음 → `NotRecommended`; `ensureAuthenticated()` → `videoRepository.analyzeVideo(normalized)` → `status != COMPLETED` → `NotReady`; `tripPlanRepository.getTripPlans(cursor)` 커서 루프에서 `videoAnalysisTaskId == analysis.id` 매칭, 없으면 `getVideoAnalysis(id)` 1회 후 재탐색, 그래도 없으면 `NotReady`; `markTripPlanUnchecked(id)` → `Created(id, title)`; 401은 `forceRefresh` 후 1회 재시도 (contracts §2, research R4, contracts/video-analyze-usage.md)
- [x] T040 [US3] [API] `domain/src/commonTest/.../usecase/CreateOnboardingScheduleUseCaseTest.kt`: InvalidFormat / NotRecommended(짧은 주소·추가 매개변수는 같은 링크로 인정) / analyze 202 → NotReady / 200 후 일정 매칭 / 매칭 실패 → 재조회 후 성공 / 401 재시도 / `markTripPlanUnchecked` 호출 검증
- [x] T041 [US3] [API] `ScheduleViewModel.kt` 일정 생성 분기: `SubmitVideoLink` 시 `tutorialStep != null`이면 `CreateOnboardingScheduleUseCase(videoLink, recommendedUrls)` — `InvalidFormat` → `videoLinkError=WRONG_FORMAT` + `errorToast="유효한 영상 링크가 아닙니다"`, `NotRecommended` → `videoLinkError=INVALID_LINK` + `errorToast="온보딩에서는 추천 영상 링크만 사용할 수 있어요"`, `NotReady` → `errorToast="미리 준비된 일정을 가져오지 못했어요. 다시 시도해주세요."`(버튼 활성 유지), `Created` → `NavigateToAnalysisComplete`; `UpdateVideoLink` → `videoLinkError=null`(재활성); `isSubmittingVideoLink`로 중복 탭 차단. 단계 null이면 기존 `StartVideoScheduleCreationUseCase` 경로 유지 (FR-024~FR-026, data-model.md §5)
- [x] T042 [US3] [UI] `feature/schedule/.../navigation/ScheduleEditEntry.kt`·`androidMain/.../navigation/ScheduleNavDisplay.kt`: `NavigateToAnalysisComplete` → `AnalysisComplete` 라우트 push(분석 중 화면 생략), `FinishOnboarding` → `onFinishActivity()` → `ScheduleActivity.finish()`. `ScheduleEditScreen(onSkipOnboarding)` 배선. `NotificationPromptViewModel.kt`(#48)에 `OnboardingRepository.observeTutorialStep()` 구독 → `isOnboardingMode: StateFlow<Boolean>` 추가, `ScheduleNavDisplay`의 알림 시트 조건에 `!isOnboardingMode`를 더해 튜토리얼 중 시트가 코치마크 위에 뜨지 않게 함 (contracts §4, research R8, SC-005)
- [x] T043 [US3] [API] `feature/schedule/src/androidUnitTest/.../ScheduleViewModelTest.kt` 확장: 추천 영상 Loading→Content/Error, `CopyRecommendedLink` → 클립보드 SideEffect·토스트·단계 `PASTE_LINK`, `PasteFromClipboard` → `videoLink`·단계 `FREE`, 온보딩 분기 결과 4종별 토스트·에러·이동, 단계 null이면 기존 분기, `SkipOnboarding` → 완료 기록 + `FinishOnboarding`
- [x] T044 [P] [US3] [UI] `ScheduleEditScreenshotTest.kt`에 추천 영상 로딩/목록/오류, 3단계 코치마크, 4단계 코치마크 + 클립보드 토스트 골든 추가

**Checkpoint**: `사용법 보기` → 5탭으로 분석 완료 화면 도달, 어두운 영역·뒤로가기 무반응, `건너뛰기` 3경로 동작, 오프라인 시 안내+재시도

---

## Phase 6: User Story 4 - 생성된 첫 일정 확인 (Priority: P2)

**Goal**: `생성된 일정 확인하기` → 메인 화면 `저장한 일정` 맨 위 `확인전` 강조 + 지도 마커, 상세 한 번 열면 `확인후`, 온보딩 완료 기록, 완료 화면 뒤로가기 차단 (FR-027~FR-031)

**Independent Test**: quickstart 시나리오 10·16·17

- [x] T045 [US4] [API] `ScheduleViewModel.kt` `ConfirmOnboardingSchedule` → `CompleteOnboardingUseCase(TUTORIAL_FINISHED)` → `SideEffect.FinishOnboarding` + `ScheduleViewModelTest.kt` 케이스 추가 (FR-029)
- [x] T046 [US4] [UI] `feature/schedule/.../ScheduleAnalysisCompleteScreen.kt`: 기존 `SaveMenu` 목업 제거, 캐릭터 일러스트·`축하해요 첫일정 분석이 완료됐어요!`·`짜여진 일정을 저장하고 쉽게 관리해보세요`·`생성된 일정 확인하기` 버튼(`onConfirm` → `ConfirmOnboardingSchedule`), `BackHandler(enabled = true) {}`로 시스템 뒤로가기 차단. `ScheduleAnalysisCompleteScreenshotTest.kt` 골든 재생성 (FR-027)
- [x] T047 [US4] [API] `MapViewModel.kt`: `TripPlanRepository.observeUncheckedTripPlanIds()`와 일정 목록을 `combine`해 `MapScheduleUiModel.isUnchecked` 계산(`MapUiMapper.kt`·`MapUiState.kt`에 `uncheckedScheduleIds`·`isUnchecked` 추가), `SelectSchedule`/`onOpenSchedule(id)` 시 `markTripPlanChecked(id)`, 튜토리얼 단계 `FREE → null` 전이 및 `uncheckedScheduleIds` 변화 감지 시 `loadSchedules()` 재호출(복귀 새로고침, Activity 생명주기 코드 없음) (research R2·R8, FR-030)
- [x] T048 [US4] [UI] `MapScreen.kt`: `isUnchecked` 카드에 강조 배경 토큰 적용(바텀시트 목록·확장 전체 목록 모두), 목록 정렬은 기존 최신순 유지로 신규 일정이 맨 위. 상세에서 돌아왔을 때 저장 일정 장소 마커 유지 확인 (FR-029, FR-030, FR-031)
- [x] T049 [US4] [API] `MapViewModelTest.kt` 확장: `uncheckedScheduleIds` combine → `isUnchecked`, 상세 진입 시 `markTripPlanChecked`, 단계 `FREE→null` 전이 시 목록 재로드, 앱 재실행(새 ViewModel) 후에도 DataStore 값으로 강조 유지
- [x] T050 [P] [US4] [UI] `MapScreenshotTest.kt`에 `확인전` 강조 카드(시트/확장 목록) 골든 추가

**Checkpoint**: 튜토리얼 완주 후 메인 화면에 새 일정이 강조되고, 열어보면 일반 배경, 재실행 시 메인 직행

---

## Phase 7: Polish & Cross-Cutting Concerns

- [x] T051 [API] `./gradlew :app-shared:linkDebugFrameworkIosSimulatorArm64` 통과 확인, `IntroViewModel`·core:ui로 이동한 `TermsViewModel` MissingBinding 시 METRO_INSTRUCTION대로 `IosAppGraph`에 수동 등록. `HomeViewController` 시그니처 변경 없음 확인, Swift 진입(`iOSApp.swift`) 배선은 별도 이슈로 기록 (plan.md 리스크)
- [x] T052 [P] [API] 001 산출물 조정 반영: `docs/specs/mypage-screen.md`를 v0.2.1로 갱신(`TBD-05` 해소 — 완료 플래그는 `CompleteOnboardingUseCase` 3곳에서 기록, 초기화 범위에 약관 동의 시각·튜토리얼 단계·확인전 집합 추가, 완료 토스트 위치는 온보딩 시작 화면, 약관 상세·웹뷰 구현 위치 `core:ui`), `specs/001-mypage-screen/spec.md` Assumptions의 `30초면 충분해요`·`30초 만에 확인해보기` 문구를 002 시안 문구로 교체 (research R9)
- [x] T053 [P] [API] 문서 갱신: `docs/NAVIGATION_STRUCTURE.md` 라우트 표에 `OnboardingStart`/`TermsDetail`과 intro 단일 백스택, `data/README.md`에 `OnboardingLocalDataSource`·`TripPlanLocalDataSource`, DataStore 키 목록(기존 3종 + `terms_agreed_at`·`unchecked_trip_plan_ids`)과 초기화 범위(`AppSettings.clearAll` 2키 + `Onboarding.clearAll` 2키 + 확인전 집합) 갱신, `domain/README.md` UseCase 목록에 `CreateOnboardingScheduleUseCase`·`CompleteOnboardingUseCase`
- [ ] T054 [UI] `specs/002-onboarding-flow/quickstart.md` 수동 시나리오 1~20을 Android 에뮬레이터·iOS 시뮬레이터에서 검증하고 결과를 PR에 기록(19 앱 초기화는 실제 회원 탈퇴이므로 테스트 계정으로만). curl 스모크로 `discover/category` 상위 8개가 `analyze` 200 + `COMPLETED`를 주는지 확인해 미분석 영상은 서버 이슈로 등록
- [ ] T055 [P] [UI] 신규 문자열 디자인 확인 요청: `온보딩에서는 추천 영상 링크만 사용할 수 있어요`, `미리 준비된 일정을 가져오지 못했어요. 다시 시도해주세요.`, `추천 영상을 불러오지 못했어요` (contracts §9 "디자인 확인 필요") — 확정 시 상수만 교체

---

## Dependencies & Execution Order

- **Phase 1 → Phase 2 → 스토리 Phase(3~6)**: T000(develop 리베이스)이 전부를 막는다. T001·T003·T004가 Phase 2를 막는다. T005→T006→T007 순, T008·T010은 병렬(T009는 #48로 불필요), T011은 T006~T010 이후, T056은 T005·T008 이후
- **US1(T017~T025)**: T005~T007(온보딩 저장소), T003(라우트), T004(intro 의존성) 필요. T018→T019→T023 순, T020·T021은 T018 이후 병렬, T022는 T021 이후, T025는 독립
- **US2(T026~T030)**: US1의 T018·T019·T020·T021과 Phase 2의 T013(시트·체크박스)·T016(약관 상세) 필요. T026 ∥ T027 → T028 → T029
- **US3(T031~T044)**: T005~T007·T010·T012(코치마크)·T017 필요. 지도(T031→T032→T033)와 일정(T035→T036→T037→T038)은 병렬, T039→T040→T041→T042→T043 순, T044는 T038 이후
- **US4(T045~T050)**: T008(확인전 집합)·T017·US3의 T041·T042 필요. T045 ∥ T046, T047→T048→T049, T050은 T048 이후
- **Phase 7**: 모든 스토리 이후. T051은 iOS 빌드 게이트

## Parallel Opportunities

- Phase 1: T002 ∥ T003 ∥ T004
- Phase 2: (T005→T006→T007) ∥ T008 ∥ T010 ∥ T012 ∥ T013 ∥ T015; T014는 T012·T013 이후, T016은 T015 이후, T056은 T005·T008 이후
- 스토리 간: US3 지도(T031~T034)와 일정(T035~T038) 병렬. US4 T045·T046은 US3 완료 전 시작 가능
- 그룹 간: `[UI]` 태스크는 `[API]` 태스크의 MVI 계약(data-model.md §4)만 맞추면 Fake 상태로 먼저 진행 가능. `[API]` 태스크는 domain·data 계층을 UI와 무관하게 먼저 완료 가능

## Implementation Strategy

1. Phase 1~2 → US1(MVP: 인트로 → 시작 화면 → 바로 시작 → 메인, 재실행 검증) → US2 → US3 → US4 → Phase 7
2. UI 구현 하위 이슈와 API·데이터 연동 하위 이슈를 병행하되, 각 스토리 Checkpoint에서 통합 확인
3. 추천 영상은 `discover/category` 전체 상위 8개로 확정(신규 API 없음). 해당 영상의 사전 분석은 운영 전제이며 미분석 시 `NotReady` 안내로 방어

## GitHub Issues

- 부모: #47 (https://github.com/Link-Trip/LinkIt-KMP/issues/47)
- UI 구현 하위 이슈: #49 (https://github.com/Link-Trip/LinkIt-KMP/issues/49)
- API·데이터 연동 하위 이슈: #50 (https://github.com/Link-Trip/LinkIt-KMP/issues/50)
