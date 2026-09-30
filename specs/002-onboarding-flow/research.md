# Research: 온보딩 플로우

**Date**: 2026-09-18 | **Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)

Technical Context에 NEEDS CLARIFICATION은 없었다. 아래는 Swagger(`https://linktrip.cloud/api/v3/api-docs`, LinkTrip API v1, 2026-09-18 재조회), 기존 코드베이스 조사, 001 마이페이지 계획을 대조해 내린 결정이다.

> **2026-09-21 갱신**: 001 마이페이지 데이터 연동(PR #48, develop `7e9eafd`)이 먼저 머지되어 `AppSettingsRepository`(`onboarding_completed` 포함)·`TermsRepository`·`TermsDetail` 라우트·`PlatformWebView`·`TermsDetailScreen`·`IntroNavigator`·`ResetAppUseCase`가 이미 존재한다. R1·R5·R7·R8·R9·R12를 그에 맞춰 조정했다. 작업 브랜치는 develop 리베이스 후 진행한다.

## Swagger 대조 결과

| 스펙 요구 | 서버 API 현황 | 결론 |
|---|---|---|
| 추천 영상 목록(FR-019, Q5 서버 제공) | 온보딩 전용 없음. `GET /video/discover/{theme,category,channels,countries}`만 존재 | 기존 `GET /video/discover/category`를 파라미터 없이 호출(전체 영상)해 상위 8개 사용(2026-09-21 확정, 신규 API 제안 없음) |
| 추천 영상 링크로 즉시 분석 완료(FR-026) | `POST /video/analyze`: 이미 분석 완료된 URL이면 **200 + 결과 인라인**(`status=COMPLETED`), 아니면 202 | 기존 `VideoRepository.analyzeVideo()` 그대로 사용. 도메인 `VideoAnalysisStatus != COMPLETED`면 온보딩 실패 처리 → [contracts/video-analyze-usage.md](contracts/video-analyze-usage.md) |
| 생성된 일정 자동 저장(FR-028, Q2) | `POST /trip-plans` 없음. `GET /trip-plans` 설명: "영상 분석 완료 시 자동으로 여행 계획이 생성됩니다. 분석 요청 시점에 계정이 연결되어 있으면 즉시 생성" | 별도 저장 호출 불필요. `analyze` 200 후 `GET /trip-plans`에서 `videoAnalysisTaskId == analysis.id`인 항목을 찾아 id를 얻는다 |
| 일정 이름 규칙 `국가/도시 + x박x일 + 여행 스타일`(FR-028) | `TripPlanSummary.title`은 서버가 생성 | 클라이언트는 서버 제목을 그대로 표시. 규칙 준수는 서버 책임으로 계약에 명시 |
| 약관 상세(FR-009) | 약관 API 없음(001과 동일) | 001 R6 결정 재사용: 운영 웹페이지 URL을 앱이 보유, 앱 내 웹뷰 |
| 온보딩 완료·약관 동의·튜토리얼 단계·확인전 상태 | 서버 개념 없음 | 로컬(DataStore + 메모리) |

`POST /feedback`, `DELETE /members/me`는 001 제안 이후 서버에 구현되어 있다(이 기능 범위 밖, 메모리 갱신 완료).

## R1. 온보딩 상태 저장 위치와 공유 방식

- **Decision**: domain `OnboardingRepository`를 신설한다. `isOnboardingCompleted()/setOnboardingCompleted()`와 `isTermsAgreed()/setTermsAgreed(agreedAtEpochMillis)`는 DataStore 키(`onboarding_completed`, `terms_agreed_at`), `observeTutorialStep(): Flow<TutorialStep?>` / `setTutorialStep(step)`는 data 계층 `OnboardingLocalDataSourceImpl`의 `MutableStateFlow`(`@SingleIn(DataScope::class)`)로 프로세스 메모리에만 둔다. `MapViewModel`과 `ScheduleViewModel`이 같은 Flow를 구독해 튜토리얼 모드를 켠다.
- **Rationale**: 튜토리얼은 HomeActivity(지도)와 ScheduleActivity(영상 링크)를 넘나든다. Intent extra로 나르면 단계 진행(지도 2단계 → 일정 2단계)을 양쪽에서 따로 관리해야 하고 iOS 콜백 경로와도 달라진다. AppGraph는 애플리케이션 단일 인스턴스이므로 메모리 StateFlow가 두 Activity에 공유된다. 스펙 FR-015가 중간 단계 미저장을 요구하므로 DataStore에 쓰지 않는다.
- **Alternatives considered**: (a) Intent extra `EXTRA_TUTORIAL` — Schedule 쪽 단계 진행·완료 콜백을 다시 Home으로 돌려보내야 해 복잡. (b) DataStore에 단계 저장 — FR-015 위배, 프로세스 재생성 시 어색한 중간 복귀. (c) 001 `AppSettingsRepository`(#48)에 `terms_agreed_at`·튜토리얼 단계까지 합치기 — 지도 설정·알림 안내 이력과 관심사가 다르고 메모리 StateFlow(`@SingleIn`)를 얹어야 하므로 온보딩 전용 Repository로 분리한다. 대신 #48이 `AppSettingsRepository`에 둔 `isOnboardingCompleted/setOnboardingCompleted`와 `onboarding_completed` 키는 `OnboardingRepository`로 이관해 온보딩 상태가 한곳에 있게 한다(R9).

## R2. `확인전/확인후` 표현의 데이터 소스

- **Decision**: `TripPlanRepository`에 `observeUncheckedTripPlanIds(): Flow<Set<String>>`, `markTripPlanUnchecked(id)`, `markTripPlanChecked(id)`, `clearUncheckedTripPlans()`를 추가하고 `TripPlanLocalDataSource`(DataStore `stringSetPreferencesKey("unchecked_trip_plan_ids")`)가 저장한다. `MapViewModel`은 일정 목록과 이 Flow를 `combine`해 `MapScheduleUiModel.isUnchecked`를 만든다. 상세 진입(`MapIntent.SelectSchedule`/`onOpenSchedule`) 시 `markTripPlanChecked`.
- **Rationale**: 서버 `TripPlanSummary`에 확인 여부가 없고, 스펙상 "한 번 열면 다시 강조하지 않음"은 기기별 로컬 상태로 충분하다. 온보딩뿐 아니라 일반 생성 흐름(Figma `생성된 신규 일정` 섹션)에도 같은 규칙이 적용되므로 Repository 수준에 둔다.
- **Alternatives considered**: `MapUiState`에 메모리로만 유지 — 앱 재실행 후 강조가 사라져 스펙 US4-6("재실행 후 그대로")과 어긋남. 서버 필드 제안 — 기기 로컬 UX라 서버 변경이 과함.

## R3. 추천 영상 출처

- **Decision**: 기존 `GET /video/discover/category`를 `country`·`region` 없이 호출하면 전체 영상 목록이 오므로(Swagger: "둘 다 미전달 시 전체 영상 목록 반환") 그 상위 8개를 추천 영상으로 쓴다. 신규 API·DTO·`VideoApi`/`VideoRemoteDataSource` 변경 없이 domain에 `VideoRepository.getOnboardingVideos(): List<DiscoverVideo>`만 추가하고, `VideoRepositoryImpl`이 `videoRemoteDataSource.getDiscoverVideosByCategory(country = null, region = null)`의 앞 8개(`ONBOARDING_VIDEO_COUNT = 8`)를 도메인 모델로 반환한다. 일반 `ScheduleEditScreen`(온보딩 아님)의 하드코딩 추천 영상 3건도 같은 목록으로 교체한다.
- **Rationale**: Q5에서 서버 제공 확정. 온보딩 전용 API를 새로 만들 필요 없이 운영이 전체 목록 상위 8개를 사전 분석해 두면 성립한다. 사전 분석 여부는 클라이언트가 알 수 없으므로 `analyze` 응답 코드로 방어(R4).
- **Alternatives considered**: 앱 내장 목록 — Q5에서 기각. 온보딩 전용 `GET /video/discover/onboarding` 제안 — 서버 작업이 추가로 필요해 기각(2026-09-21). `theme` 파라미터에 `ONBOARDING` 값 사용 — 기존 API 의미를 오염시키고 커서 페이지네이션이 불필요.

## R4. 온보딩 전용 일정 생성 UseCase

- **Decision**: `CreateOnboardingScheduleUseCase(youtubeUrl: String, recommendedVideoUrls: List<String>): CreateOnboardingScheduleResult`. 순서: (1) `YouTubeUrl.videoIdOrNull(youtubeUrl)` 실패 → `InvalidFormat` (2) 추천 목록의 videoId 집합에 없으면 → `NotRecommended` (3) `ensureAuthenticated()` (4) `videoRepository.analyzeVideo(normalizedUrl)` (5) `analysis.status != COMPLETED` → `NotReady` (6) `tripPlanRepository.getTripPlans(cursor)` 커서 루프에서 `videoAnalysisTaskId == analysis.id` 탐색, 없으면 `getVideoAnalysis(analysis.id)` 1회 재조회 후 재탐색, 그래도 없으면 `NotReady` (7) `tripPlanRepository.markTripPlanUnchecked(tripPlan.id)` → `Created(tripPlanId, title)`. 401은 기존 UseCase처럼 `forceRefresh` 후 1회 재시도.
- **Rationale**: 링크 정규화·추천 여부 판정·서버 자동 생성 결과 매칭·확인전 표시는 여러 Repository를 조합하는 비즈니스 규칙이라 UseCase 대상(domain/README). 기존 `StartVideoScheduleCreationUseCase`는 중복 일정 팝업·202 폴링 전제라 온보딩 요구(즉시 완료, 추천만 허용)와 다르다. 정규화 로직은 `domain/util/YouTubeUrl.kt`로 추출해 둘이 공유한다.
- **Alternatives considered**: 기존 UseCase에 `onboarding` 플래그 추가 — 결과 타입이 갈라져 ViewModel 분기가 더 복잡. 추천 여부 판정을 ViewModel에서 — 주소 표기 차이 정규화가 도메인 규칙이라 부적절.

## R5. 인트로 Activity 구조와 화면 전환

- **Decision**: `feature/intro`에 `IntroViewModel`(MVI)과 Navigation3 단일 백스택 `IntroNavDisplay`(`startRoute = LinkItNavKey.Intro`)를 둔다. 라우트: `Intro`(애니메이션) → `OnboardingStart`(시작 화면 + 약관 시트 모달) → `TermsDetail(type)`(core:ui 화면). 애니메이션 종료 시 `IntroIntent.SplashFinished` → ViewModel이 `isOnboardingCompleted()`를 읽어 `SideEffect.NavigateToHome(startTutorial = false)` 또는 `OnboardingStart`로 push. `동의하고 시작하기` → `setTermsAgreed(now)` → 선택 버튼에 따라 `setTutorialStep(CREATE_BUTTON)`(사용법 보기) 또는 `CompleteOnboardingUseCase(SKIPPED_AT_START)`(바로 시작) → `NavigateToHome`. Android는 기존 `HomeNavigator.navigate(this); finish()`. `IntroActivity`는 METRO_INSTRUCTION 3단계(`MetroViewModelFactory` 주입, `LocalMetroViewModelFactory`)로 개편하되 #48이 넣은 `EXTRA_SHOW_RESET_TOAST` 읽기는 유지하고, `IntroViewController(appGraph, onComplete, showResetCompletedToast)`로 시그니처를 바꾼다(기존 `showResetCompletedToast` 파라미터 보존). 앱 초기화 완료 토스트(`앱 초기화가 완료되었습니다.`)는 현재 인트로 애니메이션 화면에 뜨지만 001 스펙 FR-026·Figma `18212:34914`는 온보딩 시작 화면이므로 `IntroNavDisplay(showResetCompletedToast)` → `OnboardingStartScreen`으로 옮긴다(초기화 후에는 `onboarding_completed=false`라 항상 시작 화면에 도달한다). `build.gradle.kts`에 `metrox.viewmodel(+compose)`, `jetbrainsNavigation3` 번들, `core:ui` 추가.
- **Rationale**: 약관 상세는 뒤로가기가 있는 풀스크린이라 백스택이 필요하고, 프로젝트 표준이 Nav3다. 인트로 애니메이션은 매 실행 재생(Q3)이므로 Intro가 시작 라우트로 남는다. 온보딩 시작 화면의 시스템 뒤로가기는 백스택 루트이므로 앱 종료(스펙 US1-6).
- **Alternatives considered**: Home 안의 라우트로 온보딩 시작 화면 배치 — 하단 탭·지도 초기화가 먼저 일어나 첫 화면이 무거워지고 런처 Activity 분리 구조와 어긋남. 약관 상세를 다이얼로그로 — 스펙이 뒤로가기·제목이 있는 화면을 요구.

## R6. 코치마크(딤·말풍선) 컴포넌트

- **Decision**: `core:designsystem/component/coachmark/LinkItCoachMark(targetBounds: Rect?, message: String, pointer: CoachMarkPointer?, onTargetClick: () -> Unit, modifier)`. 전체 화면 `Box` 위에 반투명 스크림(`Canvas` + `BlendMode.Clear`로 대상 영역 컷아웃), 대상 위·아래 자동 배치 말풍선(`LinkItTheme.color.semantic` 토큰, radius 토큰), 선택적 포인터 아이콘. `pointerInput`으로 대상 영역 밖 터치를 소비하고 대상 영역 터치는 `onTargetClick`으로 전달(대상 컴포저블 자체를 다시 그리지 않고 클릭만 위임). 대상 좌표는 화면이 `Modifier.onGloballyPositioned { boundsInRoot }`로 상태에 넣는다. `BackHandler(enabled = true) {}`로 시스템 뒤로가기를 무시한다(Android actual, iOS는 no-op).
- **Rationale**: 디자인 시스템에 유사 컴포넌트가 없고 지도·일정 두 feature가 같은 UI를 쓰므로 designsystem 배치. 스펙 FR-012의 "대상과 건너뛰기만 밝게, 나머지 차단"을 컷아웃 + 터치 소비로 만족한다. `건너뛰기`는 코치마크 위 레이어에 별도로 그려 항상 눌리게 한다.
- **Alternatives considered**: 대상 컴포저블을 오버레이 안에 복제해 그리기 — 상태 동기화가 어렵고 FAB 애니메이션과 충돌. 서드파티 코치마크 라이브러리 — KMP 지원·의존성 추가 부담.

## R7. 약관 동의 시트·체크박스·약관 상세 배치

- **Decision**: `LinkItModalBottomSheet`(Material3 `ModalBottomSheet` 래퍼: 핸들·모서리 radius·배경 토큰, `onDismissRequest`), `LinkItCheckbox(checked, onCheckedChange, enabled)`(원형, `Utility.CircleCheckFill` 아이콘 토글)를 designsystem에 추가한다. `TermsConsentSheet`는 intro에 두고, #48이 `feature/map/mypage/{platform,terms}`에 `internal`로 둔 `PlatformWebView`(expect/actual, `reloadToken` 재시도 포함)·`TermsDetailScreen`(`TermsDetailContent`·`TermsLoadState`)·`TermsViewModel`을 `core:ui`로 **이동**한다(신규 작성 아님). `TermsListScreen`은 map에 남기고 core:ui의 `TermsViewModel`을 쓴다. `TermsRepository`(domain)·`TermsRepositoryImpl`(data, URL 상수 4종)·`TermsDocument(Type)`은 #48로 이미 존재하며 변경 없이 재사용한다(`getTermsDocument(type)`를 추가하지 않고 기존처럼 목록에서 `type`으로 찾는다).
- **Rationale**: 스펙 FR-011(끌어 내리기·바깥 탭으로 닫힘)은 M3 시트가 기본 제공한다. 체크 항목·전체 동의 동기화는 ViewModel 상태로 처리. 약관 상세는 온보딩(intro)과 마이페이지(map) 두 feature가 쓰므로 feature 간 의존 금지 규칙상 core에 있어야 한다. `core:ui`는 현재 Platform.kt만 있는 빈 모듈이라 공유 UI 자리로 적합하다.
- **Alternatives considered**: 시트를 Map처럼 `AnchoredDraggable`로 수제 — 모달 스크림·접근성·dismiss를 다시 구현해야 함. 약관 상세를 intro와 map에 각각 복제 — 규칙 위반은 아니지만 유지보수 이중화.

## R8. 지도·일정 화면의 튜토리얼 모드와 화면 간 복귀

- **Decision**:
  - `MapViewModel`: `OnboardingRepository.observeTutorialStep()` 구독 → `MapUiState.tutorialStep`. `ToggleCreateMenu`(FAB 탭) 시 단계가 `CREATE_BUTTON`이면 `setTutorialStep(VIDEO_LINK_OPTION)`; `onCreateFromVideo`는 `setTutorialStep(COPY_LINK)` 후 기존 `navigateToScheduleEdit`. `SkipOnboarding` → `CompleteOnboardingUseCase(SKIPPED_IN_TUTORIAL)`. 튜토리얼 중 생성 메뉴의 나머지 두 항목·닫기 버튼은 `enabled=false`(ComingSoon 다이얼로그도 막음). `MapScreen`은 단계에 따라 FAB/`영상 링크로 만들기` 항목의 `boundsInRoot`를 `LinkItCoachMark`에 넘긴다. 상단 `건너뛰기`는 `LinkItButton(size=Small, color=Assistive)`를 우상단 프로필 버튼 자리에 두고 프로필 버튼은 튜토리얼 중 숨긴다(Figma 시안).
  - 복귀 새로고침: `HomeActivity`가 `ON_RESUME`에 `MapIntent.RefreshSchedules`를 보내는 대신, `MapViewModel`이 `observeUncheckedTripPlanIds()`와 `observeTutorialStep()` 변화(단계 `FREE`→`null` 전이)를 감지해 `loadSchedules()`를 다시 호출한다. Android·iOS 동일 경로.
  - `ScheduleViewModel`: 단계 구독 → `ScheduleUiState.tutorialStep`. 화면 진입 후 `TutorialGuideDelayMillis`(임시 600ms, 디자인 미정) 뒤 코치마크 표시. `CopyRecommendedLink(url)` → 클립보드 쓰기(UI) + `clipboardToast = url` + `setTutorialStep(PASTE_LINK)`. `PasteFromClipboard`/`ApplyClipboardToast` → `videoLink` 채움 + `setTutorialStep(FREE)`. `SubmitVideoLink`: 단계 != null이면 `CreateOnboardingScheduleUseCase` 분기, 아니면 기존 분기. `Created` → `SideEffect.NavigateToAnalysisComplete`. 분석 완료 화면 `생성된 일정 확인하기` → `ConfirmOnboardingSchedule` → `CompleteOnboardingUseCase(TUTORIAL_FINISHED)` → `SideEffect.FinishOnboarding` → `ScheduleNavDisplay(onFinishActivity)`로 `finish()`. 분석 완료 화면은 `BackHandler`로 뒤로가기를 막는다(FR-027). 기존 `SaveMenu` 목업은 제거한다(최신 시안에 없음).
  - `건너뛰기`(일정 화면 TopNav 우측)는 `CompleteOnboardingUseCase(SKIPPED_IN_TUTORIAL)` 후 `FinishOnboarding`.
  - 알림 안내 바텀시트(`ScheduleNavDisplay`, #48부터 `NotificationPromptViewModel`이 DataStore `notification_prompted`로 판단): 튜토리얼 단계가 `null`이 아닌 동안은 띄우지 않는다. 코치마크 위에 시트가 겹치면 스펙 SC-005(대상·건너뛰기 외 조작으로 화면 변화 0건)를 어기기 때문이다. `NotificationPromptViewModel`이 `OnboardingRepository.observeTutorialStep()`도 구독해 `isOnboardingMode`를 내보내고, 시트 조건에 `!isOnboardingMode`를 더한다. 온보딩 완료 후 다음 일정 화면 진입 때 정상 노출된다(노출 이력은 기록하지 않음).
- **Rationale**: 기존 MVI 구조와 Activity 전환 경로(HomeNavigator/ScheduleNavigator/`finish()`)를 그대로 쓰고, 상태만 Repository Flow로 공유한다. Map 새로고침을 Flow 전이로 처리하면 Activity 생명주기 코드가 필요 없고 iOS도 같은 코드로 동작한다.
- **Alternatives considered**: ScheduleActivity를 `startActivityForResult`로 띄워 결과로 새로고침 — Android 전용 경로가 늘고 iOS와 갈라짐.

## R9. 001 마이페이지 구현(PR #48)과의 조정

- **Decision**: 001이 먼저 머지되었으므로(2026-09-21, develop `7e9eafd`) 002는 #48 산출물을 재사용·이동·확장하고, 겹치는 항목은 아래처럼 정리한다.
  - `onboarding_completed` 키와 `isOnboardingCompleted/setOnboardingCompleted`: #48의 `AppSettingsRepository`/`AppSettingsLocalDataSource(Impl)`에서 **제거**하고 `OnboardingRepository`/`OnboardingLocalDataSource`로 이관한다. `AppSettingsLocalDataSourceImpl.clearAll()`은 `map_display_type`·`notification_prompted` 2키만 지운다. `MyPageViewModel`은 온보딩 메서드를 쓰지 않으므로 영향 없음. 테스트 Fake(`domain/commonTest/fake/MyPageFakes.kt`, `feature/map/androidUnitTest/testing/FakeAppSettingsRepository.kt`)에서 해당 메서드를 삭제한다.
  - `TermsRepository`/`TermsRepositoryImpl`/`TermsDocument(Type)`/`LinkItNavKey.Terms`·`TermsDetail(type)` 라우트/`MapEntry`의 `entry<Terms>`·`entry<TermsDetail>`: #48 그대로 재사용, 변경 없음.
  - `PlatformWebView`/`TermsDetailScreen`/`TermsViewModel`: `feature/map/mypage/{platform,terms}` → `core:ui`로 이동(R7). `MyPageStrings.TermsLoadError/TermsRetry` 문자열도 core:ui로 옮기고 map은 이를 참조한다. `feature/map`이 `core:ui`를 의존하도록 추가한다.
  - `ResetAppUseCase`(#48: 인증 → 탈퇴(401 1회 재시도, `NOT_FOUND_MEMBER` 성공) → `appSettingsRepository.clearAll()` → `authRepository.logout()`): 로컬 초기화 단계에 `onboardingRepository.clearAll()`(완료 여부·약관 동의 시각·튜토리얼 단계)과 `tripPlanRepository.clearUncheckedTripPlans()`를 추가한다. 원격 실패 시 로컬 미변경 보장은 유지. `ResetAppUseCaseTest`에 호출 검증을 더한다. 초기화 후 `IntroNavigator`가 `NEW_TASK|CLEAR_TASK`로 IntroActivity를 띄우면 `onboarding_completed=false`라 온보딩 시작 화면부터 시작한다(스펙 US1-7).
  - 앱 초기화 완료 토스트 경로(`IntroNavigatorImpl` → `EXTRA_SHOW_RESET_TOAST` → `IntroActivity` → `IntroScreen(showResetCompletedToast)`, iOS `IntroViewController(onComplete, showResetCompletedToast)`)는 보존하되 표시 위치를 온보딩 시작 화면으로 옮긴다(R5).
  - `MapViewModel`은 #48부터 `AppSettingsRepository.observeMapDisplayType()`을 `init`에서 구독한다. 튜토리얼 단계·확인전 집합 구독(R8, R2)도 같은 패턴(`init` → `viewModelScope.launch { flow.collect { reduce } }`)을 따른다.
  - domain `build.gradle.kts`에는 #48이 `kotlinx-coroutines-core`를 `api`로 추가했으므로 `Flow` 노출을 위한 gradle 변경이 없다.
  - 문서: `docs/specs/mypage-screen.md`의 `TBD-05`(온보딩 완료 플래그 기록 시점)는 이 기능이 해소한다(`CompleteOnboardingUseCase` 3곳). 001 스펙 Assumptions의 시작 화면 문구(`30초면 충분해요`, `30초 만에 확인해보기`)는 002 시안 문구로 대체. `data/README.md` DataStore 키 목록에 `terms_agreed_at`·`unchecked_trip_plan_ids`를 추가하고 초기화 범위 설명을 갱신한다.
- **Rationale**: 같은 키·화면을 두 번 만들면 충돌한다. 001 코드가 이미 있으므로 "002가 소유"가 아니라 "002가 이동·확장"으로 바꾸고, 온보딩 상태(완료 여부·약관 동의·튜토리얼 단계)는 한 Repository에 모아 초기화·테스트 경계를 단순하게 유지한다.
- **Alternatives considered**: `onboarding_completed`를 `AppSettingsRepository`에 남기고 나머지만 `OnboardingRepository`에 두기 — 001 수정이 없어 변경은 적지만 온보딩 상태가 두 저장소로 갈라지고 `CompleteOnboardingUseCase`가 두 Repository를 주입받아야 해 기각.

## R10. 클립보드 처리

- **Decision**: 기존 `ScheduleEditScreen`의 `LocalClipboardManager` 사용을 유지하되 화면 진입·포커스 복귀 시 `clipboardManager.hasText()`로 `ScheduleIntent.ClipboardAvailabilityChanged(hasText)`를 보내 `복사한 링크 붙여넣기` 칩 활성 조건(FR-022)을 만든다. 클립보드 토스트(FR-021)는 앱 안에서 `링크복사`를 눌렀을 때만 `ScheduleUiState.clipboardToast`에 URL을 넣어 표시하고, 시스템 클립보드를 읽어 판단하지 않는다. 토스트 본문 탭 → `ApplyClipboardToast`, 닫기 → `DismissClipboardToast`. 붙여넣기 실패(권한·빈 값)는 칩 비활성 유지.
- **Rationale**: 스펙 Edge Case가 "앱 밖에서 복사한 텍스트는 토스트 미표시"를 요구하므로 토스트는 앱 내부 이벤트로만 만든다. iOS 16+는 프로그램적 읽기 시 권한 배너가 뜨므로 읽기는 사용자 탭 시점으로 한정한다.
- **Alternatives considered**: expect/actual 클립보드 추상화 신설 — 현재 Compose API로 충분하고 사용처가 한 화면뿐.

## R11. 인트로 애니메이션과 대기 시간 상수

- **Decision**: 현재 정적 지구 이미지 + 1.5초 유지. `IntroScreen`의 `LaunchedEffect`는 `delay(IntroSplashDurationMillis)` 후 `IntroIntent.SplashFinished`만 보내고 목적지 결정은 ViewModel이 한다. 튜토리얼 코치마크 지연 `TutorialGuideDelayMillis = 600`, 클립보드 토스트·오류 토스트 표시 3초(Map 전례 `ScheduleActionFeedbackDurationMillis` 재사용). 모두 `internal const`로 두고 디자인 확정 시 값만 바꾼다.
- **Rationale**: 디자인이 길이를 `~ms`로 미정. 애니메이션 자체(지구 줌인)는 스펙 범위 밖.

## R12. 테스트 전략

- **Decision**:
  - domain commonTest: `CreateOnboardingScheduleUseCaseTest`(InvalidFormat / NotRecommended(표기 차이 정규화 포함) / 202→NotReady / 200 후 일정 매칭·재조회 / 401 재시도 / markUnchecked 호출), `CompleteOnboardingUseCaseTest`, `YouTubeUrlTest`, 기존 `ResetAppUseCaseTest` 확장(`onboardingRepository.clearAll()`·`clearUncheckedTripPlans()` 호출 순서, 원격 실패 시 미호출).
  - data commonTest: `VideoRepositoryImpl.getOnboardingVideos`(전체 목록에서 8개 절단, 8개 미만이면 그대로), `OnboardingLocalDataSource`·`TripPlanLocalDataSource`(DataStore 임시 파일) 저장·clear.
  - feature androidUnitTest(Robolectric): `IntroViewModelTest`(완료 여부 분기, 전체 동의 동기화, 버튼 활성 조건, 두 경로의 단계 세팅), `MapViewModelTest` 확장(단계 전이, 건너뛰기, 확인전 combine, 복귀 새로고침), `ScheduleViewModelTest` 확장(온보딩 분기 결과별 토스트·이동, 클립보드 칩 조건).
  - Roborazzi 골든: 온보딩 시작, 약관 시트(미체크/전체 체크), 약관 상세(로딩/실패), 지도 튜토리얼 1·2단계, 영상 링크 튜토리얼 3·4단계 + 클립보드 토스트, 분석 완료(정리 후), designsystem 컴포넌트 3종.
- **Rationale**: 프로젝트 관례(가짜 Repository로 실제 UseCase 조립, `XxxContent` 직접 호출 골든)를 따른다.
