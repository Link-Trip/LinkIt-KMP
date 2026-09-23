# 계약: 도메인 인터페이스·MVI·라우트·컴포넌트·플랫폼 경계

프로젝트 규칙(domain/README, data/README, NAVIGATION_STRUCTURE, METRO_INSTRUCTION)에 맞춘 시그니처 정의. 구현 본문은 tasks 단계에서 작성한다.

## 1. Repository 인터페이스 (domain/repository)

```kotlin
interface OnboardingRepository {
    suspend fun isOnboardingCompleted(): Boolean                 // 기본 false (onboarding_completed — #48 AppSettingsRepository에서 이관)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun isTermsAgreed(): Boolean                         // terms_agreed_at 존재 여부
    suspend fun setTermsAgreed(agreedAtEpochMillis: Long)
    fun observeTutorialStep(): Flow<TutorialStep?>               // 메모리, 초기 null
    suspend fun setTutorialStep(step: TutorialStep?)
    suspend fun clearAll()                                       // 키 2개 제거 + 단계 null (ResetAppUseCase가 호출)
}

interface AppSettingsRepository {                               // 기존(#48) 수정: 온보딩 메서드 2개를 OnboardingRepository로 이관
    fun observeMapDisplayType(): Flow<MapDisplayType>
    suspend fun setMapDisplayType(type: MapDisplayType)
    suspend fun isNotificationPrompted(): Boolean
    suspend fun setNotificationPrompted(prompted: Boolean)
    suspend fun clearAll()                                       // map_display_type·notification_prompted만 제거
}

interface TermsRepository {                                      // 기존(#48) 변경 없음. 상세 화면은 목록에서 type으로 찾는다
    suspend fun getTermsDocuments(): List<TermsDocument>         // 고정 순서 4종
}

interface TripPlanRepository {                                   // 기존 + 추가
    // 기존: getTripPlans(cursor), getTripPlan(id), updateTripPlan(...), deleteTripPlan(id)
    fun observeUncheckedTripPlanIds(): Flow<Set<String>>
    suspend fun markTripPlanUnchecked(tripPlanId: String)
    suspend fun markTripPlanChecked(tripPlanId: String)
    suspend fun clearUncheckedTripPlans()
}

interface VideoRepository {                                      // 기존 + 추가
    suspend fun getOnboardingVideos(): List<DiscoverVideo>       // GET /video/discover/category(파라미터 없음) 전체 중 상위 8개
}
```

## 2. UseCase (domain/usecase)

```kotlin
@Inject class CreateOnboardingScheduleUseCase(
    ensureAuthenticated: EnsureAuthenticatedUseCase,
    videoRepository: VideoRepository,
    tripPlanRepository: TripPlanRepository,
) { suspend operator fun invoke(youtubeUrl: String, recommendedVideoUrls: List<String>): CreateOnboardingScheduleResult }
// 규칙: YouTubeUrl.videoIdOrNull 실패 → InvalidFormat / 추천 videoId 집합에 없음 → NotRecommended
//      ensureAuthenticated → analyzeVideo(normalized) → status != COMPLETED → NotReady
//      getTripPlans 커서 루프에서 videoAnalysisTaskId 매칭, 실패 시 getVideoAnalysis(id) 1회 후 재탐색, 실패 → NotReady
//      markTripPlanUnchecked(id) → Created(id, title). 401은 forceRefresh 후 1회 재시도. 그 외 LinkTripApiException 전파

@Inject class CompleteOnboardingUseCase(
    onboardingRepository: OnboardingRepository,
) { suspend operator fun invoke(reason: OnboardingCompletion) }
// 규칙: setOnboardingCompleted(true) → setTutorialStep(null). 순서 고정(완료 기록이 먼저)

@Inject class ResetAppUseCase(                                   // 기존(#48) 수정
    ensureAuthenticated: EnsureAuthenticatedUseCase,
    memberRepository: MemberRepository,
    appSettingsRepository: AppSettingsRepository,
    onboardingRepository: OnboardingRepository,                  // 추가
    tripPlanRepository: TripPlanRepository,                      // 추가
    authRepository: AuthRepository,
) { suspend operator fun invoke() }
// 순서: ensureAuthenticated → withdraw(401은 forceRefresh 후 1회 재시도, NOT_FOUND_MEMBER는 성공)
//      → appSettings.clearAll() → onboarding.clearAll() → tripPlan.clearUncheckedTripPlans() → auth.logout()
// 원격 단계 실패 시 로컬 미변경(#48 규칙 유지)

// domain/util
object YouTubeUrl { fun normalize(url: String): String; fun videoIdOrNull(url: String): String? }
// StartVideoScheduleCreationUseCase의 private 로직을 이곳으로 이동해 공유
```

ViewModel 직접 호출(UseCase 없음): `onboardingRepository.isOnboardingCompleted()`, `setTermsAgreed()`, `setTutorialStep()`, `observeTutorialStep()`, `videoRepository.getOnboardingVideos()`, `termsRepository.getTermsDocuments()`(core:ui로 이동한 `TermsViewModel`), `tripPlanRepository.observeUncheckedTripPlanIds()/markTripPlanChecked()`.

## 3. data 계층 시그니처

```kotlin
// api: 추가 없음. 기존 VideoApi.getDiscoverVideosByCategory(country = null, region = null) 재사용

// datasource
interface OnboardingLocalDataSource {
    suspend fun isOnboardingCompleted(): Boolean; suspend fun saveOnboardingCompleted(value: Boolean)
    suspend fun getTermsAgreedAt(): Long?;        suspend fun saveTermsAgreedAt(value: Long)
    fun observeTutorialStep(): Flow<String?>;     suspend fun saveTutorialStep(value: String?)   // enum name
    suspend fun clearAll()
}
@Inject @ContributesBinding(DataScope::class) @SingleIn(DataScope::class)
class OnboardingLocalDataSourceImpl(dataStore: DataStore<Preferences>) : OnboardingLocalDataSource
// 주의: 메모리 StateFlow를 보유하므로 @SingleIn 필수 (두 Activity가 같은 인스턴스를 봐야 함)

interface AppSettingsLocalDataSource {                         // 기존(#48) 수정: isOnboardingCompleted/saveOnboardingCompleted·KEY_ONBOARDING_COMPLETED 제거
    fun observeMapDisplayType(): Flow<String?>;   suspend fun saveMapDisplayType(value: String)
    suspend fun isNotificationPrompted(): Boolean; suspend fun saveNotificationPrompted(value: Boolean)
    suspend fun clearAll()                                       // map_display_type·notification_prompted 2키
}

interface TripPlanLocalDataSource {
    fun observeUncheckedIds(): Flow<Set<String>>
    suspend fun addUncheckedId(id: String); suspend fun removeUncheckedId(id: String); suspend fun clearUnchecked()
}
@Inject @ContributesBinding(DataScope::class) class TripPlanLocalDataSourceImpl(dataStore) : TripPlanLocalDataSource

// VideoRemoteDataSource: 추가 없음. 기존 getDiscoverVideosByCategory(null, null) 재사용

// repository impl
@Inject @ContributesBinding(DataScope::class) class OnboardingRepositoryImpl(local: OnboardingLocalDataSource)
// 기존(#48) 변경 없음: TermsRepositoryImpl(URL 상수 4종), AppSettingsRepositoryImpl은 온보딩 메서드만 제거
class TripPlanRepositoryImpl(remote: TripPlanRemoteDataSource, local: TripPlanLocalDataSource)  // 기존 수정
class VideoRepositoryImpl(remote: VideoRemoteDataSource)                                         // 기존 수정: getOnboardingVideos = getDiscoverVideosByCategory(null, null).take(ONBOARDING_VIDEO_COUNT /* 8 */)
```

## 4. MVI 계약

`IntroUiState/TermsSheetState/IntroIntent/IntroSideEffect`, `MapUiState`·`ScheduleUiState` 추가 필드와 Intent/SideEffect는 [data-model.md §4](../data-model.md)를 따른다. 화면 콜백:

```kotlin
// feature/intro
@Composable fun IntroNavDisplay(showResetCompletedToast: Boolean, onNavigateToHome: () -> Unit)   // Intro/OnboardingStart/TermsDetail 호스트. 토스트 플래그는 OnboardingStartScreen에 전달
@Composable fun IntroScreen(onSplashFinished: () -> Unit)                     // 애니메이션만
@Composable fun OnboardingStartScreen(uiState: IntroUiState, onIntent: (IntroIntent) -> Unit, showResetCompletedToast: Boolean = false)  // 시트 + 앱 초기화 완료 토스트(ResetCompletedToastMessage, #48 IntroScreen에서 이동) 포함
@Composable fun TermsConsentSheet(state: TermsSheetState, onIntent: (IntroIntent) -> Unit)

// core/ui (#48 feature/map/mypage/{terms,platform}에서 이동, internal 제거 — 시그니처는 #48 그대로)
@Composable fun TermsDetailScreen(type: TermsDocumentType, onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: TermsViewModel = metroViewModel())
@Composable fun TermsDetailContent(document: TermsDocument, loadState: TermsLoadState, onBack: () -> Unit, onRetry: () -> Unit, modifier: Modifier = Modifier, webView: @Composable () -> Unit = {})   // 골든용
@Composable expect fun PlatformWebView(url: String, reloadToken: Int, onLoadingChanged: (Boolean) -> Unit, onError: () -> Unit, modifier: Modifier = Modifier)

// feature/map (기존 시그니처 유지, 내부 동작 확장)
MapScreen(onOpenSchedule, navigateToScheduleEdit, …)  // 온보딩 모드는 uiState.tutorialStep으로 판단

// feature/schedule
ScheduleEditScreen(onBack, onCreateSchedule, onSkipOnboarding: () -> Unit, …)
ScheduleAnalysisCompleteScreen(onConfirm: () -> Unit)        // ViewModel이 ConfirmOnboardingSchedule 처리
ScheduleNavDisplay(startRoute, onFinishActivity)             // FinishOnboarding → onFinishActivity()
NotificationPromptViewModel.isPrompted: StateFlow<Boolean?>  // #48 기존
NotificationPromptViewModel.isOnboardingMode: StateFlow<Boolean>   // 추가. 알림 시트 조건: 권한 없음 && isPrompted == false && !isOnboardingMode && !dismissed
```

## 5. 라우트 (core/navigation)

```kotlin
@Serializable data object OnboardingStart : LinkItNavKey
// 기존(#48): @Serializable data class TermsDetail(val type: String) : LinkItNavKey   // TermsDocumentType.name
```
`TermsDetail(type)`은 #48로 존재한다(serializer 등록·`MapEntry`의 `entry<Terms>`·`entry<TermsDetail>` 포함). 002는 `OnboardingStart`만 추가하고 `LinkItSavedStateConfiguration` polymorphic 블록에 serializer를 등록한다. `IntroNavDisplay`의 entryProvider에 `entry<Intro>`, `entry<OnboardingStart>`, `entry<TermsDetail>`.

## 6. 디자인 시스템 컴포넌트 (core/designsystem)

```kotlin
@Composable fun LinkItCoachMark(
    targetBounds: Rect?,                 // 루트 좌표계. null이면 스크림만
    message: String,
    pointer: CoachMarkPointer? = null,   // 커서 아이콘 위치(대상 기준)
    onTargetClick: () -> Unit,
    modifier: Modifier = Modifier,       // 전체 화면 오버레이. 대상 밖 터치 소비, 시스템 뒤로가기 무시
)
@Composable fun LinkItModalBottomSheet(onDismissRequest: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit)
@Composable fun LinkItCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true)
```

## 7. 플랫폼 경계

```kotlin
// Android: 기존 HomeNavigator/ScheduleNavigator/IntroNavigator(#48) 그대로. IntroActivity는 MetroViewModelFactory 주입 + LocalMetroViewModelFactory 제공,
//          EXTRA_SHOW_RESET_TOAST 읽기(#48) 유지 → IntroNavDisplay(showResetCompletedToast)
// iOS
fun IntroViewController(appGraph: AppGraph, onComplete: () -> Unit, showResetCompletedToast: Boolean = false): UIViewController   // 기존(onComplete, showResetCompletedToast)에 appGraph 추가
fun HomeViewController(appGraph: AppGraph, navigateToScheduleEdit: () -> Unit, …)      // 변경 없음
// 코치마크 BackHandler: androidx.activity BackHandler(Android) / no-op(iOS) — designsystem expect/actual
```

## 8. iOS 수동 등록 목록 (IosAppGraph)

`@Binds` 추가 3개: `OnboardingLocalDataSourceImpl`, `TripPlanLocalDataSourceImpl`, `OnboardingRepositoryImpl`. `TermsRepositoryImpl`·`AppSettingsLocalDataSourceImpl`·`AppSettingsRepositoryImpl`은 #48에 등록되어 있다. ViewModel(`IntroViewModel`, core:ui로 이동한 `TermsViewModel`) MissingBinding 시 METRO_INSTRUCTION대로 수동 등록.

## 9. 사용자 노출 문자열 (Figma 확정 / 신규 표기)

| 키 | 문자열 | 출처 |
|---|---|---|
| start.badge | 여행 영상, 이제 저장만 하지 마세요 | Figma |
| start.title | 보고있던 여행 영상을 내 일정으로 만들어보세요! | Figma |
| start.subtitle | 영상을 분석해 마커와 일정으로 정리해드릴게요 ⭐ | Figma |
| start.primary / secondary | 30초 만에 사용법 보기 / 바로 시작하기 | Figma |
| terms.title / subtitle | 핑고를 시작하려면 동의가 필요해요 / 필수 항목에 동의해야 서비스를 이용할 수 있어요 | Figma |
| terms.all / service / privacy | 전체 동의 / [필수] 서비스 이용약관 동의 / [필수] 개인정보 수집·이용 동의 | Figma |
| terms.detail / start | 상세보기 / 동의하고 시작하기 | Figma |
| terms.error / retry | 약관을 불러오지 못했어요 / 다시 시도 | #48 `MyPageStrings.TermsLoadError/TermsRetry` → core:ui `TermsStrings`로 이관 |
| reset.toast | 앱 초기화가 완료되었습니다. | #48 `IntroScreen.kt` `ResetCompletedToastMessage` → OnboardingStartScreen으로 이동 |
| tutorial.skip | 건너뛰기 | Figma |
| tutorial.step1 / step2 | 일정 생성 버튼을 선택해보세요 / "영상 링크로 만들기" 를 선택해보세요 | Figma |
| tutorial.step3 / step4 | 링크를 복사해요 / 붙여넣어요~ | Figma |
| edit.recommended / copy | 추천영상 / 링크복사 | Figma |
| edit.paste / linkLabel / placeholder | 복사한 링크 붙여넣기 / 영상 링크 / URL 를 붙여넣거나 입력해주세요. | Figma |
| edit.create | 일정 생성하기 | Figma |
| toast.clipboard.title | 클립보드에 복사한 링크 | Figma |
| toast.invalid | 유효한 영상 링크가 아닙니다 | Figma |
| toast.notRecommended | 온보딩에서는 추천 영상 링크만 사용할 수 있어요 | **신규(디자인 확인 필요)** |
| toast.notReady | 미리 준비된 일정을 가져오지 못했어요. 다시 시도해주세요. | **신규(디자인 확인 필요)** |
| edit.recommended.error / retry | 추천 영상을 불러오지 못했어요 / 다시 시도 | **신규(디자인 확인 필요)** |
| complete.title / body / cta | 축하해요 첫일정 분석이 완료됐어요! / 짜여진 일정을 저장하고 쉽게 관리해보세요 / 생성된 일정 확인하기 | Figma |
| map.empty.title / body / cta / link | 저장된 일정이 없어요 / 여행 영상 하나면 핑고가 일정으로 만들어드려요! / 일정 생성하기 / 볼만한 영상 찾아보기 | Figma (기존 `아직 등록된 일정이 없습니다.` 교체) |
