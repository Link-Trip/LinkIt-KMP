# 계약: 앱 초기화 → 회원 탈퇴 (기존 API 사용)

**서버**: `DELETE /members/me` — Swagger `Member` 태그 "회원 탈퇴 (개인정보 파기)". 2026-09-21 재조회로 배포 확인.
**클라이언트 역할**: 앱 초기화(FR-025~027)에서 서버 측 사용자 데이터를 한 번의 호출로 지운다. 2026-09-18에 제안했던 "회원 유지형 초기화"와 달리 서버는 **회원 자체를 탈퇴 처리**하므로, 초기화 후 재로그인하면 같은 기기라도 새 회원으로 시작한다. 사용자 관점의 "첫 설치 상태"와 일치하므로 그대로 채택한다.

## 서버 동작 (Swagger description)

- 기기식별자(serialNumber)를 비가역 값으로 마스킹해 개인정보를 파기한다.
- FCM 토큰·플랫폼 정보를 제거하고, 회원의 여행 계획을 모두 소프트 삭제한다.
- 하나의 트랜잭션으로 처리되어 부분 삭제 상태가 남지 않는다 → FR-027 원자성 충족.
- 이미 탈퇴한 회원에 다시 호출해도 200 (`deletedTripPlanCount=0`).
- 탈퇴 후 동일 기기로 재로그인하면 새 회원으로 시작한다. 클라이언트는 탈퇴 성공 시 보관 중인 토큰을 폐기해야 한다.

## 요청

| 항목 | 값 |
|---|---|
| Method / Path | `DELETE /members/me` |
| Headers | `Authorization: Bearer <accessToken>`, `Idempotency-Key: <uuid v4>` (모두 공통 플러그인이 자동 첨부) |
| Body | 없음 |

## 응답

| HTTP | 바디 | 클라이언트 처리 |
|---|---|---|
| 200 | `{ status: 200, message: "OK", data: { deletedTripPlanCount: 3 } }` | 성공. `WithdrawMemberResponse.deletedTripPlanCount`는 로그 용도 |
| 400 `BAD_REQUEST_MISSING_IDEMPOTENCY_KEY` | ExceptionResponse | 실패 토스트 (공통 헤더 누락은 코드 결함) |
| 401 `UNAUTHORIZED_AUTHENTICATION_FAILED` / `UNAUTHORIZED_TOKEN_EXPIRED` / `UNAUTHORIZED_TOKEN_INVALID` | ExceptionResponse | `EnsureAuthenticatedUseCase(forceRefresh=true)` 후 1회 재시도, 재실패 시 실패 토스트 |
| 404 `NOT_FOUND_MEMBER` | ExceptionResponse | 이미 없는 회원이므로 **성공으로 간주**하고 로컬 초기화 진행 |
| 409 `DUPLICATE_REQUEST` | ExceptionResponse | 실패 토스트 (사용자 재시도 시 새 키로 전송) |

## 호출 순서 (`ResetAppUseCase`)

```
1. ensureAuthenticated()                         // 토큰 없으면 로그인(빈 회원이 생겨도 바로 탈퇴되므로 무해)
2. memberRepository.withdraw()                    // DELETE /members/me. NOT_FOUND_MEMBER는 성공 취급, 401은 forceRefresh 후 1회 재시도
3. appSettingsRepository.clearAll()               // map_display_type, onboarding_completed, notification_prompted 제거
4. authRepository.logout()                        // access_token 폐기(서버 요구 사항). device_id는 유지
```

2단계가 예외로 끝나면 3·4단계를 수행하지 않고 예외를 올린다. ViewModel은 `앱 초기화에 실패했습니다. 다시 시도해주세요.` 토스트를 띄우고 마이페이지에 남는다(FR-027).

## 클라이언트 매핑

```
MemberApi.withdraw(): ApiResponse<WithdrawMemberResponse>          // @DELETE("members/me")
MemberRemoteDataSource.withdraw(): WithdrawMemberResponse
MemberRepository.withdraw(): Int                                    // deletedTripPlanCount
ResetAppUseCase()                                                   // 위 순서
```

## 참고: 여행 계획 단건 삭제 반복 (폐기)

`GET /trip-plans` 커서 루프 + `DELETE /trip-plans/{id}` 반복 방식은 회원 탈퇴 API 배포로 폐기한다. 여러 요청에 걸쳐 부분 삭제 상태가 생길 수 있어 FR-027을 약하게만 만족했다.
