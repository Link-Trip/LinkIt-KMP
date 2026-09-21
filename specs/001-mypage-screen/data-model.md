# Data Model: 마이페이지

**Date**: 2026-09-18 | **Spec**: [spec.md](spec.md) | **Research**: [research.md](research.md)

## 1. 도메인 모델 (domain/model)

### settings/MapDisplayType
| 값 | 의미 | 비고 |
|---|---|---|
| `DEFAULT` | 기본 지도 | 초기값 |
| `SATELLITE` | 위성 지도 | |

- 앱 전체에서 단일 값. feature/map의 기존 `MapType` enum은 이 타입으로 교체한다.
- Android `GoogleMapType.NORMAL/SATELLITE`, iOS `MKMapTypeStandard/Satellite`로 매핑(feature 계층 책임).

### feedback/FeedbackType
| 값 | UI 라벨 | 서버 전송값 |
|---|---|---|
| `SUGGESTION` | 제안 | `SUGGESTION` |
| `BUG` | 오류·버그 | `BUG` |
| `ETC` | 기타 | `ETC` (미선택 시 기본값) |

### feedback (전송 단위, 별도 클래스 없음)
`FeedbackRepository.sendFeedback(type, content, appInfo)` 파라미터로 표현한다. 검증 규칙:
- `content`: trim 후 1자 이상, 원문 최대 200자(UI에서 차단, UseCase에서 `require`).
- `type`: null이면 `ETC`.

### terms/TermsDocumentType, terms/TermsDocument
| `TermsDocumentType` | 제목 |
|---|---|
| `SERVICE` | 서비스 이용약관 |
| `PRIVACY` | 개인정보 처리방침 |
| `OPEN_SOURCE` | 오픈소스 라이센스 고지 |
| `LOCATION` | 위치기반 서비스 이용약관 |

`TermsDocument(type: TermsDocumentType, title: String, url: String)`. 목록 순서는 위 표 순서로 고정. URL은 data의 `TermsRepositoryImpl` 상수(운영 확정 전 자리 표시).

### member/NotificationSetting
`NotificationSetting(enabled: Boolean)` — `PUT /members/me/notification` 응답 매핑.

### app/AppInfo
`AppInfo(appVersion: String, os: String, osVersion: String, deviceModel: String)` — 의견 전송 시 자동 첨부. `os`는 `"ANDROID" | "IOS"`.

### exception/LinkTripErrorCode (추가)
`FEEDBACK_DAILY_LIMIT_EXCEEDED`(429, 제안), `NOT_FOUND_MEMBER`(404), `UNAUTHORIZED_TOKEN_EXPIRED`(401).

## 2. 로컬 저장 (DataStore Preferences, 파일 `linkit.preferences_pb`)

| 키 | 타입 | 기본값 | 쓰는 곳 | 초기화 시 |
|---|---|---|---|---|
| `access_token` (기존) | String | 없음 | AuthLocalDataSource | `logout()`으로 제거 |
| `device_id` (기존) | String | 없음 | AuthLocalDataSource | **유지** (R4) |
| `map_display_type` | String(enum name) | `DEFAULT` | AppSettingsLocalDataSource | 제거 |
| `onboarding_completed` | Boolean | false | AppSettingsLocalDataSource (Intro 종료 시 true) | 제거 |
| `notification_prompted` | Boolean | false | AppSettingsLocalDataSource (schedule 알림 안내 표시 후 true) | 제거 |

`AppSettingsLocalDataSource.clearAll()`은 위 세 키만 제거한다(인증 키는 Auth가 책임).

## 3. 서버 리소스 (사용·제안)

| 리소스 | 메서드 | 상태 | 용도 |
|---|---|---|---|
| `/members/me/notification` | PUT | 기존 | 기기 알림 허용값 best-effort 동기화 |
| `/trip-plans` | GET(cursor) | 기존 | 초기화 시 전체 일정 id 수집 |
| `/trip-plans/{id}` | DELETE | 기존 | 초기화 시 단건 삭제(404는 성공 취급) |
| `/auth/login` | POST | 기존 | `ensureAuthenticated()` |
| `/feedback` | POST | **제안** | 의견 전송 — [contracts/feedback-api.yaml](contracts/feedback-api.yaml) |
| `/members/me` | DELETE | **제안** | 회원 데이터 일괄 초기화 — [contracts/member-reset-api.yaml](contracts/member-reset-api.yaml) |

## 4. UI 상태 (feature/map/mypage)

### MyPageUiState
| 필드 | 타입 | 초기값 | 근거 |
|---|---|---|---|
| `mapDisplayType` | MapDisplayType | DEFAULT | FR-004/005, Repository Flow 구독 |
| `notificationStatus` | `UNKNOWN / ENABLED / DISABLED` | UNKNOWN | FR-008/009, Edge Case(조회 실패 시 카드 숨김·토글 off) |
| `isResetDialogVisible` | Boolean | false | FR-023 |
| `isResetInProgress` | Boolean | false | Edge Case(진행 중 팝업 조작 불가) |
| `feedbackSheet` | `FeedbackSheetState?` | null | FR-012 (null = 닫힘) |

### FeedbackSheetState
| 필드 | 타입 | 초기값 | 규칙 |
|---|---|---|---|
| `selectedType` | FeedbackType? | null | 단일 선택, 재탭 시 해제(FR-013) |
| `content` | String | "" | 200자 초과 입력 무시(FR-014) |
| `isSending` | Boolean | false | 전송 중 버튼 비활성(FR-019) |
| `canSend` (파생) | Boolean | — | `content.isNotBlank() && !isSending` |

### MyPageIntent
`SelectMapDisplayType(type)`, `RefreshNotificationStatus`, `OpenNotificationSettings`, `OpenFeedbackSheet`, `CloseFeedbackSheet`, `SelectFeedbackType(type)`, `ChangeFeedbackContent(text)`, `SendFeedback`, `ShowResetDialog`, `DismissResetDialog`, `ConfirmReset`

### MyPageSideEffect
`ShowToast(message: String, variant: Success|Error)`, `NavigateToNotificationSettings`, `AppResetCompleted`

### TermsUiState (TermsViewModel)
`documents: List<TermsDocument>`(목록), 상세는 `TermsDetailScreen(route.type)`이 목록에서 찾아 `url`을 웹뷰에 넘김. 웹뷰 상태 `loadState: LOADING / CONTENT / ERROR`는 화면 로컬 상태.

## 5. 상태 전이

### 앱 초기화
```
Idle ──ShowResetDialog──▶ DialogVisible ──Dismiss/닫기──▶ Idle
DialogVisible ──ConfirmReset──▶ InProgress(isResetInProgress=true, 팝업 유지·조작 불가)
InProgress ──UseCase 성공──▶ AppResetCompleted(SideEffect) → 호스트가 Intro로 전환
InProgress ──UseCase 실패──▶ DialogVisible? 아니오 → Idle + ShowToast(Error "앱 초기화에 실패했습니다. 다시 시도해주세요.")
```
UseCase 내부 순서: 인증 보장 → 일정 전체 수집 → 각 삭제(404 무시) → 로컬 설정 clearAll → logout. 원격 단계 실패 시 로컬 미변경.

### 의견 전송
```
SheetClosed ──OpenFeedbackSheet──▶ Editing(selectedType=null, content="")
Editing ──SendFeedback(canSend)──▶ Sending
Sending ──200──▶ SheetClosed + ShowToast(Success "의견 전송이 완료되었습니다.")
Sending ──429 FEEDBACK_DAILY_LIMIT_EXCEEDED──▶ Editing + ShowToast(Error "최대 의견 전송 횟수를 초과했습니다.")
Sending ──그 외 예외──▶ Editing(내용 유지) + ShowToast(Error "전송에 실패했습니다. 다시 시도해주세요.")
Editing ──CloseFeedbackSheet──▶ SheetClosed (전송 없음)
```

### 알림 상태
```
진입/ON_RESUME ──RefreshNotificationStatus──▶ 플랫폼 조회
  성공 → ENABLED | DISABLED (변경 시 SyncNotificationSettingUseCase best-effort)
  실패 → UNKNOWN (카드 숨김, 토글 off)
OpenNotificationSettings → NavigateToNotificationSettings(SideEffect) → 플랫폼 설정 화면
```

### 지도 설정
```
Repository Flow 값 → uiState.mapDisplayType
SelectMapDisplayType(type): type == 현재 → 무시 / 다르면 setMapDisplayType(type) 후 ShowToast(Success "위성|기본 지도로 변경되었습니다.")
```
