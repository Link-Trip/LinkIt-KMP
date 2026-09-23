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
`NotificationSetting(enabled: Boolean)` — `GET`·`PUT /members/me/notification` 응답 매핑(같은 `NotificationSettingResponse`). 성공 시 `enabled`를 로컬 `notification_enabled`에 저장한다.

### app/AppInfo
`AppInfo(appVersion: String, platform: String, osVersion: String, deviceModel: String)` — 의견 전송 시 자동 첨부. `platform`은 `"ANDROID" | "IOS"`(서버 `CreateFeedbackRequest.platform` enum과 동일). 서버 길이 상한(appVersion·osVersion 20, deviceModel 50)은 data 계층이 잘라 맞춘다.

### exception/LinkTripErrorCode (추가)
2026-09-21 Swagger 기준 클라이언트 enum에 없는 서버 코드 전부: `FEEDBACK_DAILY_LIMIT_EXCEEDED`(429), `BAD_REQUEST_FEEDBACK_TYPE`(400), `BAD_REQUEST_PLATFORM`(400), `NOT_FOUND_MEMBER`(404), `UNAUTHORIZED_TOKEN_EXPIRED`(401), `UNAUTHORIZED_TOKEN_INVALID`(401).

## 2. 로컬 저장 (DataStore Preferences, 파일 `linkit.preferences_pb`)

| 키 | 타입 | 기본값 | 쓰는 곳 | 초기화 시 |
|---|---|---|---|---|
| `access_token` (기존) | String | 없음 | AuthLocalDataSource | `logout()`으로 제거 |
| `device_id` (기존) | String | 없음 | AuthLocalDataSource | **유지** (R4) |
| `map_display_type` | String(enum name) | `DEFAULT` | AppSettingsLocalDataSource | 제거 |
| `onboarding_completed` | Boolean | false | AppSettingsLocalDataSource (Intro 종료 시 true) | 제거 |
| `notification_prompted` | Boolean | false | AppSettingsLocalDataSource (schedule 알림 안내 표시 후 true) | 제거 |
| `notification_enabled` | Boolean | true | AppSettingsLocalDataSource (마이페이지 진입 시 GET 응답 저장, 토글 → 서버 PUT 성공 후 저장. 서버 값의 표시용 캐시) | 제거 |

`AppSettingsLocalDataSource.clearAll()`은 위 앱 설정 키만 제거한다(인증 키는 Auth가 책임, 온보딩 키는 002에서 `OnboardingLocalDataSource`로 이관). `notification_enabled`는 `Flow`로 노출해 마이페이지가 구독한다.

## 3. 서버 리소스 (2026-09-23 Swagger 재조회 기준, 전부 배포됨)

| 리소스 | 메서드 | 상태 | 용도 |
|---|---|---|---|
| `/members/me/notification` | GET | 기존(2026-09-23 배포 확인) | 마이페이지 진입 시 앱 알림 수신 설정 조회 → 로컬 캐시 갱신 — [contracts/member-notification-api.md](contracts/member-notification-api.md). TBD-06 해소 |
| `/members/me/notification` | PUT | 기존 | 사용자가 토글로 바꾼 앱 알림 수신 설정 반영 — [contracts/member-notification-api.md](contracts/member-notification-api.md) |
| `/auth/login` | POST | 기존 | `ensureAuthenticated()` |
| `/feedback` | POST | 기존 | 의견 전송 — [contracts/feedback-api.yaml](contracts/feedback-api.yaml) |
| `/members/me` | DELETE | 기존 | 앱 초기화 시 회원 탈퇴(일정 전체 소프트 삭제 포함, 단일 트랜잭션) — [contracts/member-withdraw-api.md](contracts/member-withdraw-api.md) |

`GET /trip-plans` + `DELETE /trip-plans/{id}` 반복 방식은 회원 탈퇴 API 배포로 사용하지 않는다.

## 4. UI 상태 (feature/map/mypage)

### MyPageUiState
| 필드 | 타입 | 초기값 | 근거 |
|---|---|---|---|
| `mapDisplayType` | MapDisplayType | DEFAULT | FR-004/005, Repository Flow 구독 |
| `notificationStatus` | `UNKNOWN / ENABLED / DISABLED` | UNKNOWN | 기기 알림 권한. FR-008/009, Edge Case(조회 실패 시 카드 숨김·토글 disabled·off) |
| `isNotificationEnabled` | Boolean | true | 앱 알림 수신 설정. Repository Flow 구독(진입 시 GET 결과도 Flow로 반영). FR-011a/011e |
| `isNotificationUpdating` | Boolean | false | 서버 반영 중 재탭 무시. FR-011b |
| `notificationSwitch` (파생) | `enabled = status == ENABLED`, `checked = enabled && isNotificationEnabled` | — | FR-009/011a. UNKNOWN·DISABLED면 disabled·off |
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
`SelectMapDisplayType(type)`, `RefreshNotificationStatus(enabled: Boolean?)`, `OpenNotificationSettings`, `ToggleNotificationEnabled`, `OpenFeedbackSheet`, `CloseFeedbackSheet`, `SelectFeedbackType(type)`, `ChangeFeedbackContent(text)`, `SendFeedback`, `ShowResetDialog`, `DismissResetDialog`, `ConfirmReset`

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
UseCase 내부 순서: 인증 보장 → `DELETE /members/me` 회원 탈퇴(`NOT_FOUND_MEMBER`는 성공 취급) → 로컬 설정 clearAll → logout(토큰 폐기). 원격 단계 실패 시 로컬 미변경. 탈퇴 후 재로그인은 새 회원으로 시작한다.

### 의견 전송
```
SheetClosed ──OpenFeedbackSheet──▶ Editing(selectedType=null, content="")
Editing ──SendFeedback(canSend)──▶ Sending
Sending ──200──▶ SheetClosed + ShowToast(Success "의견 전송이 완료되었습니다.")
Sending ──429 FEEDBACK_DAILY_LIMIT_EXCEEDED──▶ Editing + ShowToast(Error "최대 의견 전송 횟수를 초과했습니다.")
Sending ──그 외 예외(429 TOO_MANY_REQUESTS rate limit 포함)──▶ Editing(내용 유지) + ShowToast(Error "전송에 실패했습니다. 다시 시도해주세요.")
Editing ──CloseFeedbackSheet──▶ SheetClosed (전송 없음)
```

### 알림 상태
```
진입(init, 1회) ──FetchNotificationSettingUseCase──▶ GET /members/me/notification
  성공 → setNotificationEnabled(응답) → Repository Flow → isNotificationEnabled 갱신
  실패 → 로컬 캐시 유지, 토스트 없음 (다음 진입 때 재시도)
  조회 중 ToggleNotificationEnabled → 조회 Job 취소 (사용자 조작 우선)

진입/ON_RESUME ──RefreshNotificationStatus──▶ 플랫폼 조회 (서버 호출 없음)
  성공 → ENABLED (카드 숨김, 토글 enabled, checked = isNotificationEnabled)
       | DISABLED (카드 표시, 토글 disabled·off)
  실패 → UNKNOWN (카드 숨김, 토글 disabled·off)
OpenNotificationSettings → NavigateToNotificationSettings(SideEffect) → 플랫폼 설정 화면

행/토글 탭:
  status != ENABLED → OpenNotificationSettings
  status == ENABLED → ToggleNotificationEnabled

ToggleNotificationEnabled (isNotificationUpdating=false일 때만):
  isNotificationEnabled = !current, isNotificationUpdating = true (낙관적 반영)
  UpdateNotificationSettingUseCase(!current)
    성공 → Repository Flow가 새 값 방출, isNotificationUpdating = false
    실패 → isNotificationEnabled = current(되돌림), isNotificationUpdating = false
           + ShowToast(Error "알림 설정 변경에 실패했습니다. 다시 시도해주세요.")
```

### 지도 설정
```
Repository Flow 값 → uiState.mapDisplayType
SelectMapDisplayType(type): type == 현재 → 무시 / 다르면 setMapDisplayType(type) 후 ShowToast(Success "위성|기본 지도로 변경되었습니다.")
```
