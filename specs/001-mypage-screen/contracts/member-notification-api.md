# 계약: 알림 수신 설정 동기화 (기존 API 사용)

**서버**: `PUT /members/me/notification` — Swagger `Member` 태그, 이미 배포됨.
**클라이언트 역할**: 기기 알림 허용 여부(진실)를 서버에 best-effort로 반영. UI는 서버 응답에 의존하지 않는다.

## 요청

| 항목 | 값 |
|---|---|
| Method / Path | `PUT /members/me/notification` |
| Headers | `Authorization: Bearer <accessToken>`, `Idempotency-Key: <uuid v4>`, `Content-Type: application/json` (모두 공통 플러그인이 자동 첨부, API 코드에서 직접 다루지 않음) |
| Body | `{ "enabled": true }` — `NotificationSettingRequest.enabled` 필수 |

## 응답

| HTTP | 바디 | 클라이언트 처리 |
|---|---|---|
| 200 | `{ status: 200, message: "OK", data: { enabled: true } }` | `NotificationSettingResponse.toDomain()` → `NotificationSetting(enabled)`. 사용처 없음(로그) |
| 400 `BAD_REQUEST_VALIDATION` / `BAD_REQUEST_MISSING_IDEMPOTENCY_KEY` | ExceptionResponse | `LinkTripApiException` — 무시(UI 영향 없음) |
| 401 `UNAUTHORIZED_AUTHENTICATION_FAILED` / `UNAUTHORIZED_TOKEN_EXPIRED` | ExceptionResponse | `EnsureAuthenticatedUseCase(forceRefresh=true)` 후 1회 재시도, 재실패 무시 |
| 404 `NOT_FOUND_MEMBER` | ExceptionResponse | 무시 |
| 409 `DUPLICATE_REQUEST` | ExceptionResponse | 무시 |
| 429 `TOO_MANY_REQUESTS` | ExceptionResponse | 무시 |

## 호출 시점

- 마이페이지 진입 및 ON_RESUME 재조회에서 기기 허용값이 **직전 조회값과 다를 때만** 호출한다(불필요한 PUT 방지).
- 앱 초기화 흐름에서는 호출하지 않는다.

## 클라이언트 매핑

```
MemberApi.updateNotificationSetting(@Body NotificationSettingRequest): ApiResponse<NotificationSettingResponse>
MemberRemoteDataSource.updateNotificationSetting(enabled: Boolean): NotificationSettingResponse
MemberRepository.updateNotificationSetting(enabled: Boolean): NotificationSetting
SyncNotificationSettingUseCase(enabled: Boolean)   // 실패 삼킴
```
