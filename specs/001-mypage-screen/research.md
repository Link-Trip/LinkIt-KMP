# Research: 마이페이지

**Date**: 2026-09-18 | **Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)

Technical Context에 NEEDS CLARIFICATION은 없었다. 아래는 Swagger(`https://linktrip.cloud/api/v3/api-docs`, LinkTrip API v1)와 기존 data 레이어를 대조해 내린 결정이다.

## Swagger 대조 결과

| 스펙 요구 | 서버 API 현황 | 결론 |
|---|---|---|
| 의견 전송(FR-015~018) | 없음 | **신규 제안** `POST /feedback` → [contracts/feedback-api.yaml](contracts/feedback-api.yaml) |
| 앱 초기화 서버 데이터 삭제(FR-025) | `DELETE /trip-plans/{tripPlanId}` 단건만 존재, 회원 삭제 없음 | 단건 삭제 반복으로 구현 + **신규 제안** `DELETE /members/me` → [contracts/member-reset-api.yaml](contracts/member-reset-api.yaml) |
| 알림 상태(FR-008~011) | `PUT /members/me/notification {enabled}` 존재, `PUT /members/me/fcm-token` 존재 | 기기 권한값을 서버에 best-effort 동기화 → [contracts/member-notification-api.md](contracts/member-notification-api.md). FCM 토큰 등록은 앱에 FCM 미도입이라 범위 밖 |
| 이용약관(FR-020~022) | 없음 | 운영 웹페이지 URL을 앱이 보유(Q3 확정). 서버 API 불필요 |
| 지도 설정(FR-004~007) | 해당 없음(로컬 설정) | DataStore |

서버 에러 코드 중 클라이언트 enum(`LinkTripErrorCode`)에 없는 것: `NOT_FOUND_MEMBER`, `UNAUTHORIZED_TOKEN_EXPIRED`, `BAD_REQUEST_PLATFORM`. 이번에 `NOT_FOUND_MEMBER`, `UNAUTHORIZED_TOKEN_EXPIRED`와 제안 코드 `FEEDBACK_DAILY_LIMIT_EXCEEDED`를 추가한다.

## R1. 지도 설정 저장과 전파

- **Decision**: `AppSettingsLocalDataSource`(DataStore 키 `map_display_type`, 값 `"DEFAULT"|"SATELLITE"`) + `AppSettingsRepository.observeMapDisplayType(): Flow<MapDisplayType>` / `setMapDisplayType(type)`. `MapViewModel`은 init에서 구독해 `MapUiState.mapType`에 반영하고, 기존 `MapIntent.ToggleMapType`은 로컬 토글 대신 Repository에 저장한다. `MapPlaceDetailScreen`의 하드코딩 `MapType.DEFAULT`도 구독값으로 바꾼다. feature/map의 `MapType` enum은 domain `MapDisplayType`으로 통합한다.
- **Rationale**: Q2에서 모든 지도 화면 동일 적용이 확정됐고, domain/README는 "화면이 변화를 계속 반영해야 하는 데이터부터 Flow 적용"을 허용한다. DataStore의 `data` Flow를 그대로 노출하면 마이페이지에서 바꾼 값이 지도 화면에 즉시 전파된다.
- **Alternatives considered**: (a) MapViewModel 상태만 공유 — 화면 간 ViewModel 공유가 없어 탈락. (b) 매 진입 시 `suspend get` — 이미 떠 있는 지도 화면에 반영되지 않아 SC-001 위배.

## R2. 기기 알림 상태 조회·설정 이동·서버 동기화

- **Decision**: feature/map `mypage/platform`에 expect/actual 두 개. `suspend fun isAppNotificationEnabled(): Boolean` (Android `NotificationManagerCompat.areNotificationsEnabled()`, iOS `UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler`를 `suspendCancellableCoroutine`으로 래핑) 과 `fun openAppNotificationSettings()` (Android `Settings.ACTION_APP_NOTIFICATION_SETTINGS` + `EXTRA_APP_PACKAGE`, iOS `UIApplication.openURL(openSettingsURLString)`). 화면은 `LifecycleEventEffect(ON_RESUME)`으로 재조회한다. 조회 결과가 바뀌면 `SyncNotificationSettingUseCase(enabled)`가 `MemberRepository.updateNotificationSetting(enabled)`를 호출하되 실패는 무시한다(로그만).
- **Rationale**: 스펙상 토글은 읽기 전용 표시이며 진실은 기기 설정에 있다. 서버 알림 설정 API가 이미 있으므로 푸시 도입 시 서버가 기기 상태와 어긋나지 않도록 동기화해 두는 편이 싸다. 실패를 무시하는 이유는 UX 요구(FR-008~011)가 서버와 무관하기 때문이다.
- **Alternatives considered**: 서버 설정값을 토글의 진실로 삼기 — 기기에서 알림을 꺼도 켜진 것으로 보여 스펙 위배. core 모듈에 두기 — 사용처가 마이페이지뿐이라 feature/map에 둔다(공용화 필요 시 core:ui로 승격).

## R3. 의견 보내기 API와 클라이언트 구조

- **Decision**: `POST /feedback` 계약 제안(인증 필수, Idempotency-Key 필수, 요청 `{type, content, appVersion, os, osVersion, deviceModel}`, 성공 200 `ApiResponse<Unit>`, 초과 429 `FEEDBACK_DAILY_LIMIT_EXCEEDED`, 검증 실패 400). 클라이언트는 `FeedbackApi` → `FeedbackRemoteDataSource(type: String, content: String, appVersion, os, osVersion, deviceModel)` → `FeedbackRepository.sendFeedback(type: FeedbackType, content: String, appInfo: AppInfo)` → `SendFeedbackUseCase(type: FeedbackType?, content: String)`가 `ensureAuthenticated()` 후 `type ?: ETC`, `content.trim()`, `AppInfoRepository.getAppInfo()`를 조합한다. ViewModel은 `LinkTripApiException.errorCode`로 초과/실패 토스트를 분기한다(429면 초과, 그 외 실패).
- **Rationale**: 디자인 설명이 "전송 시 앱 버전·OS·기기 정보 자동 첨부, 차단은 서버가 수행"으로 명시했다. 유형 기본값·트림·정보 첨부는 비즈니스 규칙이므로 UseCase에 둔다(domain/README). 하루 5회의 "하루" 경계는 서버 기준일(스펙 Assumptions).
- **Alternatives considered**: 이메일/외부 폼 열기 — 기존 spec v0.1.2의 TBD였으나 Figma가 인앱 바텀시트로 확정. 서버 API 확정 전까지 로컬 저장 후 전송 — 스펙에 없는 오프라인 큐라 탈락.

## R4. 앱 초기화 실행 순서와 원자성

- **Decision**: `ResetAppUseCase`: (1) `ensureAuthenticated()` (2) `TripPlanRepository.getTripPlans(cursor)`로 전체 페이지 수집(기존 `GetSavedTripPlansForMapUseCase`의 커서 루프와 같은 방식) (3) 각 id에 `deleteTripPlan(id)`, `NOT_FOUND_TRIP_PLAN`은 성공으로 간주 (4) 모두 성공하면 `AppSettingsRepository.clearAll()`(map_display_type, onboarding_completed, notification_prompted 제거) (5) `AuthRepository.logout()`(access_token 제거). `device_id`는 유지한다. 원격 단계에서 예외가 나면 로컬은 손대지 않고 예외를 올려 ViewModel이 실패 토스트(`앱 초기화에 실패했습니다. 다시 시도해주세요.`)를 띄운다.
- **Rationale**: 회원 삭제 API가 없어 단건 삭제 반복이 유일한 방법이다. 삭제는 소프트 삭제이고 404를 성공으로 보면 재시도가 멱등하다. FR-027(부분 상태 노출 금지)은 "로컬 초기화는 원격 완료 후"로 만족한다. device_id를 지우면 Android는 ANDROID_ID로 어차피 같은 값을 다시 얻으므로 의미가 없고, iOS는 새 UUID가 생겨 서버에 유령 회원이 남는다.
- **Alternatives considered**: 로컬 먼저 지우고 원격 삭제 — 실패 시 온보딩으로 갔다가 재로그인하면 옛 일정이 되살아나 스펙 위배. 서버 `DELETE /members/me` 제안 채택 시 (2)(3)을 한 번의 호출로 교체한다.
- **저장 장소·보관함 저장항목**: 현재 코드베이스에 여행 계획 외 별도 저장소가 없다(보관함 화면은 목업 상태, 장소는 여행 계획 아이템). 따라서 여행 계획 삭제로 스펙의 삭제 범위를 충족하며, 향후 보관함 저장소가 생기면 `ResetAppUseCase`에 합류시킨다.

## R5. 온보딩 상태와 알림 안내 노출 이력

- **Decision**: DataStore 키 `onboarding_completed`(Intro 종료 시 true 저장), `notification_prompted`(기존 feature/schedule의 SharedPreferences `NotificationPromptedKey`를 이관). 둘 다 `AppSettingsRepository`가 노출하고 `clearAll()`에 포함된다. `ScheduleNavDisplay`는 SharedPreferences 대신 `metroViewModel`로 주입한 작은 `NotificationPromptViewModel`(또는 Repository 직접 호출)로 읽고 쓴다.
- **Rationale**: Q1에서 최초 진입 상태 초기화가 확정됐다. 이력이 SharedPreferences에 흩어져 있으면 data 계층의 초기화가 닿지 않는다.
- **Alternatives considered**: IntroActivity 게이팅(온보딩 완료 시 건너뛰기)까지 이번에 구현 — 현재 앱은 항상 Intro를 보여주므로 스펙 시나리오(초기화 후 재실행 시 온보딩)가 이미 성립한다. 게이팅은 사이트맵상 별도 기능이라 이번 범위에서 제외하고 키만 마련한다.

## R6. 이용약관 웹뷰

- **Decision**: expect `@Composable fun PlatformWebView(url: String, onLoadingChanged: (Boolean) -> Unit, onError: () -> Unit, modifier)`; Android actual은 `AndroidView { WebView }` + `WebViewClient.onPageStarted/onPageFinished/onReceivedError`, iOS actual은 `UIKitView { WKWebView }` + `WKNavigationDelegate`. 외부 링크는 같은 웹뷰에서 열되 다른 도메인이면 시스템 브라우저로 넘긴다. URL은 `TermsRepositoryImpl`의 상수 맵(자리 표시 `https://linktrip.cloud/terms/{service|privacy|oss|location}`)에서 제공하고 domain `TermsDocument(type, title, url)`로 반환한다.
- **Rationale**: Q3 확정. 카탈로그에 웹뷰 라이브러리가 없고, 지도(`MapBackground`)가 이미 expect/actual 패턴이므로 의존성 추가 없이 일관되게 구현할 수 있다. URL을 data에 두면 나중에 원격 설정으로 바꿔도 domain·feature가 변하지 않는다.
- **Alternatives considered**: compose-webview-multiplatform 라이브러리 — 의존성·버전 관리 부담, 기능 요구가 단순해 불필요. 시스템 브라우저로 열기 — "앱을 벗어나지 않고" 요구 위배.

## R7. 앱·기기 정보 제공

- **Decision**: data/core `fun interface AppInfoProvider { fun getAppInfo(): AppInfoValue }`를 `DeviceIdProvider`와 같은 방식으로 `AndroidDataGraph`(PackageManager `versionName`, `Build.VERSION.RELEASE`, `Build.MODEL`)와 `IosAppGraph`(`NSBundle.mainBundle` `CFBundleShortVersionString`, `UIDevice.systemVersion`, `UIDevice.model`)가 제공한다. domain에는 `AppInfoRepository.getAppInfo(): AppInfo`로 노출한다.
- **Rationale**: domain은 순수 Kotlin이어야 하고, data 모듈에는 iosMain이 없어 플랫폼 그래프가 제공하는 기존 패턴을 재사용한다.
- **Alternatives considered**: `core:common`의 `platform()` 확장 — 버전·모델까지 필요해 부족. BuildKonfig 도입 — 새 플러그인 추가라 과하다.

## R8. 토스트·팝업 표시 방식

- **Decision**: 마이페이지 토스트 5종(지도 변경 2, 의견 3)과 초기화 실패는 `MyPageSideEffect.ShowToast(message, variant)`로 내보내고 화면 하단 `LinkItToast`(`ToastVariant` Success/Error)로 3초 표시한다. 초기화 완료 토스트는 화면이 Intro로 바뀌므로 Intent extra(Android)/콜백 파라미터(iOS)로 전달해 IntroActivity/IntroViewController가 표시한다. 초기화 팝업은 기존 `ResetDialog`를 `LinkItDialog(title, description, confirmText="초기화", showCloseButton=true)` + 보조 버튼 `돌아가기`로 교체한다.
- **Rationale**: MapScreen이 이미 상태 기반 피드백을 쓰고, 디자인 시스템 토스트/다이얼로그 컴포넌트가 있다. `PopupEffectManager`는 현재 화면에 연결된 호스트가 없어 새로 배선하는 비용이 더 크다.

## R9. iOS DI 수동 등록

- **Decision**: `IosAppGraph`에 `@Binds` 추가: `AppSettingsLocalDataSourceImpl`, `FeedbackRemoteDataSourceImpl`, `MemberRemoteDataSourceImpl`, `AppSettingsRepositoryImpl`, `FeedbackRepositoryImpl`, `TermsRepositoryImpl`, `MemberRepositoryImpl`, `AppInfoRepositoryImpl` + `@Provides AppInfoProvider`. ViewModel(`MyPageViewModel`, `TermsViewModel`)은 기존 `MyPageViewModel`과 같은 경로로 동작하는지 확인하고 MissingBinding 시 METRO_INSTRUCTION대로 수동 등록한다.

## R10. 모듈 배치

- **Decision**: `feature/map/mypage` 유지, 약관 화면도 `feature/map/mypage/terms`에 둔다.
- **Rationale**: 마이페이지는 Map 탭 백스택의 서브 라우트이며 `MapEntry`가 이미 라우팅한다. 별도 모듈은 home 조립·iOS 등록·convention 설정을 늘리지만 얻는 것이 없다. 화면 수가 늘어 분리가 필요해지면 그때 `feature/mypage`로 이동한다.

## R11. 초기화 완료 후 온보딩 이동

- **Decision**: Android는 Navigator 패턴을 따른다. `core:navigation/androidMain/navigator/feature/IntroNavigator`(마커) + `feature/intro/androidMain/navigator/IntroNavigatorImpl`(`FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK`, extra `EXTRA_SHOW_RESET_TOAST=true`). `HomeActivity`가 `IntroNavigator`를 주입받아 `HomeNavDisplay(onAppReset = { introNavigator.navigate(this) { putExtra(...); addFlags(...) }; finish() })`로 전달하고, `MapEntry` → `MyPageScreen(onAppReset)`까지 콜백으로 내려간다. iOS는 `HomeViewController(onAppReset: () -> Unit)` 콜백으로 Swift 측이 IntroViewController를 루트로 교체한다.
- **Rationale**: ARCHITECTURE의 Navigator 패턴과 "iOS는 콜백 람다" 규칙 그대로. CLEAR_TASK로 뒤로가기 시 마이페이지 복귀를 막는다(FR-026).
