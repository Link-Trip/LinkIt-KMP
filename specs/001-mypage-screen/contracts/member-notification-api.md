# 계약: 알림 수신 설정 조회·변경 (기존 API 사용)

**서버**: `GET /members/me/notification`(Swagger `Member` 태그 `getNotificationSetting`, 2026-09-23 배포 확인)과 `PUT /members/me/notification`(`updateNotificationSetting`, 이미 배포됨). 두 API 모두 `ApiResponse<NotificationSettingResponse>`를 돌려준다.
**클라이언트 역할**: 마이페이지 진입 시 `GET`으로 서버의 앱 알림 수신 설정을 읽어 로컬(DataStore `notification_enabled`)을 맞추고, 사용자가 `알림` 토글로 바꾼 값을 `PUT`으로 반영한다. 서버 값이 기준이고 로컬은 표시용 캐시다. `PUT`은 성공해야 로컬에 저장하고, 실패하면 토글을 되돌리고 실패 토스트를 띄운다. 기기 알림 권한 변화는 보내지 않는다(2026-09-23 개정, 이전에는 권한값 best-effort 동기화). `GET` 실패는 알리지 않고 로컬 값을 유지한다(v0.3.1, TBD-06 해소).

## 1. 조회 — `GET /members/me/notification`

| 항목 | 값 |
|---|---|
| Method / Path | `GET /members/me/notification` |
| Headers | `Authorization: Bearer <accessToken>` (공통 플러그인이 자동 첨부). GET이라 `Idempotency-Key` 불필요 |
| Body | 없음 |

| HTTP | 바디 | 클라이언트 처리 |
|---|---|---|
| 200 | `{ status: 200, message: "OK", data: { enabled: true } }` | `NotificationSettingResponse.toDomain()` → `NotificationSetting(enabled)`. 응답 `enabled`를 로컬 `notification_enabled`에 저장 → Repository Flow로 토글 갱신. 한 번도 바꾸지 않은 회원도 `true` |
| 401 `UNAUTHORIZED_AUTHENTICATION_FAILED` / `UNAUTHORIZED_TOKEN_EXPIRED` | ExceptionResponse | `EnsureAuthenticatedUseCase(forceRefresh=true)` 후 1회 재시도, 재실패는 삼킴 |
| 404 `NOT_FOUND_MEMBER` | ExceptionResponse | 삼킴(로컬 유지). 탈퇴 직후 등 드문 경우 |
| 네트워크 오류 | — | 삼킴(로컬 유지), 토스트 없음 |

## 2. 변경 — `PUT /members/me/notification`

| 항목 | 값 |
|---|---|
| Method / Path | `PUT /members/me/notification` |
| Headers | `Authorization: Bearer <accessToken>`, `Idempotency-Key: <uuid v4>`, `Content-Type: application/json` (모두 공통 플러그인이 자동 첨부, API 코드에서 직접 다루지 않음) |
| Body | `{ "enabled": true }` — `NotificationSettingRequest.enabled` 필수 |

| HTTP | 바디 | 클라이언트 처리 |
|---|---|---|
| 200 | `{ status: 200, message: "OK", data: { enabled: true } }` | `NotificationSettingResponse.toDomain()` → `NotificationSetting(enabled)`. 응답 `enabled`를 로컬 `notification_enabled`에 저장 |
| 400 `BAD_REQUEST_VALIDATION` / `BAD_REQUEST_MISSING_IDEMPOTENCY_KEY` | ExceptionResponse | `LinkTripApiException` 전파 → 토글 되돌림 + 실패 토스트 |
| 401 `UNAUTHORIZED_AUTHENTICATION_FAILED` / `UNAUTHORIZED_TOKEN_EXPIRED` | ExceptionResponse | `EnsureAuthenticatedUseCase(forceRefresh=true)` 후 1회 재시도, 재실패는 전파 |
| 404 `NOT_FOUND_MEMBER` | ExceptionResponse | 전파 → 토글 되돌림 + 실패 토스트 |
| 409 `DUPLICATE_REQUEST` | ExceptionResponse | 전파 → 토글 되돌림 + 실패 토스트 |
| 429 `TOO_MANY_REQUESTS` | ExceptionResponse | 전파 → 토글 되돌림 + 실패 토스트 |
| 네트워크 오류 | — | 전파 → 토글 되돌림 + 실패 토스트 |

## 3. 호출 시점

- **GET**: `MyPageViewModel` init에서 1회. ON_RESUME(기기 설정 복귀), 기기 권한 변화, 앱 초기화 흐름에서는 호출하지 않는다. 응답 전에는 로컬 값을 그대로 표시한다(디자인에 로딩 상태 없음, 기본값이 양쪽 `true`라 깜빡임은 재설치 등 드문 경우에만 발생).
- **PUT**: 기기 알림 권한이 켜진 상태에서 사용자가 `알림` 행 또는 토글을 눌러 수신 설정을 바꿀 때만. 요청 `enabled`는 바꾼 뒤 값이다. 호출 중(응답 전)에는 토글 재탭을 무시해 중복 PUT을 막는다.
- **경합**: GET 응답 전에 사용자가 토글을 조작하면 ViewModel이 GET Job을 취소한다. 사용자 조작이 우선이며, 뒤늦은 GET 결과가 낙관적 반영값을 덮지 않는다.
- **앱 초기화**: `DELETE /members/me`로 회원이 사라지고 로컬 `notification_enabled`도 제거되므로 별도 처리 없음. 재로그인 시 새 회원의 서버 기본값과 로컬 기본값이 모두 `true`.

## 4. 클라이언트 매핑

```
MemberApi.getNotificationSetting(): ApiResponse<NotificationSettingResponse>                          // 2026-09-23 추가
MemberApi.updateNotificationSetting(@Body NotificationSettingRequest): ApiResponse<NotificationSettingResponse>
MemberRemoteDataSource.getNotificationSetting(): NotificationSettingResponse
MemberRemoteDataSource.updateNotificationSetting(enabled: Boolean): NotificationSettingResponse
MemberRepository.getNotificationSetting(): NotificationSetting
MemberRepository.updateNotificationSetting(enabled: Boolean): NotificationSetting
AppSettingsRepository.observeNotificationEnabled(): Flow<Boolean> / setNotificationEnabled(enabled: Boolean)
FetchNotificationSettingUseCase()                    // 인증 보장 → GET(401 시 1회 재시도) → 성공 시 setNotificationEnabled. CancellationException 외 예외는 삼킴
UpdateNotificationSettingUseCase(enabled: Boolean)   // 인증 보장 → PUT(401 시 1회 재시도) → 성공 시 setNotificationEnabled. 실패는 전파
```
