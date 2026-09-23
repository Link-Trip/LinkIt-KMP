# 계약: 도메인 인터페이스·MVI·라우트·플랫폼 경계

프로젝트 규칙(domain/README, data/README, NAVIGATION_STRUCTURE)에 맞춘 시그니처 정의. 구현 본문은 tasks 단계에서 작성한다.

## 1. Repository 인터페이스 (domain/repository)

```kotlin
interface AppSettingsRepository {
    fun observeMapDisplayType(): Flow<MapDisplayType>          // 기본값 DEFAULT
    suspend fun setMapDisplayType(type: MapDisplayType)
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun isNotificationPrompted(): Boolean
    suspend fun setNotificationPrompted(prompted: Boolean)
    fun observeNotificationEnabled(): Flow<Boolean>             // 앱 알림 수신 설정, 기본값 true (2026-09-23 추가)
    suspend fun setNotificationEnabled(enabled: Boolean)
    suspend fun clearAll()                                      // 위 키 전부 제거 (인증 키 제외)
}

interface FeedbackRepository {
    suspend fun sendFeedback(type: FeedbackType, content: String, appInfo: AppInfo)
}

interface TermsRepository {
    suspend fun getTermsDocuments(): List<TermsDocument>        // 고정 순서 4종
}

interface MemberRepository {
    suspend fun updateNotificationSetting(enabled: Boolean): NotificationSetting
    suspend fun withdraw(): Int                                 // DELETE /members/me, 삭제된 여행 계획 수 반환
}

interface AppInfoRepository {
    suspend fun getAppInfo(): AppInfo
}
```

기존 `AuthRepository.logout()`은 그대로 사용한다. (2026-09-21) 서버 `DELETE /members/me` 배포로 `TripPlanRepository` 단건 삭제 반복은 쓰지 않는다.

## 2. UseCase (domain/usecase)

```kotlin
@Inject class SendFeedbackUseCase(
    ensureAuthenticated: EnsureAuthenticatedUseCase,
    feedbackRepository: FeedbackRepository,
    appInfoRepository: AppInfoRepository,
) { suspend operator fun invoke(type: FeedbackType?, content: String) }
// 규칙: content.trim() 1..200자 아니면 IllegalArgumentException, type ?: ETC, AppInfo 첨부
// 401이면 forceRefresh 후 1회 재시도. 429/그 외 LinkTripApiException은 그대로 전파(ViewModel이 분기)

@Inject class ResetAppUseCase(
    ensureAuthenticated: EnsureAuthenticatedUseCase,
    memberRepository: MemberRepository,
    appSettingsRepository: AppSettingsRepository,
    authRepository: AuthRepository,
) { suspend operator fun invoke() }
// 순서: 인증 → memberRepository.withdraw() (NOT_FOUND_MEMBER는 성공 취급, 401은 forceRefresh 후 1회 재시도)
//      → appSettingsRepository.clearAll() → authRepository.logout()
// 원격 단계 예외는 로컬 변경 없이 전파 — contracts/member-withdraw-api.md

// 2026-09-23 개정: SyncNotificationSettingUseCase(best-effort) 제거 → UpdateNotificationSettingUseCase
// 순서: 인증 보장 → memberRepository.updateNotificationSetting(enabled) (401은 forceRefresh 후 1회 재시도)
//      → 성공 시 appSettingsRepository.setNotificationEnabled(응답 enabled). 실패는 전파, 로컬 미변경
@Inject class UpdateNotificationSettingUseCase(
    ensureAuthenticated: EnsureAuthenticatedUseCase,
    memberRepository: MemberRepository,
    appSettingsRepository: AppSettingsRepository,
) { suspend operator fun invoke(enabled: Boolean) }
```

## 3. data 계층 시그니처

```kotlin
// api
internal interface FeedbackApi { @POST("feedback") @Headers("Content-Type: application/json")
    suspend fun createFeedback(@Body request: CreateFeedbackRequest): ApiResponse<Unit> }
internal interface MemberApi {
    @PUT("members/me/notification") @Headers("Content-Type: application/json")
    suspend fun updateNotificationSetting(@Body request: NotificationSettingRequest): ApiResponse<NotificationSettingResponse>
    @DELETE("members/me")
    suspend fun withdraw(): ApiResponse<WithdrawMemberResponse>
}

// dto
@Serializable internal data class CreateFeedbackRequest(val type: String, val content: String,
    val appVersion: String, val platform: String, val osVersion: String, val deviceModel: String)   // platform: IOS|ANDROID
@Serializable internal data class NotificationSettingRequest(val enabled: Boolean)
@Serializable data class NotificationSettingResponse(val enabled: Boolean)   // public: DataSource 시그니처 노출
@Serializable data class WithdrawMemberResponse(val deletedTripPlanCount: Int)   // public

// datasource
interface FeedbackRemoteDataSource { suspend fun createFeedback(type: String, content: String,
    appVersion: String, platform: String, osVersion: String, deviceModel: String) }
interface MemberRemoteDataSource {
    suspend fun updateNotificationSetting(enabled: Boolean): NotificationSettingResponse
    suspend fun withdraw(): WithdrawMemberResponse
}
interface AppSettingsLocalDataSource {
    fun observeMapDisplayType(): Flow<String?>
    suspend fun saveMapDisplayType(value: String)
    suspend fun isOnboardingCompleted(): Boolean; suspend fun saveOnboardingCompleted(value: Boolean)
    suspend fun isNotificationPrompted(): Boolean; suspend fun saveNotificationPrompted(value: Boolean)
    fun observeNotificationEnabled(): Flow<Boolean?>; suspend fun saveNotificationEnabled(value: Boolean)   // 키 notification_enabled, null이면 기본 true
    suspend fun clearAll()
}

// core
fun interface AppInfoProvider { fun getAppInfo(): AppInfoValue }   // AppInfoValue(appVersion, platform, osVersion, deviceModel) — data 내부 값 객체
// 서버 상한: appVersion·osVersion 20자, deviceModel 50자 — FeedbackRepositoryImpl에서 take(n)으로 자른다
```

## 4. MVI 계약 (feature/map/mypage)

`MyPageUiState`, `FeedbackSheetState`, `MyPageIntent`, `MyPageSideEffect`의 필드·케이스는 [data-model.md §4](../data-model.md)를 따른다. 화면 콜백:

```kotlin
@Composable fun MyPageScreen(
    onBack: () -> Unit,
    onOpenTerms: () -> Unit,
    onAppReset: () -> Unit,            // SideEffect.AppResetCompleted 시 호출 (호스트가 Intro 전환)
    viewModel: MyPageViewModel = metroViewModel(),
)
@Composable fun TermsListScreen(onBack: () -> Unit, onOpenDocument: (TermsDocumentType) -> Unit)
@Composable fun TermsDetailScreen(type: TermsDocumentType, onBack: () -> Unit)
```

## 5. 라우트 (core/navigation)

```kotlin
@Serializable data object Terms : LinkItNavKey
@Serializable data class TermsDetail(val type: String) : LinkItNavKey   // TermsDocumentType.name
```
`LinkItSavedStateConfiguration`의 polymorphic 블록에 두 serializer를 등록하고, `MapEntry`에 `entry<Terms>`, `entry<TermsDetail>`를 추가한다.

## 6. 플랫폼 경계 (expect/actual)

```kotlin
// feature/map/commonMain/.../mypage/platform
expect suspend fun isAppNotificationEnabled(): Boolean?     // null = 조회 불가
expect fun openAppNotificationSettings()
@Composable expect fun PlatformWebView(url: String, modifier: Modifier,
    onLoadingChanged: (Boolean) -> Unit, onError: () -> Unit)

// Android Navigator
interface IntroNavigator : Navigator                        // core/navigation/androidMain
class IntroNavigatorImpl : IntroNavigator                   // feature/intro/androidMain, EXTRA_SHOW_RESET_TOAST
// iOS
fun HomeViewController(appGraph: AppGraph, navigateToScheduleEdit: () -> Unit = {}, onAppReset: () -> Unit = {})
fun IntroViewController(appGraph: AppGraph, showResetCompletedToast: Boolean = false, ...)
```

## 7. iOS 수동 등록 목록 (IosAppGraph)

`@Binds`: AppSettingsLocalDataSourceImpl, FeedbackRemoteDataSourceImpl, MemberRemoteDataSourceImpl, AppSettingsRepositoryImpl, FeedbackRepositoryImpl, TermsRepositoryImpl, MemberRepositoryImpl, AppInfoRepositoryImpl.
`@Provides`: `AppInfoProvider` (NSBundle/UIDevice).

## 8. 사용자 노출 문자열 (Figma 확정)

| 키 | 문자열 |
|---|---|
| toast.map.satellite | 위성 지도로 변경되었습니다. |
| toast.map.default | 기본 지도로 변경되었습니다. |
| toast.feedback.success | 의견 전송이 완료되었습니다. |
| toast.feedback.limit | 최대 의견 전송 횟수를 초과했습니다. |
| toast.feedback.failure | 전송에 실패했습니다. 다시 시도해주세요. |
| toast.reset.success | 앱 초기화가 완료되었습니다. |
| toast.reset.failure | 앱 초기화에 실패했습니다. 다시 시도해주세요. (Figma 미정의, 스펙 Edge Case 대응) |
| dialog.reset.title / description | 정말 앱을 초기화 하시겠어요? / 앱을 초기화하면 다시 복구할 수 없어요 |
| dialog.reset.confirm / cancel | 초기화 / 돌아가기 |
| card.notification.title / body / action | 기기 알림이 꺼져있어요 / 알림이 꺼져있으면 영상 분석이 완료되어도 바로 알 수 없어요! / 설정하러 가기 |
| toast.notification.failure | 알림 설정 변경에 실패했습니다. 다시 시도해주세요. |
| sheet.feedback.title / body | 의견 보내기 / 보내주신 의견은 서비스 개선에 활용돼요. (개별 답변은 어려워요 🥹) 스팸 방지를 위해 하루 최대 5회까지 전송할 수 있어요! |
| sheet.feedback.placeholder / send | 핑고를 쓰면서 느낀 점이나 발견한 문제를 알려주세요! / 보내기 |
| terms.error / retry | 약관을 불러오지 못했어요 / 다시 시도 (Figma 미정의, FR-021a 대응) |
