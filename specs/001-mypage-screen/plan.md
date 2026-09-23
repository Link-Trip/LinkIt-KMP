# Implementation Plan: 마이페이지

**Branch**: `improve/#41-mypage_settings` (spec dir `001-mypage-screen`) | **Date**: 2026-09-18 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-mypage-screen/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

마이페이지(Figma Pingo v3.0.3, 2026-03-12)를 스펙대로 완성한다. 기존 `feature/map/mypage`의 화면 골격(지도 설정 카드, 초기화 팝업)은 유지하고, 다음을 추가·교체한다.

- **지도 설정 영속화**: DataStore에 저장하고 `Flow`로 관찰해 메인 지도·장소 상세 등 모든 지도 화면이 같은 값을 쓴다. 변경 토스트를 표시한다.
- **알림 상태** (2026-09-23 개정): 기기 알림 권한이 꺼져 있으면 안내 카드와 disabled·off 토글을 표시하고 탭 시 기기 알림 설정 화면으로 이동한다. 권한이 켜져 있으면 토글이 enabled이고 앱 알림 수신 설정(DataStore `notification_enabled`)에 따라 on/off를 표시하며, 탭 시 전환하고 서버 `PUT /members/me/notification`에 반영한다(실패 시 되돌림 + 토스트). 복귀 시 권한을 재조회한다.
- **의견 보내기**: 바텀시트(유형 칩·200자 입력·보내기)와 완료/초과/실패 토스트. 서버 `POST /feedback`(2026-09-21 배포 확인) 계약대로 구현한다.
- **이용약관**: 목록 화면과 상세 웹뷰 화면(expect/actual). 로딩·실패·재시도 상태를 가진다.
- **앱 초기화**: 서버 `DELETE /members/me` 회원 탈퇴(2026-09-21 배포 확인, 일정 전체 소프트 삭제 포함 단일 트랜잭션) → 로컬 DataStore 초기화 → 로그아웃(토큰 폐기) → 온보딩 시작 화면으로 이동 + 완료 토스트. 재로그인 시 새 회원으로 시작한다.

기술 접근은 프로젝트 문서(ARCHITECTURE, data/README, domain/README, METRO_INSTRUCTION, NAVIGATION_STRUCTURE)를 그대로 따른다. API 상세 판단은 [research.md](research.md), 계약은 [contracts/](contracts/)에 있다.

## Technical Context

**Language/Version**: Kotlin 2.2.20, Kotlin Multiplatform (Android + iosArm64/iosSimulatorArm64), Compose Multiplatform

**Primary Dependencies**: Metro DI 0.10.4(+ MetroX ViewModel/Compose), Ktor + Ktorfit(API), kotlinx.serialization, AndroidX DataStore Preferences(KMP), Navigation3(멀티 백스택), Google Maps Compose(Android) / MapKit(iOS), Roborazzi(스크린샷 테스트), Robolectric

**Storage**: DataStore Preferences 단일 파일 `linkit.preferences_pb`(access_token, device_id 기존) — 이번에 `map_display_type`, `onboarding_completed`, `notification_prompted` 키 추가, 2026-09-23 `notification_enabled` 추가. 서버 데이터는 LinkTrip API(여행 계획)

**Testing**: `kotlin-test`(domain/data commonTest, Ktor MockEngine), Robolectric + JUnit4(feature androidUnitTest, ViewModel 테스트), Roborazzi(스크린샷 골든)

**Target Platform**: Android(minSdk 프로젝트 설정, Android 13+ 알림 권한), iOS 15+ (MapKit, WKWebView, UNUserNotificationCenter)

**Project Type**: mobile-app (KMP 멀티모듈 Clean Architecture + MVI)

**Performance Goals**: 지도 설정 반영 1초 이내(SC-001), 알림 상태 복귀 갱신 2초 이내(SC-006), 약관 웹페이지 3초 이내 표시(SC-007)

**Constraints**: feature→domain만 의존(data 직접 의존 금지), Repository 시그니처에 DTO 금지, non-GET API는 Idempotency-Key 자동 첨부(재시도 시 키 고정 주의), iOS는 `IosAppGraph` 수동 바인딩 필수, DataStore `@SingleIn(DataScope::class)`, 의견 전송 문자열 상한(appVersion·osVersion 20자, deviceModel 50자)은 클라이언트가 잘라 전송

**Scale/Scope**: 화면 4개(마이페이지, 의견 바텀시트, 약관 목록, 약관 상세) + 지도 화면 2곳 수정 + 온보딩 진입 연결. API: 3개 사용(알림 설정 PUT, 의견 전송 POST, 회원 탈퇴 DELETE) — 2026-09-21 Swagger 기준 모두 배포됨

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

`.specify/memory/constitution.md`는 아직 템플릿 상태(원칙 미정의)이므로 constitution 자체의 게이트는 없다. 대신 프로젝트가 규범으로 삼는 문서를 게이트로 적용한다.

| 게이트 | 출처 | 판정 |
|---|---|---|
| feature 모듈은 domain·core만 의존, data 직접 의존 금지 | docs/ARCHITECTURE.md | PASS — 새 Repository 인터페이스는 domain, 구현은 data |
| Repository/UseCase 시그니처에 DTO 금지, 파라미터는 원시값·도메인 모델 | domain/README.md | PASS — [contracts/domain-contracts.md](contracts/domain-contracts.md) |
| 비즈니스 규칙은 UseCase로 분리, 단순 조회만 ViewModel 직접 호출 | domain/README.md | PASS — `SendFeedbackUseCase`, `ResetAppUseCase`, `SyncNotificationSettingUseCase`는 UseCase, 지도 설정 읽기/쓰기는 Repository 직접 호출 |
| DataSource는 Remote/Local 접미사, Impl에 `@ContributesBinding(DataScope::class)` | data/README.md | PASS |
| 새 Impl은 `IosAppGraph`에 `@Binds` 수동 등록 | docs/METRO_INSTRUCTION.md | PASS — 작업 목록에 포함 |
| 새 Route는 `LinkItNavKey` + serializer 등록 + feature Entry | docs/NAVIGATION_STRUCTURE.md | PASS — `Terms`, `TermsDetail` 추가 |
| Figma 구현 시 디자인 토큰·기존 컴포넌트 우선, 스크린샷 테스트 | docs/COMPOSE_IMPLEMENTATION_GUIDE.md | PASS — LinkItChip/TextArea/Toast/Dialog 재사용, Roborazzi 골든 추가 |
| API·DataSource에서 헤더/에러 바디 직접 처리 금지 | data/README.md | PASS — `LinkTripHeaders`·`HttpResponseValidator` 그대로 사용 |

**Post-design re-check (Phase 1 후)**: 위 판정 유지. 새로 도입한 expect/actual(웹뷰, 알림 권한, 설정 화면 이동, 앱 정보)은 각각 feature/map(UI 계층)과 data/core(플랫폼 정보)에 두어 domain 순수성을 지킨다. 복잡도 예외 없음.

## Project Structure

### Documentation (this feature)

```text
specs/001-mypage-screen/
├── plan.md              # This file
├── spec.md              # 확정 스펙 (Clarifications 5건 반영)
├── research.md          # Phase 0: 기술 결정 R1~R11
├── data-model.md        # Phase 1: 도메인 모델·DataStore 키·상태 전이
├── quickstart.md        # Phase 1: 검증 시나리오·명령
├── contracts/
│   ├── feedback-api.yaml            # [기존] POST /feedback (2026-09-21 배포 확인, platform 필드)
│   ├── member-withdraw-api.md       # [기존] DELETE /members/me 회원 탈퇴 사용 계약 (구 member-reset-api.yaml 제안 대체)
│   ├── member-notification-api.md   # [기존] PUT /members/me/notification 사용 계약
│   └── domain-contracts.md          # Repository/UseCase/MVI/Route/expect-actual 시그니처
├── checklists/requirements.md
└── tasks.md             # Phase 2 output (/speckit-tasks — 이 명령에서는 생성하지 않음)
```

### Source Code (repository root)

```text
domain/src/commonMain/kotlin/com/linkit/company/domain/
├── model/settings/MapDisplayType.kt                 # 신규 enum DEFAULT/SATELLITE
├── model/feedback/FeedbackType.kt                    # 신규 enum SUGGESTION/BUG/ETC
├── model/terms/TermsDocument.kt                      # 신규 (type, title, url) + TermsDocumentType
├── model/member/NotificationSetting.kt               # 신규 (enabled)
├── model/app/AppInfo.kt                              # 신규 (appVersion, platform, osVersion, deviceModel)
├── repository/AppSettingsRepository.kt               # 신규 지도 설정·온보딩·알림 안내 이력·전체 초기화
├── repository/FeedbackRepository.kt                  # 신규
├── repository/TermsRepository.kt                     # 신규
├── repository/MemberRepository.kt                    # 신규 알림 설정 변경
├── repository/AppInfoRepository.kt                   # 신규 앱/기기 정보 조회
├── exception/LinkTripException.kt                    # 수정 에러 코드 추가(FEEDBACK_DAILY_LIMIT_EXCEEDED, NOT_FOUND_MEMBER, UNAUTHORIZED_TOKEN_EXPIRED)
└── usecase/
    ├── SendFeedbackUseCase.kt                        # 신규 유형 기본값·트림·앱 정보 첨부·인증 보장
    ├── ResetAppUseCase.kt                            # 신규 회원 탈퇴(DELETE /members/me) → 로컬 초기화 → 로그아웃
    └── SyncNotificationSettingUseCase.kt             # 신규 기기 권한값을 서버에 best-effort 반영

data/src/commonMain/kotlin/com/linkit/company/data/
├── api/FeedbackApi.kt                                # 신규 POST feedback
├── api/MemberApi.kt                                  # 신규 PUT members/me/notification, DELETE members/me
├── core/AppInfoProvider.kt                           # 신규 fun interface (플랫폼 그래프가 제공)
├── dto/feedback/CreateFeedbackRequest.kt             # 신규 internal
├── dto/member/NotificationSettingRequest.kt          # 신규 internal
├── dto/member/NotificationSettingResponse.kt         # 신규 public
├── dto/member/WithdrawMemberResponse.kt              # 신규 public (deletedTripPlanCount)
├── mapper/MemberMapper.kt                            # 신규
├── datasource/settings/AppSettingsLocalDataSource.kt (+Impl)   # 신규 DataStore 키
├── datasource/feedback/FeedbackRemoteDataSource.kt (+Impl)     # 신규
├── datasource/member/MemberRemoteDataSource.kt (+Impl)         # 신규
├── repository/AppSettingsRepositoryImpl.kt           # 신규
├── repository/FeedbackRepositoryImpl.kt              # 신규
├── repository/TermsRepositoryImpl.kt                 # 신규 (URL 상수 보유, 원격 없음)
├── repository/MemberRepositoryImpl.kt                # 신규
├── repository/AppInfoRepositoryImpl.kt               # 신규 (AppInfoProvider 위임)
└── (androidMain) AndroidDataGraph.kt                 # 수정 AppInfoProvider 제공

app-shared/src/iosMain/kotlin/com/linkit/company/IosAppGraph.kt   # 수정 @Binds 9개 + AppInfoProvider 제공

core/navigation/src/
├── commonMain/.../LinkItRoute.kt                     # 수정 Terms, TermsDetail(type) 라우트 + serializer
└── androidMain/.../navigator/feature/IntroNavigator.kt   # 신규 마커 인터페이스

feature/intro/src/androidMain/.../navigator/IntroNavigatorImpl.kt  # 신규 (CLEAR_TASK, 완료 토스트 extra)
feature/intro/src/androidMain/.../IntroActivity.kt    # 수정 초기화 완료 토스트 표시

feature/home/src/
├── androidMain/.../HomeActivity.kt                   # 수정 IntroNavigator 주입, onAppReset 콜백
├── iosMain/.../HomeViewController.kt                 # 수정 onAppReset 콜백 파라미터
└── commonMain/.../navigation/HomeNavDisplay.kt       # 수정 onAppReset 전달

feature/map/src/
├── commonMain/.../mypage/
│   ├── MyPageScreen.kt                               # 수정 알림 카드·알림설정·의견 보내기·토스트 호스트
│   ├── MyPageUiState.kt / MyPageIntent.kt / MyPageSideEffect.kt / MyPageViewModel.kt   # 수정
│   ├── FeedbackBottomSheet.kt                        # 신규
│   ├── terms/TermsListScreen.kt, TermsDetailScreen.kt, TermsViewModel.kt   # 신규
│   ├── platform/NotificationPermission.kt            # 신규 expect (상태 조회·설정 화면 이동)
│   └── platform/PlatformWebView.kt                   # 신규 expect
├── androidMain/.../mypage/platform/*.android.kt      # 신규 actual (NotificationManagerCompat, Intent, WebView)
├── iosMain/.../mypage/platform/*.ios.kt              # 신규 actual (UNUserNotificationCenter, openSettingsURL, WKWebView)
├── commonMain/.../main/navigation/MapEntry.kt        # 수정 Terms 라우트 entry, onAppReset 전달
├── commonMain/.../main/MapViewModel.kt               # 수정 AppSettingsRepository 구독, ToggleMapType → 저장
├── commonMain/.../main/MapPlaceDetailScreen.kt       # 수정 하드코딩 DEFAULT 제거
└── androidUnitTest/.../mypage/                       # 수정·신규 ViewModel 테스트 + Roborazzi 골든

feature/schedule/src/androidMain/.../navigation/ScheduleNavDisplay.kt   # 수정 알림 안내 이력 SharedPreferences → AppSettingsRepository
```

**Structure Decision**: 마이페이지는 이미 `feature/map/mypage`에 있고 Map 탭 백스택의 서브 라우트로 연결되어 있으므로 별도 `feature/mypage` 모듈을 만들지 않는다(연구 R10). 새 도메인 개념(설정·의견·약관·회원·앱 정보)은 domain/data 규칙대로 도메인별 하위 패키지로 나눈다. 플랫폼 의존 UI(웹뷰·알림 권한)는 feature/map의 expect/actual로, 플랫폼 정보 제공자는 data/core의 `AppInfoProvider`로 두어 `DeviceIdProvider`와 같은 패턴을 따른다.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

위반 없음.

## 구현 단계 개요 (tasks.md 생성 시 참고)

1. **domain**: 모델·Repository 인터페이스·에러 코드·UseCase 3종 + commonTest
2. **data**: DTO·API·DataSource·RepositoryImpl·AppInfoProvider + Android/iOS 그래프 등록 + MockEngine 테스트
3. **지도 설정 연결**: MapViewModel/MapPlaceDetailScreen이 `AppSettingsRepository.observeMapDisplayType()` 구독
4. **마이페이지 UI**: 알림 카드·알림설정 행·의견 바텀시트·토스트·팝업 문구 교체(`돌아가기`)
5. **약관**: Route 2개 + 목록/상세 화면 + expect/actual 웹뷰
6. **초기화 연결**: ResetAppUseCase → HomeActivity `IntroNavigator` → IntroActivity 완료 토스트, iOS 콜백
7. **알림 안내 이력 이관**: ScheduleNavDisplay SharedPreferences → DataStore
8. **테스트**: ViewModel(Robolectric) + Roborazzi 골든 + 도메인/데이터 단위 테스트, quickstart 시나리오 수동 검증

## 리스크와 대응

| 리스크 | 대응 |
|---|---|
| 의견 전송 429가 두 코드(`FEEDBACK_DAILY_LIMIT_EXCEEDED`, `TOO_MANY_REQUESTS`)로 온다 | HTTP 상태가 아니라 `errorCode`로 분기. rate limit은 일반 실패 토스트로 처리 |
| 의견 문자열 상한(appVersion·osVersion 20, deviceModel 50) 초과 시 400 | `FeedbackRepositoryImpl`에서 `take(n)`으로 잘라 전송, MockEngine 테스트로 검증 |
| 앱 초기화가 회원 탈퇴이므로 되돌릴 수 없음 | 스펙 팝업 문구(`앱을 초기화하면 다시 복구할 수 없어요`)와 일치. 초기화 중(`isResetInProgress`) 팝업 조작 차단, 실패 시 로컬 미변경 |
| 탈퇴 직후 재로그인이 새 회원을 만든다 | 스펙의 "첫 설치 상태"와 일치. `device_id`는 유지해도 서버가 옛 serialNumber를 마스킹하므로 옛 회원과 재연결되지 않음 |
| iOS 알림 권한 조회가 비동기 | `suspend` 기반 expect/actual로 통일, 초기값은 "미확인"으로 두고 카드 숨김(Edge Case 반영) |
| Idempotency-Key 재생성으로 재시도 멱등성 무력화 | 이번 범위에 `HttpRequestRetry` 도입 금지. 의견 재시도는 사용자 명시 탭에만 의존 |
