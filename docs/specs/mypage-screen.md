# 마이페이지 스펙

> 기준 Figma: [Pingo v3.0.3 - 마이페이지](https://www.figma.com/design/Pym5oUSWQjyWVtN86oj6lb/Pingo--v3.0.3?node-id=17789-52610&m=dev)  
> 기준 영역: `마이페이지`  
> 기준 section: `17789:52610` (디자인 설명 기준일 2026-03-12)  
> 스펙 버전: `v0.3.1`  
> 작성일: 2026-09-23  
> 상세 스펙: [specs/001-mypage-screen/spec.md](../../specs/001-mypage-screen/spec.md) (유저 스토리·FR·SC 단일 출처)

## 1. 한눈에 보기

마이페이지는 메인 지도 화면 우상단 프로필 버튼으로 진입하는 설정성 화면이다. 지도 표시 방식 선택, 기기 알림 상태 확인과 알림 수신 on/off, 의견 보내기, 이용약관 열람, 앱 초기화를 제공한다. v0.1.2의 `TBD-01~03`은 Figma v3.0.3 디자인 설명과 서버 API 확인(2026-09-21)으로 모두 결론이 났다.

| 영역 | 항목 | 동작 |
|---|---|---|
| 알림 안내 카드 | 기기 알림이 꺼져있어요 / 설정하러 가기 | 기기 알림 권한이 꺼져 있을 때만 최상단에 표시. 탭 시 기기 알림 설정 화면으로 이동 |
| 지도 설정 | 기본 / 위성 | 즉시 저장·토스트. 앱 안 모든 지도 화면에 반영, 앱 초기화 전까지 유지 |
| 알림설정 | 알림 (토글) | 권한 꺼짐: 토글 disabled·off, 행 탭 시 기기 설정 이동. 권한 켜짐: 토글 enabled, 앱 알림 수신 설정에 따라 on/off, 탭 시 수신 설정 전환 + `PUT /members/me/notification`. 진입 시 `GET /members/me/notification`으로 수신 설정을 서버 값에 맞춤 |
| 도움말 & 지원 | 의견 보내기 | 바텀시트에서 유형(제안/오류·버그/기타) + 200자 내용 전송. 하루 5회 제한 |
| 도움말 & 지원 | 이용약관 | 4종 목록 → 앱 내 웹뷰 상세(로딩·실패·재시도) |
| 앱 설정 | 앱 초기화 | 확인 팝업 → 서버 회원 탈퇴 → 로컬 설정 초기화 → 로그아웃 → 온보딩 시작 + 완료 토스트 |

## 2. 화면 상태별 읽기

골든 스크린샷은 `screenshots/feature/map/`(Roborazzi)에 있다. 각 케이스의 Figma 노드는 [specs/001-mypage-screen/ui-plan.md §5](../../specs/001-mypage-screen/ui-plan.md)에 대응표가 있다.

| 상태 | Figma 노드 | 설명 |
|---|---|---|
| 권한 꺼짐 | `18197:40381` | 안내 카드 + 토글 disabled·off |
| 권한 켜짐·수신 on | `18197:41176` | 카드 없음 + 토글 enabled·on, `지도 설정`이 첫 항목 |
| 권한 켜짐·수신 off | (Figma 없음, `18197:41176`의 토글 off 변형) | 카드 없음 + 토글 enabled·off |
| 토스트 | `18197:41365` | 하단 20 마진, 3초 자동 숨김, 최신 1개만 표시 |
| 초기화 팝업 | `18212:35261` | `정말 앱을 초기화 하시겠어요?` / `앱을 초기화하면 다시 복구할 수 없어요` / `초기화`(negative) / `돌아가기` / 닫기. 진행 중에는 조작 불가 |
| 의견 바텀시트 | `18197:34527` | 칩 미선택 시작, 재탭 해제, 미선택 전송은 `기타`. 공백 제외 1자 이상일 때 `보내기` 활성 |
| 약관 목록·상세 | `18287:116697` / `18287:116612` | 4행 목록, 상세는 웹뷰 + 로딩 인디케이터, 실패 시 `약관을 불러오지 못했어요` + `다시 시도` |
| 초기화 완료 | `18212:34914` | 온보딩 시작 화면 + `앱 초기화가 완료되었습니다.` |

## 3. 핵심 규칙

| 규칙 | 내용 |
|---|---|
| 지도 설정 저장 | DataStore `map_display_type`. `AppSettingsRepository.observeMapDisplayType()`를 메인 지도·장소 상세·마이페이지가 구독한다 |
| 알림 2계층 | (1) 기기 알림 권한: OS가 결정, 앱은 진입·복귀 시 조회만. (2) 앱 알림 수신 설정: 사용자가 토글로 정하는 값. 서버가 기준이고 DataStore `notification_enabled`(기본 true)는 표시용 캐시다. 토글은 권한이 켜져 있을 때만 enabled이고 수신 설정을 표시한다. 권한이 꺼져 있거나 조회 불가면 disabled·off |
| 알림 서버 동기화 | 사용자가 토글을 바꿀 때만 `PUT /members/me/notification {enabled}`. 낙관적으로 토글을 먼저 바꾸고, 성공 시 로컬 저장, 실패 시 토글 되돌림 + `알림 설정 변경에 실패했습니다. 다시 시도해주세요.` 토스트. 기기 권한 변화는 서버에 보내지 않는다. 마이페이지 진입 시 `GET /members/me/notification`으로 서버 값을 읽어 로컬을 맞춘다(2026-09-23 서버 배포, `TBD-06` 해소). 조회 실패는 알리지 않고 로컬 값을 유지하며, 조회 응답 전에 사용자가 토글을 조작하면 조회 결과는 버린다. 복귀(ON_RESUME)·권한 변화에는 재조회하지 않는다 |
| 의견 전송 | `POST /feedback`에 유형·내용·앱 버전·플랫폼·OS 버전·기기 모델을 첨부. 429 `FEEDBACK_DAILY_LIMIT_EXCEEDED`만 초과 토스트, 그 외 실패 토스트(시트·내용 유지) |
| 앱 초기화 범위 | 서버: `DELETE /members/me` 회원 탈퇴(일정 전체 소프트 삭제·FCM 제거·기기식별자 마스킹, 단일 트랜잭션). 로컬: 지도 설정·알림 수신 설정·온보딩 완료·알림 안내 이력 제거 + 토큰 폐기. `device_id`는 유지. 재로그인 시 새 회원 |
| 초기화 원자성 | 원격 실패 시 로컬 미변경 + 실패 토스트. 완료 후 뒤로가기로 복귀 불가(Android `NEW_TASK\|CLEAR_TASK`) |
| 약관 본문 | 운영 웹페이지를 앱 내 웹뷰로 표시. URL은 `TermsRepositoryImpl` 상수(운영 확정 전 자리 표시) |

## 4. 사용자 흐름

| 흐름 | 사용자가 하는 일 | 결과 |
|---|---|---|
| 진입 | 메인 지도 우상단 프로필 버튼 | 마이페이지(하단 탭 숨김). 알림 수신 설정을 서버에서 조회해 토글에 반영, 실패 시 로컬 값 유지 |
| 지도 설정 변경 | 다른 카드 탭 | 저장 + `위성\|기본 지도로 변경되었습니다.` 토스트. 같은 카드 재탭은 무시 |
| 알림 설정 이동 | `설정하러 가기` 또는 권한 꺼진 상태의 `알림` 행 | 기기 알림 설정 화면. 복귀 시 카드·토글 갱신 |
| 알림 수신 전환 | 권한 켜진 상태에서 `알림` 행 또는 토글 탭 | 토글 즉시 전환 → 서버 반영. 실패 시 되돌림 + 실패 토스트. 처리 중 재탭 무시 |
| 의견 보내기 | 행 탭 → 유형·내용 입력 → `보내기` | 성공: 시트 닫힘 + `의견 전송이 완료되었습니다.` / 초과: `최대 의견 전송 횟수를 초과했습니다.` / 실패: `전송에 실패했습니다. 다시 시도해주세요.` |
| 이용약관 | 행 탭 → 항목 탭 | 목록 → 상세 웹뷰. 뒤로가기로 목록 → 마이페이지 |
| 앱 초기화 | 행 탭 → `초기화` | 온보딩 시작 화면 + `앱 초기화가 완료되었습니다.` / 실패: 마이페이지 유지 + `앱 초기화에 실패했습니다. 다시 시도해주세요.` |
| 초기화 취소 | `돌아가기` / 닫기 | 팝업만 닫힘 |

## 5. 구현 위치

| 계층 | 파일 |
|---|---|
| 화면 | `feature/map/.../mypage/MyPageScreen.kt`, `FeedbackBottomSheet.kt`, `terms/TermsListScreen.kt`. 약관 상세 `TermsDetailScreen.kt`는 온보딩과 공유하도록 `core/ui/.../terms/`로 이동(002) |
| ViewModel | `mypage/MyPageViewModel.kt`, `main/MapPlaceDetailViewModel.kt`. `TermsViewModel.kt`는 `core/ui/.../terms/`로 이동(002) |
| 플랫폼 경계 | `mypage/platform/NotificationPermission.kt`. `PlatformWebView.kt`(expect/actual)는 `core/ui/.../terms/`로 이동(002) |
| 라우트 | `LinkItNavKey.MyPage`, `Terms`, `TermsDetail(type)` — `MapEntry`가 등록, Map 탭 백스택의 서브 라우트 |
| 도메인 | `AppSettingsRepository`(알림 수신 설정 `observeNotificationEnabled`·`setNotificationEnabled` 포함), `MemberRepository`, `FeedbackRepository`, `TermsRepository`, `AppInfoRepository`, `SendFeedbackUseCase`, `ResetAppUseCase`(002에서 `OnboardingRepository.clearAll()`·`TripPlanRepository.clearUncheckedTripPlans()` 추가), `UpdateNotificationSettingUseCase`(서버 반영 성공 시 로컬 저장, 실패는 전파. 기존 best-effort `SyncNotificationSettingUseCase`를 대체), `FetchNotificationSettingUseCase`(진입 시 서버 조회 → 로컬 저장, 실패는 삼킴) |
| 데이터 | `AppSettingsLocalDataSource`(DataStore, `map_display_type`·`notification_prompted`·`notification_enabled`), `MemberApi`(GET·PUT `members/me/notification`, DELETE `members/me`), `FeedbackApi`, `AppInfoProvider`(플랫폼 그래프 제공). 온보딩 완료 키는 002에서 `OnboardingLocalDataSource`로 이관 |
| 초기화 전환 | Android `IntroNavigator`/`IntroNavigatorImpl` → `IntroActivity(EXTRA_SHOW_RESET_TOAST)`. iOS `HomeViewController(onAppReset)` 콜백 |

## 6. 미정/정책 필요

| ID | 항목 | 내용 |
|---|---|---|
| `TBD-04` | 약관 웹페이지 주소 | 운영 측 확정 전 자리 표시 URL(`https://linktrip.cloud/terms/{service\|privacy\|oss\|location}`) 사용 중 |
| ~~`TBD-06`~~ | 알림 수신 설정 서버 조회 | **해소(v0.3.1)**: 2026-09-23 서버에 `GET /members/me/notification`(Swagger `Member/getNotificationSetting`)이 배포됐다. 마이페이지 진입 시 조회해 로컬 `notification_enabled`를 서버 값으로 맞춘다. 한 번도 바꾸지 않은 회원도 `true`로 내려온다. 같은 기기 재설치(Android `ANDROID_ID` 기반 회원)처럼 로컬만 초기화된 경우도 서버 값으로 복원된다 |
| ~~`TBD-05`~~ | 온보딩 완료 플래그 기록 시점 | **해소(002, v0.2.1)**: `CompleteOnboardingUseCase`가 `바로 시작하기`·튜토리얼 `건너뛰기`·분석 완료 `생성된 일정 확인하기` 세 곳에서 `true`를 기록한다. 키는 `OnboardingLocalDataSource`(`onboarding_completed`·`terms_agreed_at`)로 이관됐고, 앱 초기화는 앱 설정 2키 → 온보딩 2키·튜토리얼 단계 → 확인전 일정 집합 순으로 지운다. 초기화 완료 토스트는 온보딩 시작 화면(`OnboardingStartScreen`)이 표시한다 |

## 7. 변경 이력

| Version | Date | Author | 변경 사항 | 근거 |
|---|---|---|---|---|
| `v0.1.0` | 2026-06-14 | Codex | 마이페이지 스펙 최초 작성 | Figma metadata / 화면 캡처 / sitemap |
| `v0.1.1` | 2026-06-14 | Codex | 서브에이전트 리뷰 반영. 지도 타입 옵션과 반영 정책을 분리하고 sitemap의 프로필 페이지 명칭 차이 명시 | Spec Document Reviewer Agent 1차 리뷰 |
| `v0.1.2` | 2026-06-14 | Codex | 재리뷰 반영. 기본 진입 시 기본 지도 옵션 선택 상태 설명 보강 | Spec Document Reviewer Agent 2차 리뷰 |
| `v0.2.0` | 2026-09-21 | Claude | Figma v3.0.3 노드 `17789-52610` 기준으로 전면 개정. 알림 카드·알림설정·의견 바텀시트·약관 4종 추가, `TBD-01~03` 해소(지도 설정 영속화·인앱 의견 API·회원 탈퇴 기반 초기화), 구현 위치 표 추가 | specs/001-mypage-screen, 이슈 #41/#44/#45 |
| `v0.2.1` | 2026-09-21 | Claude | 온보딩 플로우(002) 조정 반영. `TBD-05` 해소(완료 플래그는 `CompleteOnboardingUseCase` 3곳에서 기록), 초기화 범위에 약관 동의 시각·튜토리얼 단계·확인전 일정 집합 추가, 완료 토스트 위치를 온보딩 시작 화면으로, 약관 상세·웹뷰·`TermsViewModel` 구현 위치를 `core:ui`로 갱신 | specs/002-onboarding-flow research R9, 이슈 #47/#49/#50 |
| `v0.3.0` | 2026-09-23 | Claude | 알림설정을 2계층으로 개정. 권한 꺼짐 시 토글 disabled·off + 안내 카드, 권한 켜짐 시 토글 enabled + 앱 알림 수신 설정(DataStore `notification_enabled`) on/off 표시·전환, 전환 시 `PUT /members/me/notification` 반영(실패 시 되돌림·토스트). 기기 권한 변화의 서버 동기화 제거, `TBD-06`(서버 조회 API 부재) 추가 | 요구사항 재정의(2026-09-23), Swagger `Member/updateNotificationSetting` |
| `v0.3.1` | 2026-09-23 | Claude | `TBD-06` 해소. 서버 `GET /members/me/notification` 배포에 맞춰 마이페이지 진입 시 알림 수신 설정을 서버 값으로 동기화(`FetchNotificationSettingUseCase`), 로컬 `notification_enabled`는 표시용 캐시로 전환. 조회 실패는 무시, 조회 중 토글 조작 시 조회 결과 폐기 | Swagger `Member/getNotificationSetting`(2026-09-23 재조회), 이슈 #53 |
