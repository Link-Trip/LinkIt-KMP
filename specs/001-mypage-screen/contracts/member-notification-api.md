# 계약: 알림 수신 설정 변경 (기존 API 사용)

**서버**: `PUT /members/me/notification` — Swagger `Member` 태그 `updateNotificationSetting`, 이미 배포됨. 조회(GET) API는 없다(2026-09-23 재확인, TBD-06).
**클라이언트 역할**: 사용자가 마이페이지 `알림` 토글로 바꾼 앱 알림 수신 설정을 서버에 반영한다. 성공해야 로컬(DataStore `notification_enabled`)에 저장하고, 실패하면 토글을 되돌리고 실패 토스트를 띄운다. 기기 알림 권한 변화는 보내지 않는다(2026-09-23 개정, 이전에는 권한값 best-effort 동기화).

## 요청

| 항목 | 값 |
|---|---|
| Method / Path | `PUT /members/me/notification` |
| Headers | `Authorization: Bearer <accessToken>`, `Idempotency-Key: <uuid v4>`, `Content-Type: application/json` (모두 공통 플러그인이 자동 첨부, API 코드에서 직접 다루지 않음) |
| Body | `{ "enabled": true }` — `NotificationSettingRequest.enabled` 필수 |

## 응답

| HTTP | 바디 | 클라이언트 처리 |
|---|---|---|
| 200 | `{ status: 200, message: "OK", data: { enabled: true } }` | `NotificationSettingResponse.toDomain()` → `NotificationSetting(enabled)`. 응답 `enabled`를 로컬 `notification_enabled`에 저장 |
| 400 `BAD_REQUEST_VALIDATION` / `BAD_REQUEST_MISSING_IDEMPOTENCY_KEY` | ExceptionResponse | `LinkTripApiException` 전파 → 토글 되돌림 + 실패 토스트 |
| 401 `UNAUTHORIZED_AUTHENTICATION_FAILED` / `UNAUTHORIZED_TOKEN_EXPIRED` | ExceptionResponse | `EnsureAuthenticatedUseCase(forceRefresh=true)` 후 1회 재시도, 재실패는 전파 |
| 404 `NOT_FOUND_MEMBER` | ExceptionResponse | 전파 → 토글 되돌림 + 실패 토스트 |
| 409 `DUPLICATE_REQUEST` | ExceptionResponse | 전파 → 토글 되돌림 + 실패 토스트 |
| 429 `TOO_MANY_REQUESTS` | ExceptionResponse | 전파 → 토글 되돌림 + 실패 토스트 |
| 네트워크 오류 | — | 전파 → 토글 되돌림 + 실패 토스트 |

## 호출 시점

- 기기 알림 권한이 켜진 상태에서 사용자가 `알림` 행 또는 토글을 눌러 수신 설정을 바꿀 때만 호출한다. 요청 `enabled`는 바꾼 뒤 값이다.
- 호출 중(응답 전)에는 토글 재탭을 무시해 중복 PUT을 막는다.
- 마이페이지 진입·ON_RESUME 재조회, 기기 권한 변화, 앱 초기화 흐름에서는 호출하지 않는다.
- 조회 API가 없으므로 앱 시작 시 서버 값을 읽어오지 않는다. 로컬 기본값은 `true`이며, 서버 회원 기본값도 수신 허용으로 가정한다.

## 클라이언트 매핑

```
MemberApi.updateNotificationSetting(@Body NotificationSettingRequest): ApiResponse<NotificationSettingResponse>
MemberRemoteDataSource.updateNotificationSetting(enabled: Boolean): NotificationSettingResponse
MemberRepository.updateNotificationSetting(enabled: Boolean): NotificationSetting
AppSettingsRepository.observeNotificationEnabled(): Flow<Boolean> / setNotificationEnabled(enabled: Boolean)
UpdateNotificationSettingUseCase(enabled: Boolean)   // 인증 보장 → PUT(401 시 1회 재시도) → 성공 시 setNotificationEnabled. 실패는 전파
```
