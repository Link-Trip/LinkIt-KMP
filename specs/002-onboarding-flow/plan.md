# Implementation Plan: 온보딩 플로우

**Branch**: `feature/#47-onboarding_flow` (spec dir `002-onboarding-flow`) | **Date**: 2026-09-18 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-onboarding-flow/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

앱 첫 실행 사용자를 "인트로 애니메이션 → 온보딩 시작 → 약관 동의 → 체험형 튜토리얼(지도에서 `일정 생성` → `영상 링크로 만들기` → 추천 영상 `링크복사` → `복사한 링크 붙여넣기` → `일정 생성하기`) → 분석 완료 → 메인 화면(신규 일정 `확인전` 강조)"으로 이끈다. 어느 단계든 `건너뛰기`로 메인 화면에 갈 수 있고, 완료·건너뛰기 이후에는 인트로 애니메이션만 지나 메인 화면이 열린다.

기술 접근:

- **온보딩 상태 계층 신설**: domain `OnboardingRepository`(온보딩 완료·약관 동의 시각은 DataStore, 튜토리얼 단계는 프로세스 메모리)와 `TripPlanRepository` 확장(`확인전` 일정 id 집합, DataStore). `onboarding_completed`는 001(PR #48)이 `AppSettingsRepository`에 먼저 넣었으므로 `OnboardingRepository`로 이관한다. 두 Activity(Home/Schedule)와 두 ViewModel(Map/Schedule)이 같은 `Flow`를 구독해 튜토리얼 단계를 공유한다. Intent extra로 상태를 나르지 않는다.
- **intro 모듈 확장**: `IntroViewModel`(MVI) + Navigation3 단일 백스택(`Intro` → `OnboardingStart` → `TermsDetail`). 약관 동의 바텀시트는 온보딩 시작 화면 위 모달. 완료 여부에 따라 Home으로 갈 때 튜토리얼 단계를 `CREATE_BUTTON`으로 세팅하거나 세팅하지 않는다.
- **디자인 시스템 컴포넌트 3종 추가**: `LinkItCoachMark`(딤 + 대상 컷아웃 + 말풍선·포인터, 대상 외 터치 차단), `LinkItModalBottomSheet`(Material3 `ModalBottomSheet` 래퍼), `LinkItCheckbox`. 약관 상세 화면과 `PlatformWebView`(expect/actual)는 #48이 `feature/map/mypage`에 둔 구현을 `core:ui`로 이동해 intro·map 양쪽이 쓴다.
- **지도·일정 화면의 튜토리얼 모드**: `MapScreen`은 단계 `CREATE_BUTTON/VIDEO_LINK_OPTION`에서 `건너뛰기`와 코치마크를 얹고 나머지 생성 방식을 비활성화한다. `ScheduleEditScreen`은 추천 영상을 서버 목록으로 교체하고 단계 `COPY_LINK/PASTE_LINK`에서 코치마크·클립보드 토스트를 표시하며, 온보딩 중에는 `CreateOnboardingScheduleUseCase`가 추천 영상 링크만 허용해 `POST /video/analyze` 200(사전 분석 완료) 응답으로 곧바로 분석 완료 화면에 간다. 서버가 분석 완료 시 여행 계획을 자동 생성하므로 "자동 저장"은 별도 저장 호출 없이 성립한다.
- **추천 영상 출처**: 온보딩 전용 API를 두지 않고 기존 `GET /video/discover/category`를 파라미터 없이 호출해(전체 영상) 상위 8개를 추천 영상으로 쓴다. 기존 `VideoApi.getDiscoverVideosByCategory(null, null)`·`VideoRemoteDataSource`를 그대로 재사용하며, 사전 분석 여부는 `analyze` 응답 코드로 방어한다.

API 상세 판단은 [research.md](research.md), 도메인·상태 정의는 [data-model.md](data-model.md), 시그니처는 [contracts/domain-contracts.md](contracts/domain-contracts.md)에 있다.

## Technical Context

**Language/Version**: Kotlin 2.2.20, Kotlin Multiplatform (Android + iosArm64/iosSimulatorArm64), Compose Multiplatform 1.9.1

**Primary Dependencies**: Metro DI 0.10.2(+ MetroX ViewModel/Compose), Ktor 3.2.1 + Ktorfit 2.6.5, kotlinx.serialization, AndroidX DataStore Preferences(KMP), Navigation3(멀티 백스택), Coil 3(썸네일), Google Maps Compose(Android) / MapKit(iOS), Material3 `ModalBottomSheet`, Roborazzi 1.41 + Robolectric 4.14(스크린샷·ViewModel 테스트)

**Storage**: DataStore Preferences 단일 파일 `linkit.preferences_pb` — 기존 키 `access_token`, `device_id`, `map_display_type`, `onboarding_completed`, `notification_prompted`(#48) + 신규 키 `terms_agreed_at`, `unchecked_trip_plan_ids`. `onboarding_completed`는 `AppSettingsLocalDataSource`에서 `OnboardingLocalDataSource`로 이관. 튜토리얼 단계는 `@SingleIn(DataScope::class)` 메모리 상태(프로세스 종료 시 소멸, 스펙 FR-015). 서버 데이터는 LinkTrip API(영상 분석·여행 계획·탐색)

**Testing**: `kotlin-test`(domain/data commonTest, Ktor MockEngine), Robolectric + JUnit4(feature androidUnitTest ViewModel 테스트), Roborazzi(스크린샷 골든: intro·map·schedule·designsystem)

**Target Platform**: Android(minSdk 프로젝트 설정), iOS 15+. iOS는 Swift 진입(`iOSApp.swift`)이 현재 stale 상태이므로 Kotlin ViewController 시그니처까지만 책임진다(리스크 참조)

**Project Type**: mobile-app (KMP 멀티모듈 Clean Architecture + MVI)

**Performance Goals**: 추천 영상 링크로 `일정 생성하기` 후 2초 이내 분석 완료 화면(SC-006, 서버 200 인라인 응답 전제), 튜토리얼 5탭 60초 이내(SC-001), 온보딩 완료 후 재실행 시 온보딩 재노출 0회(SC-003)

**Constraints**: feature→domain만 의존(data 직접 의존·feature 간 의존 금지), Repository 시그니처에 DTO 금지, `Idempotency-Key`/`Authorization`은 `LinkTripHeaders`가 자동 첨부, iOS는 `IosAppGraph` 수동 `@Binds` 필수, DataStore 인스턴스 단일, `app-android`가 ViewModel 있는 feature 모듈을 직접 의존(intro에 ViewModel 신설 시 이미 의존 중이므로 추가 없음), 튜토리얼이 HomeActivity↔ScheduleActivity를 넘나듦, 001 마이페이지 산출물(`AppSettingsRepository`, `IntroNavigator`, `TermsDetail` 라우트, `TermsDetailScreen`, `ResetAppUseCase`)은 PR #48로 develop(`7e9eafd`)에 머지됨 — 작업 브랜치는 develop 리베이스 후 시작하고 겹치는 항목은 research R9대로 이관·이동·확장

**Scale/Scope**: 신규 화면 3개(온보딩 시작, 약관 동의 시트, 약관 상세) + 기존 화면 4개 수정(인트로, 지도 메인, 영상 링크로 만들기, 분석 완료) + 디자인 시스템 컴포넌트 3개 + 라우트 1개(`OnboardingStart`, `TermsDetail`은 기존) + 도메인 Repository 1개 신설·2개 확장(`TripPlan`, `Video`)·1개 축소(`AppSettings`) + UseCase 2개 신설·1개 수정(`ResetApp`) + 기존 API 2개 사용(`discover/category`, `analyze`)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

`.specify/memory/constitution.md`는 아직 템플릿 상태(원칙 미정의)이므로 constitution 자체의 게이트는 없다. 프로젝트가 규범으로 삼는 문서를 게이트로 적용한다.

| 게이트 | 출처 | 판정 |
|---|---|---|
| feature 모듈은 domain·core만 의존, feature 간 의존 금지 | docs/ARCHITECTURE.md | PASS — 약관 상세·웹뷰를 `core:ui`에 두어 intro와 map이 공유. 튜토리얼 단계 공유는 domain Repository `Flow`로 해결 |
| Repository/UseCase 시그니처에 DTO 금지, 파라미터는 원시값·도메인 모델 | domain/README.md | PASS — [contracts/domain-contracts.md](contracts/domain-contracts.md) |
| 비즈니스 규칙은 UseCase, 단순 조회만 ViewModel 직접 호출 | domain/README.md | PASS — `CreateOnboardingScheduleUseCase`(링크 정규화·추천 여부·분석·일정 매칭), `CompleteOnboardingUseCase`(3곳 재사용)는 UseCase. 약관 동의 저장·추천 영상 조회·온보딩 완료 여부 읽기는 Repository 직접 호출 |
| DataSource는 Remote/Local 접미사, Impl에 `@ContributesBinding(DataScope::class)` | data/README.md | PASS — `OnboardingLocalDataSource`, `TripPlanLocalDataSource`, `VideoRemoteDataSource` 확장 |
| 새 Impl은 `IosAppGraph`에 `@Binds` 수동 등록 | docs/METRO_INSTRUCTION.md | PASS — 작업 목록 포함 |
| 새 ViewModel은 `@ContributesIntoMap` + Activity에 `MetroViewModelFactory` 주입·`LocalMetroViewModelFactory` 제공 | docs/METRO_INSTRUCTION.md | PASS — `IntroActivity`를 문서의 3단계 형태로 개편, `IntroViewController(appGraph)` 시그니처 변경 |
| 새 Route는 `LinkItNavKey` + serializer 등록 + feature Entry | docs/NAVIGATION_STRUCTURE.md | PASS — `OnboardingStart` 추가(`TermsDetail(type)`·`MapEntry` 등록은 #48에 존재) |
| Figma 구현 시 디자인 토큰·기존 컴포넌트 우선, 하드코딩 색상 금지, 스크린샷 테스트 | docs/COMPOSE_IMPLEMENTATION_GUIDE.md | PASS — LinkItButton/Badge/TextArea/Toast/TopNavigation 재사용, 신규 3종은 designsystem에 추가하고 골든 작성 |
| API·DataSource에서 헤더/에러 바디 직접 처리 금지 | data/README.md | PASS — `LinkTripHeaders`·`HttpResponseValidator` 그대로. `analyze` 202/200 구분은 응답 `status`(도메인 `VideoAnalysisStatus`)로 판단 |
| 디스패처 관성적 지정 금지 | domain/README.md | PASS — UseCase는 Repository 호출만, `withContext` 없음 |

**Post-design re-check (Phase 1 후)**: 위 판정 유지. 추가로 확인한 점: (1) 튜토리얼 단계를 메모리에 두는 것은 domain/README의 "지속 구독은 Flow" 규칙과 스펙 FR-015(중간 상태 미저장)를 동시에 만족한다. (2) `TripPlanRepository`에 로컬 `확인전` 상태를 넣는 것은 "Remote/Local 조합은 Repository 책임" 규칙에 맞는다. (3) 001 구현(#48)과 겹치는 `onboarding_completed`는 `OnboardingRepository`로 이관, `TermsDetail`·`TermsRepository`는 재사용, `PlatformWebView`·`TermsDetailScreen`은 `core:ui`로 이동한다(research R9). 복잡도 예외 없음.

## Project Structure

### Documentation (this feature)

```text
specs/002-onboarding-flow/
├── plan.md              # This file
├── spec.md              # 확정 스펙 (Clarifications 5건 반영)
├── research.md          # Phase 0: 기술 결정 R1~R12
├── data-model.md        # Phase 1: 도메인 모델·DataStore 키·UI 상태·상태 전이
├── quickstart.md        # Phase 1: 검증 시나리오·명령
├── contracts/
│   ├── video-analyze-usage.md       # [기존] POST /video/analyze · GET /trip-plans 사용 계약(사전 분석·자동 생성 전제)
│   └── domain-contracts.md          # Repository/UseCase/MVI/Route/컴포넌트/expect-actual 시그니처
├── checklists/requirements.md
└── tasks.md             # Phase 2 output (/speckit-tasks — 이 명령에서는 생성하지 않음)
```

### Source Code (repository root)

```text
domain/src/commonMain/kotlin/com/linkit/company/domain/
├── model/onboarding/TutorialStep.kt                   # 신규 enum CREATE_BUTTON, VIDEO_LINK_OPTION, COPY_LINK, PASTE_LINK, FREE
├── model/onboarding/OnboardingCompletion.kt           # 신규 enum SKIPPED_AT_START, SKIPPED_IN_TUTORIAL, TUTORIAL_FINISHED
├── model/terms/TermsDocument.kt, TermsDocumentType.kt # 기존(#48) 변경 없음
├── repository/OnboardingRepository.kt                 # 신규 완료 여부·약관 동의·튜토리얼 단계
├── repository/AppSettingsRepository.kt                # 수정 isOnboardingCompleted/setOnboardingCompleted 제거 (OnboardingRepository로 이관)
├── repository/TermsRepository.kt                      # 기존(#48) 변경 없음
├── repository/TripPlanRepository.kt                   # 수정 확인전 id 집합 observe/mark/clear
├── repository/VideoRepository.kt                      # 수정 getOnboardingVideos() (category 전체 상위 8개)
├── util/YouTubeUrl.kt                                 # 신규 normalize/videoId 추출 (StartVideoScheduleCreationUseCase에서 추출·공유)
└── usecase/
    ├── ResetAppUseCase.kt                             # 수정(#48 기존) onboardingRepository.clearAll()·tripPlanRepository.clearUncheckedTripPlans() 추가
    ├── CreateOnboardingScheduleUseCase.kt             # 신규 추천 링크 검증 → analyze(200 기대) → 일정 매칭 → 확인전 표시
    └── CompleteOnboardingUseCase.kt                   # 신규 완료 기록 + 튜토리얼 단계 해제
domain/src/commonTest/.../{usecase/ResetAppUseCaseTest.kt, fake/MyPageFakes.kt}   # 수정 clearAll 호출 검증·Fake 온보딩 메서드 정리

data/src/commonMain/kotlin/com/linkit/company/data/
├── datasource/onboarding/OnboardingLocalDataSource.kt (+Impl)   # 신규 DataStore 키 2개 + 메모리 StateFlow(튜토리얼 단계)
├── datasource/settings/AppSettingsLocalDataSource.kt (+Impl)   # 수정(#48 기존) onboarding 메서드·키 제거, clearAll은 map_display_type·notification_prompted만
├── datasource/tripplan/TripPlanLocalDataSource.kt (+Impl)       # 신규 DataStore stringSet
├── repository/OnboardingRepositoryImpl.kt             # 신규
├── repository/AppSettingsRepositoryImpl.kt            # 수정(#48 기존) onboarding 메서드 제거
├── repository/TermsRepositoryImpl.kt                  # 기존(#48) 변경 없음
├── repository/TripPlanRepositoryImpl.kt               # 수정 Local 조합
└── repository/VideoRepositoryImpl.kt                  # 수정 getOnboardingVideos = getDiscoverVideosByCategory(null, null).take(8)

app-shared/src/iosMain/kotlin/com/linkit/company/IosAppGraph.kt   # 수정 @Binds 3개 추가 (OnboardingLocalDataSourceImpl, OnboardingRepositoryImpl, TripPlanLocalDataSourceImpl; Terms·AppSettings는 #48에 등록됨)

core/navigation/src/commonMain/.../LinkItRoute.kt      # 수정 OnboardingStart + serializer (Terms·TermsDetail은 #48에 존재)

core/designsystem/src/commonMain/.../component/
├── coachmark/LinkItCoachMark.kt                       # 신규 딤·컷아웃·말풍선·포인터·터치 차단
├── sheet/LinkItModalBottomSheet.kt                    # 신규 M3 ModalBottomSheet 래퍼(핸들·토큰)
└── checkbox/LinkItCheckbox.kt                         # 신규 원형 체크 (약관 항목)
core/designsystem/src/androidMain/.../component/preview/{CoachMark,ModalBottomSheet,Checkbox}Previews.kt   # 신규
core/designsystem/src/androidUnitTest/.../screenshot/{CoachMark,Checkbox}ScreenshotTest.kt                  # 신규

core/ui/src/                                           # #48의 feature/map/mypage/{platform,terms}에서 이동 (internal 제거)
├── commonMain/.../terms/TermsDetailScreen.kt          # 이동 TermsDetailContent·TermsLoadState 포함
├── commonMain/.../terms/TermsViewModel.kt             # 이동 (@ContributesIntoMap, TermsRepository.getTermsDocuments())
├── commonMain/.../terms/TermsStrings.kt               # 신규 MyPageStrings.TermsLoadError/TermsRetry 이관
├── commonMain/.../terms/PlatformWebView.kt            # 이동 expect(url, reloadToken, onLoadingChanged, onError, modifier)
├── androidMain/.../terms/PlatformWebView.android.kt   # 이동 AndroidView{WebView}
├── iosMain/.../terms/PlatformWebView.ios.kt           # 이동 UIKitView{WKWebView}
└── androidUnitTest/.../terms/TermsDetailScreenshotTest.kt   # 이동 (feature/map TermsScreenshotTest의 상세 케이스)
core/ui/build.gradle.kts                               # 수정 designsystem·domain·metrox viewmodel(+compose) 의존(현재 빈 모듈)

feature/intro/src/
├── commonMain/.../IntroUiState.kt, IntroIntent.kt, IntroSideEffect.kt, IntroViewModel.kt   # 신규 MVI
├── commonMain/.../IntroScreen.kt                      # 수정 SplashFinished만 호출, 초기화 토스트는 OnboardingStartScreen으로 이동
├── commonMain/.../OnboardingStartScreen.kt            # 신규 후킹 문구·버튼 2개 + 앱 초기화 완료 토스트(showResetCompletedToast)
├── commonMain/.../TermsConsentSheet.kt                # 신규 LinkItModalBottomSheet + LinkItCheckbox
├── commonMain/.../navigation/IntroNavDisplay.kt       # 신규 단일 백스택 (Intro → OnboardingStart → TermsDetail)
├── androidMain/.../IntroActivity.kt                   # 수정 MetroViewModelFactory 주입·LocalMetroViewModelFactory, Home 이동 (EXTRA_SHOW_RESET_TOAST 읽기 유지)
├── iosMain/.../IntroViewController.kt                 # 수정 appGraph 파라미터 추가 (showResetCompletedToast 유지)
├── androidUnitTest/.../IntroViewModelTest.kt          # 신규
├── androidUnitTest/.../{OnboardingStart,TermsConsent}ScreenshotTest.kt   # 신규
└── build.gradle.kts                                   # 수정 metrox.viewmodel(+compose), navigation3 번들, core:ui

feature/map/src/
├── commonMain/.../main/MapUiState.kt                  # 수정 tutorialStep, MapScheduleUiModel.isUnchecked
├── commonMain/.../main/MapIntent.kt                   # 수정 SkipOnboarding, AdvanceTutorial, RefreshSchedules
├── commonMain/.../main/MapViewModel.kt                # 수정 OnboardingRepository·TripPlanRepository 구독(#48 observeMapDisplayType 패턴), CompleteOnboardingUseCase
├── commonMain/.../main/MapScreen.kt                   # 수정 건너뛰기 버튼·코치마크 2단계·생성 메뉴 비활성·확인전 카드 배경·빈 상태 문구
├── commonMain/.../main/navigation/MapEntry.kt         # 수정 TermsDetailScreen import를 core:ui로
├── commonMain/.../mypage/terms/TermsListScreen.kt     # 수정 core:ui TermsViewModel 참조
├── commonMain/.../mypage/{platform,terms}/            # 삭제 PlatformWebView·TermsDetailScreen·TermsViewModel (core:ui로 이동)
├── androidUnitTest/.../testing/FakeAppSettingsRepository.kt   # 수정 온보딩 메서드 제거
├── androidUnitTest/.../main/{MapViewModelTest, MapScreenshotTest}.kt   # 수정 튜토리얼·확인전 케이스
└── build.gradle.kts                                   # 수정 core:ui 의존 추가

feature/schedule/src/
├── commonMain/.../ScheduleUiState.kt                  # 수정 recommendedVideos(load state), tutorialStep, clipboardToast, canPaste
├── commonMain/.../ScheduleIntent.kt                   # 수정 LoadRecommendedVideos, CopyRecommendedLink, PasteFromClipboard, DismissClipboardToast, ApplyClipboardToast, SkipOnboarding, ConfirmOnboardingSchedule
├── commonMain/.../ScheduleSideEffect.kt               # 수정 NavigateToAnalysisComplete, FinishOnboarding, ShowToast
├── commonMain/.../ScheduleViewModel.kt                # 수정 온보딩 분기(CreateOnboardingScheduleUseCase) / 일반 분기(기존 UseCase)
├── commonMain/.../ScheduleEditScreen.kt               # 수정 서버 추천 영상·코치마크·클립보드 토스트·건너뛰기·붙여넣기 활성 조건
├── commonMain/.../ScheduleAnalysisCompleteScreen.kt   # 수정 SaveMenu 목업 제거, ViewModel 연결
├── commonMain/.../navigation/ScheduleEditEntry.kt     # 수정 AnalysisComplete 도달 경로·완료 콜백
├── commonMain/.../NotificationPromptViewModel.kt      # 수정(#48 기존) OnboardingRepository.observeTutorialStep() 구독 → isOnboardingMode
├── androidMain/.../navigation/ScheduleNavDisplay.kt   # 수정 onFinishOnboarding → finish(), 온보딩 모드 중 알림 안내 시트 억제
└── androidUnitTest/.../{ScheduleViewModelTest, ScheduleEditScreenshotTest, ScheduleAnalysisCompleteScreenshotTest}.kt   # 수정·신규

feature/home/src/androidMain/.../HomeActivity.kt       # 변경 없음 (복귀 새로고침은 MapViewModel이 Flow로 감지 — research R8; onAppReset 배선은 #48)
```

**Structure Decision**: 온보딩 시작·약관 동의는 기존 `feature/intro`에 둔다(런처 Activity가 이미 여기 있고 사이트맵의 "인트로/최초진입" 화면군과 일치). 튜토리얼은 새 모듈이 아니라 `feature/map`·`feature/schedule`의 기존 화면에 "튜토리얼 모드"를 얹는다. 두 화면이 서로를 모르므로 단계 공유는 domain `OnboardingRepository.observeTutorialStep()`로 한다. 약관 상세와 웹뷰는 intro(온보딩)와 map(마이페이지) 모두가 필요해 #48의 feature/map 구현을 `core:ui`(현재 빈 모듈)로 옮긴다. 코치마크·모달 시트·체크박스는 재사용 가능한 순수 UI라 `core:designsystem`에 둔다.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

위반 없음.

## 구현 단계 개요 (tasks.md 생성 시 참고)

1. **domain**: `TutorialStep`, `OnboardingCompletion`, `YouTubeUrl` 유틸 추출, `OnboardingRepository` 신설(+`AppSettingsRepository`에서 온보딩 메서드 이관), `TripPlanRepository`·`VideoRepository` 확장, UseCase 2종 신설 + `ResetAppUseCase` 수정 + commonTest (`TermsDocument(Type)`·`TermsRepository`는 #48 재사용)
2. **data**: 기존 `VideoApi`·`VideoRemoteDataSource` 재사용(API·DTO 추가 없음), `OnboardingLocalDataSource`(DataStore + 메모리), `TripPlanLocalDataSource`, `AppSettingsLocalDataSource` 축소, RepositoryImpl 신설 1종(`Onboarding`)·수정 3종(`AppSettings`, `TripPlan`, `Video`의 `getOnboardingVideos`는 category 전체 상위 8개), `IosAppGraph` `@Binds` 3개, DataStore 테스트
3. **designsystem**: `LinkItCoachMark`, `LinkItModalBottomSheet`, `LinkItCheckbox` + 프리뷰 + 골든
4. **core:ui**: #48의 `PlatformWebView` expect/actual·`TermsDetailScreen`·`TermsViewModel`을 feature/map에서 이동, map 참조·골든 갱신
5. **intro**: `IntroViewModel`, `OnboardingStartScreen`, `TermsConsentSheet`, `IntroNavDisplay`, `IntroActivity`/`IntroViewController` 개편, 라우트 `OnboardingStart`, 테스트·골든
6. **map 튜토리얼 모드**: 단계 구독, `건너뛰기`, 코치마크 1·2단계, 생성 메뉴 비활성, `확인전` 강조·체크 처리, 복귀 시 새로고침, 빈 상태 문구 교체
7. **schedule 튜토리얼 모드**: 서버 추천 영상, 클립보드 토스트·붙여넣기 활성 조건, 코치마크 3·4단계, 온보딩 전용 생성 분기, 분석 완료 화면 연결·정리, 완료 시 Activity 종료, 온보딩 모드 중 알림 안내 시트 억제
8. **통합 검증**: quickstart 시나리오(신규 설치·건너뛰기 3경로·튜토리얼 완주·재실행·앱 초기화 후 재온보딩), iOS 프레임워크 링크(MissingBinding 확인)

## 리스크와 대응

| 리스크 | 대응 |
|---|---|
| 추천 영상(`discover/category` 전체 상위 8개)이 사전 분석되어 있지 않을 수 있음 | `analyze`가 202를 주면 "미리 준비된 일정을 가져오지 못했어요" 실패 안내(스펙 Edge Case)로 방어. 운영이 전체 목록 상위 8개를 사전 분석해 두는 것을 전제로 QA(quickstart curl 스모크) |
| 사전 분석된 URL 재요청 시 신규 회원에게 여행 계획이 자동 생성되는지 미확인 | quickstart의 curl 스모크로 `analyze` 200 직후 `GET /trip-plans`에 항목이 생기는지 검증. 생성되지 않으면 서버 이슈로 등록하고, 클라이언트는 `getVideoAnalysis(taskId)` 1회 재조회 후 매칭 재시도 |
| 튜토리얼이 두 Activity에 걸침, 프로세스 재생성 시 메모리 단계 소실 | 단계는 메모리, 완료 여부는 DataStore. 재생성되면 튜토리얼 모드 없이 일반 화면으로 동작하고 `onboarding_completed=false`라 다음 실행에 온보딩 시작부터(FR-015). ScheduleActivity 단독 재생성 시 `건너뛰기` 없이 일반 화면이 되는 것은 허용 |
| 001 마이페이지 구현(#48)과의 중복(`onboarding_completed`, `TermsDetail`, `PlatformWebView`, `AppSettingsRepository`, 초기화 토스트) | #48 코드를 기준으로 이관·이동·확장(research R9): `onboarding_completed`는 `OnboardingRepository`로, 약관 상세·웹뷰는 `core:ui`로, `ResetAppUseCase`는 `clearAll()`·`clearUncheckedTripPlans()` 추가. 작업 브랜치를 develop에 리베이스한 뒤 시작해 `ScheduleNavDisplay`·`MapViewModel`·`IosAppGraph` 충돌을 피한다 |
| 튜토리얼 중 알림 안내 시트가 코치마크 위에 겹침(SC-005 위반) | `NotificationPromptViewModel`(#48)이 튜토리얼 단계를 함께 구독해 온보딩 모드에서는 시트를 띄우지 않음(research R8) |
| iOS Swift 진입(`iOSApp.swift`)이 `MainViewController` 부재로 stale | Kotlin 측 `IntroViewController(appGraph, onComplete)`·`HomeViewController` 시그니처와 `IosAppGraph` 바인딩까지 완료하고 Swift 배선은 별도 이슈로 분리. `linkDebugFrameworkIosSimulatorArm64`로 MissingBinding만 검증 |
| 클립보드 접근 제한(iOS 16+ 붙여넣기 권한 배너, Android 백그라운드 읽기 제한) | 읽기는 사용자 탭(붙여넣기 칩·토스트) 시점에만 수행. `hasText()`는 포그라운드에서만 호출. 실패 시 칩 비활성·토스트 미표시(스펙 Edge Case) |
| `LocalClipboardManager` deprecated 예정 | 이번에는 기존 사용처와 동일 API 유지. 대체 API(`LocalClipboard`) 전환은 별도 리팩터링 |
| Material3 `ModalBottomSheet`가 프로젝트 전례 없음 | 래퍼 `LinkItModalBottomSheet`로 감싸 토큰·핸들을 통일. 기존 수제 시트(Map)는 건드리지 않음 |
