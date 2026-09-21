# UI 구현 계획: 마이페이지 (#44)

**Branch**: `improve/#44-mypage_ui` | **Date**: 2026-09-18 | **범위**: [tasks.md](tasks.md)의 `[UI]` 태스크만
**기준 Figma**: [Pingo v3.0.3 마이페이지 섹션 17789-52610](https://www.figma.com/design/Pym5oUSWQjyWVtN86oj6lb/Pingo--v3.0.3?node-id=17789-52610&m=dev) (2026-03-12)

## 0. 원칙

- UI는 Figma 시안과 픽셀 단위로 일치시킨다. 각 화면은 구현 직전 `get_design_context`를 노드별로 다시 호출해 치수·토큰을 확정한다.
- 색·타이포·라운드는 `LinkItTheme` 토큰만 사용한다. 하드코딩 `Color(0x..)` 금지.
- 데이터 연동(#45)과 병행 가능하도록 [data-model.md §4](data-model.md) MVI 계약을 그대로 구현하고, 네트워크가 필요한 Intent는 ViewModel에 자리만 둔다.
- 검증은 Roborazzi 골든(375×744) + Figma 스크린샷 대조로 한다.

## 1. Figma 프레임 ↔ 구현 대상

| 프레임 노드 | 내용 | 구현 파일 | 태스크 |
|---|---|---|---|
| `18197:40381` | 마이페이지, 알림 꺼짐(안내 카드 + 토글 off) | `mypage/MyPageScreen.kt` | T013 T014 T034 |
| `18197:41176` | 마이페이지, 알림 켜짐(카드 없음 + 토글 on) | 〃 | T034 |
| `18197:41365` | 토스트 표시(`의견 전송이 완료되었습니다.`) | 〃 토스트 호스트 | T013 |
| `18212:35261` (`18212:35451` Popup) | 앱 초기화 확인 팝업 | `LinkItDialog` 확장 + `MyPageScreen` | T021 |
| `18197:34527` (`18197:38784` Modal) | 의견 보내기 바텀시트 | `mypage/FeedbackBottomSheet.kt` | T027 |
| `18287:116697` | 이용약관 목록(4행) | `mypage/terms/TermsListScreen.kt` | T037 |
| `18287:116612` | 이용약관 상세(웹 본문) | `mypage/terms/TermsDetailScreen.kt` + `platform/PlatformWebView` | T036 T037 |
| `18212:34914` | 온보딩 시작 + `앱 초기화가 완료되었습니다.` 토스트 | `feature/intro` | T022 |
| `18197:88237` | 디바이스 알림 설정(OS 화면, 구현 없음) | `platform/NotificationPermission` | T032 |

## 2. 디자인 토큰 매핑 (get_design_context 결과)

### 2.1 마이페이지 본문 (`18197:40381`)

| 요소 | Figma | Compose |
|---|---|---|
| 상단 네비 | Top Navigation Normal, 제목 중앙 `마이페이지` Headline 2 Bold, label/strong, 뒤로가기 chevronLeft | `LinkItTopNavigation(title, navigationIcon = TopNavigationDefaults.BackButton)` |
| 본문 컬럼 | gap 16, 섹션 내부 gap 12, 좌우 20 | `Column(verticalArrangement = spacedBy(16.dp))` |
| 섹션 제목 | Label 1 Normal SemiBold 14/20, label/neutral | `label1NormalSemibold`, `label.neutral` |
| 알림 안내 카드 | bg Violet/99, border 1 Violet/95, radius 16, padding 17, 아이콘 20(컬러 종 일러스트), 제목 Body 2 Bold label/normal, 설명 Label 2 Medium label/neutral, 버튼 `설정하러 가기` white bg, border line/normal/neutral, radius 8, 14×8, Caption 1 Bold primary/normal | `atomic.Violet99/Violet95`, `body2NormalBold`, `label2Medium`, `LinkItButton(Outlined, Primary, Small)` (radius 8 확인 후 필요 시 `shape` 지정) |
| 지도 카드 | Row gap 12, 각 weight 1, 이미지 92 높이 radius 12, 선택 border 2 primary/normal, 라벨 위 8, 선택 Label 1 Bold primary/normal, 미선택 Label 1 Medium label/alternative | 미리보기 PNG 2종(`ContentScale.Crop`), `label1NormalBold` / `label1NormalMedium` |
| 알림설정 행 | py 12, gap 12, 아이콘 Bell 20, `알림` Body 2 Medium label/normal, 우측 Switch 43.33×26.67(패딩 3.33, 썸 20 static/white, off fill/strong, on primary/normal) | `LinkItIcon.Utility.Bell`, 신규 `LinkItSwitch`(읽기 전용, 행 전체 클릭) |
| 도움말 행 | 컨테이너 px 20 gap 4, 행 py 12, 아이콘 20 + 텍스트 gap 8, chevronRight 20, 디바이더 1 line/solid/neutral | `Communication.Message`, 신규 `Utility.Document`, `Arrow.ChevronRight`, `line.solid.neutral` |
| 앱 초기화 행 | px 20 py 12, Refresh 아이콘, chevron 없음 | `Control.Refresh` |
| 토스트 | 하단 20 마진, 335×48, Positive/Negative | `LinkItToast` (MapScreen과 동일 배치) |

### 2.2 초기화 팝업 (`18212:35451`)

- 폭 320, radius 12, shadow xsmall, 배경 background/elevated/normal
- 본문 pt 20 px 20(+내부 20) pb 4, 제목 Body 1 Bold label/strong 중앙, 설명 Label 2 Medium label/alternative, gap 4
- 액션 px 20 py 20 gap 8: `초기화` bg status/negative radius 10 py 10 Label 1 Bold static/white / `돌아가기` Outlined border line/normal/neutral radius 10 py 9 Label 1 Regular label/normal
- 우상단 닫기 24(터치 40)
- → `LinkItDialog`에 `secondaryText/onSecondaryClick`, `confirmColors`(negative), `actionsEnabled` 파라미터 추가. 기존 호출부(MapScreen 등) 시그니처 유지.

### 2.3 의견 바텀시트 (`18197:38784`)

- 딤 material/dimmer, 시트 bg background/elevated/normal, 상단 radius 12
- 닫기 아이콘 우측 16, 상단 20
- 콘텐츠 pt 20 px 20 gap 12: 큰 아이콘 48(bg background/normal/alternative, radius 12, 📨 일러스트 32) → 제목 Heading 2 Bold(NanumSquare Neo, 20/28) label/normal → 설명 Body 2 Reading Regular label/neutral 3줄 (제목·설명 gap 8, 아이콘·텍스트 gap 16)
- 유형 칩 gap 8, pill(999), px 12 py 8, 13sp Bold: 선택 bg PaleBlue/99 border PaleBlue/80 text primary/normal, 미선택 bg background/normal/alternative text label/normal → `LinkItChip(colors=, shape=, contentPadding=, textStyle=label2Bold)` 오버라이드
- 입력창 `LinkItTextArea(maxLength = 200)` 최소 82, radius 12, placeholder label/assistive
- 액션 영역 `LinkItActionArea` + `LinkItButton(Large)` `보내기`, 비활성 bg interaction/disable text label/assistive, 하단 safe area 34
- 구현: material3 `ModalBottomSheet`(scrim = material.dimmer, dragHandle = null, shape 상단 12) 래퍼 `FeedbackBottomSheet` + 순수 콘텐츠 `FeedbackSheetContent`(스크린샷 대상)

### 2.4 이용약관 (`18287:116701`, `18287:116612`)

- 목록: `LinkItTopNavigation("이용약관")`, Main py 24, 행 규격은 도움말 행과 동일(Document 아이콘 + chevron + 디바이더)
- 상세: 제목 = 문서 제목, 본문 px 20 pt 24 웹뷰. 로딩 인디케이터·실패 문구 `약관을 불러오지 못했어요` + `다시 시도`(Figma 미정의 → `LinkItButton(Outlined, Assistive, Medium)`)

## 3. 사전 결정 사항 (UI 우선 구현을 위한 경계)

| 항목 | 결정 | 이유 |
|---|---|---|
| 도메인 enum | `MapDisplayType`, `FeedbackType`, `TermsDocumentType`+`TermsDocument`를 이 브랜치에서 domain에 추가 (T001 일부 선행) | UI 상태 타입이 이 enum에 의존. #45는 나머지(`NotificationSetting`, `AppInfo`, 에러 코드)만 추가 |
| `MapType` 교체(T009) | 하지 않음. 마이페이지만 `MapDisplayType` 사용 | DATA 범위 |
| ViewModel | data-model §4의 UiState/Intent/SideEffect를 전부 정의. 로컬 Intent(시트 열기/닫기, 칩 선택, 200자 제한, 팝업, 지도 선택 + 토스트, 알림 상태 갱신)는 구현. `SendFeedback`/`ConfirmReset`은 Repository 주입 전까지 `TODO(#45)` 로 상태 전이만 수행 | UI 동작·스크린샷 검증에 필요한 부분만 |
| 지도 미리보기 이미지 | Figma PNG 2종(5.3MB/14.9MB)을 `sips`로 3x(486×276)로 축소해 `feature/map/composeResources/drawable/mypage_map_default.png`, `mypage_map_satellite.png` | 원본 크기는 앱 번들에 부적합 |
| 컬러 일러스트(🔔 20, 📨 32) | Figma SVG를 PNG(3x)로 변환해 composeResources에 추가. 변환 불가 시 노드 `get_screenshot` 결과 사용 | 다색 일러스트라 `LinkItIcon` 단색 벡터 파이프라인 대상 아님 |
| Document 아이콘 | Figma SVG(`7ad6bb0e…`)를 gen_icons 파이프라인으로 `LinkItIcon.Utility.Document`에 추가 | 단색 24px 아이콘 |
| Switch | `core/designsystem/component/switch/LinkItSwitch.kt` 신규(Figma Switch/Switch 규격) | 디자인시스템에 토글 부재 |
| 바텀시트 | material3 `ModalBottomSheet` 사용, 콘텐츠는 분리 | 키보드·뒤로가기·드래그 플랫폼 동작 확보, 스크린샷은 콘텐츠 단위 |
| ON_RESUME 재조회 | `LifecycleEventEffect` (jetbrains lifecycle-runtime-compose). feature/map에 의존성 없으면 추가 | T034 |

## 4. 작업 순서

### Phase A. 기반 (병렬 가능)
1. **A1** domain enum 3종 + `TermsDocument` (data-model §1)
2. **A2** `LinkItNavKey.Terms`, `TermsDetail(type)` + serializer 등록 (T003)
3. **A3** designsystem: `LinkItSwitch`, `LinkItDialog` 확장, `LinkItIcon.Utility.Document`
4. **A4** 리소스: 지도 미리보기 PNG 2, 일러스트 PNG 2

### Phase B. MVI 계약 + 마이페이지 본체
5. **B1** `MyPageUiState`(`mapDisplayType`, `notificationStatus`, `isResetDialogVisible`, `isResetInProgress`, `feedbackSheet`), `FeedbackSheetState`, `MyPageIntent` 11종, `MyPageSideEffect` 3종, `MyPageViewModel` 로컬 처리 + 토스트 SideEffect
6. **B2** `MyPageScreen` 재작성: 상단 네비, 알림 카드(DISABLED만), 지도 설정, 알림설정 행, 도움말 행 2, 앱 초기화 행, 토스트 호스트(3초, 최신 1개) — T013 T014 T034
7. **B3** 초기화 팝업 교체 + 진행 중 비활성 — T021
8. **B4** `FeedbackBottomSheet` + `의견 보내기` 행 연결 — T027

### Phase C. 플랫폼 경계
9. **C1** `platform/NotificationPermission.kt` expect + android/ios actual, `LifecycleEventEffect(ON_RESUME)` → `RefreshNotificationStatus`, `NavigateToNotificationSettings` 처리 — T032
10. **C2** `platform/PlatformWebView.kt` expect + android(WebView)/ios(WKWebView) actual — T036

### Phase D. 약관 화면 + 라우팅
11. **D1** `TermsListScreen`, `TermsDetailScreen`(로딩/실패/재시도 로컬 상태), `MapEntry` entry 2개, `MyPageScreen(onOpenTerms)` — T037. 목록 데이터는 `TermsDocumentType` 4종 + 자리 표시 URL 상수(TermsRepository는 #45)
12. **D2** `IntroActivity` extra `EXTRA_SHOW_RESET_TOAST` 수신 → `IntroScreen(showResetCompletedToast)` 토스트, `IntroViewController(showResetCompletedToast)` — T022

### Phase E. 검증
13. **E1** `MyPageScreenshotTest` 갱신: 기본/위성, 카드 있음/없음, 팝업, 시트 초기/입력 — T016 T028 T034
14. **E2** `terms/TermsScreenshotTest`: 목록/로딩/실패 — T038
15. **E3** 빌드 게이트: `:feature:map:testDebugUnitTest`, `:core:designsystem:testDebugUnitTest`, `:app-android:assembleDebug`, `:app-shared:linkDebugFrameworkIosSimulatorArm64`
16. **E4** Figma 대조: 골든 vs `get_screenshot` 스크린샷 비교(`scripts/compare-figma.sh` 활용), 어긋난 치수 수정
17. **E5** quickstart 수동 시나리오 1·2·3(팝업)·9(시트 닫기)·10~13 에뮬레이터/시뮬레이터 확인, PR 기록 — T041

## 5. 스크린샷 테스트 케이스

| 테스트 | 상태 | Figma 대조 |
|---|---|---|
| `myPage_notificationDisabled` | DISABLED, DEFAULT | `18197:40381` |
| `myPage_notificationEnabled` | ENABLED, DEFAULT | `18197:41176` |
| `myPage_satelliteSelected` | ENABLED, SATELLITE | — |
| `myPage_resetDialog` | `isResetDialogVisible` | `18212:35261` |
| `myPage_toast` | 토스트 표시 | `18197:41365` |
| `feedbackSheet_initial` | 칩 미선택, 빈 입력 | `18197:34527` |
| `feedbackSheet_filled` | `제안` 선택 + 입력, 보내기 활성 | — |
| `terms_list` / `terms_detailLoading` / `terms_detailError` | — | `18287:116697` / `18287:116612` / 미정의 |

## 6. 리스크

- **Popup/ModalBottomSheet 캡처**: Roborazzi `onRoot()` 캡처가 별도 윈도우를 포함하지 않으면 `captureScreenRoboImage()` 또는 콘텐츠 단위 캡처로 전환.
- **폰트**: 시트 제목이 NanumSquare Neo ExtraBold. `heading2Bold` 토큰이 해당 패밀리를 쓰는지 구현 시 확인.
- **에셋 만료**: Figma 로컬 에셋 URL은 약 7일 유효. Phase A4를 먼저 수행.
- **iOS 웹뷰/알림 권한**: 시뮬레이터에서만 검증 가능. `linkDebugFrameworkIosSimulatorArm64` 통과를 게이트로 둔다.
