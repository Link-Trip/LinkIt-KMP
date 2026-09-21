# Quickstart: 마이페이지 검증 가이드

**Spec**: [spec.md](spec.md) | **Contracts**: [contracts/](contracts/) | **Data model**: [data-model.md](data-model.md)

## 전제

- Android Studio + JDK 17, `local.properties`에 `MAPS_API_KEY`
- 서버 `https://linktrip.cloud/api` 접근 가능. `POST /feedback`, `DELETE /members/me`는 제안 상태이므로 서버 배포 전에는 의견 전송이 항상 실패 토스트로 끝난다(성공 경로는 MockEngine 테스트로 검증).
- 브랜치 `improve/#41-mypage_settings`

## 자동 검증

```bash
# 도메인 UseCase 단위 테스트 (SendFeedback 기본값/트림, ResetApp 순서·404 무시·원격 실패 시 로컬 미변경, SyncNotification 예외 삼킴)
./gradlew :domain:allTests

# data 계층 (DTO 직렬화, Mapper, MockEngine으로 200/429/401 응답 처리)
./gradlew :data:allTests

# feature/map ViewModel + 스크린샷 (Robolectric, Roborazzi)
./gradlew :feature:map:testDebugUnitTest
```

스크린샷 골든 생성·검증 명령은 [docs/COMPOSE_IMPLEMENTATION_GUIDE.md](../../docs/COMPOSE_IMPLEMENTATION_GUIDE.md) "스크린샷 테스트 작성법" 3절을 따른다. 골든 대상: 마이페이지(알림 꺼짐 카드 있음 / 켜짐), 의견 바텀시트(초기 / 입력 후 활성), 초기화 팝업, 약관 목록, 약관 상세 로딩·실패.

```bash
# 전체 빌드 (Android + iOS 프레임워크, iOS 수동 바인딩 누락 시 여기서 MissingBinding)
./gradlew :app-android:assembleDebug
./gradlew :app-shared:linkDebugFrameworkIosSimulatorArm64
```

## 수동 시나리오 (스펙 User Story 대응)

| # | 스토리 | 절차 | 기대 결과 |
|---|---|---|---|
| 1 | US1 지도 설정 | 메인 지도 우상단 프로필 → `위성` 탭 → 뒤로 → 앱 강제 종료 후 재실행 → 장소 상세 진입 | 토스트 `위성 지도로 변경되었습니다.`, 메인·장소 상세 지도 모두 위성, 재실행 후 유지 |
| 2 | US1 동일 카드 재탭 | 선택된 카드 다시 탭 | 토스트 없음, 상태 유지 |
| 3 | US2 초기화 취소 | `앱 초기화` → `돌아가기` / 닫기 | 팝업 닫힘, 데이터 변화 없음 |
| 4 | US2 초기화 실행 | 저장 일정 3건 상태에서 `앱 초기화` → `초기화` | 온보딩 시작 화면 + `앱 초기화가 완료되었습니다.`, 뒤로가기로 복귀 불가, 온보딩 후 메인 지도 일정 0건·지도 `기본` |
| 5 | US2 초기화 실패 | 기내 모드에서 `초기화` | 실패 토스트, 마이페이지 유지, 로컬 설정 유지(지도 설정 그대로) |
| 6 | US3 의견 성공 | `의견 보내기` → 유형 미선택, 1자 입력 → `보내기` (서버 배포 후) | 시트 닫힘 + 성공 토스트, 서버 기록 type=ETC |
| 7 | US3 200자 | 250자 붙여넣기 | 200자만 남음 |
| 8 | US3 초과 | 같은 날 6번째 전송 | `최대 의견 전송 횟수를 초과했습니다.`, 시트·내용 유지 |
| 9 | US3 실패 | 기내 모드 전송 | `전송에 실패했습니다. 다시 시도해주세요.`, 시트·내용 유지 |
| 10 | US4 알림 꺼짐 | 기기 설정에서 앱 알림 끄고 마이페이지 진입 | 최상단 안내 카드 + 토글 off |
| 11 | US4 설정 이동·복귀 | `설정하러 가기` → 알림 켜고 앱 복귀 | 카드 사라짐, 토글 on (2초 이내) |
| 12 | US5 약관 | `이용약관` → 4개 항목 → `개인정보 처리방침` | 상세 상단 제목, 웹페이지 표시, 뒤로가기로 목록 → 마이페이지 |
| 13 | US5 오프라인 | 기내 모드에서 약관 항목 탭 → 온라인 복구 후 `다시 시도` | 안내 + 재시도 → 웹페이지 표시 |

## API 스모크 (curl)

```bash
TOKEN=$(curl -s -X POST https://linktrip.cloud/api/auth/login \
  -H 'Content-Type: application/json' -H "Idempotency-Key: $(uuidgen)" \
  -d '{"serialNumber":"qa-device-001"}' | jq -r .data.accessToken)

# 기존: 알림 설정 동기화
curl -s -X PUT https://linktrip.cloud/api/members/me/notification \
  -H "Authorization: Bearer $TOKEN" -H "Idempotency-Key: $(uuidgen)" \
  -H 'Content-Type: application/json' -d '{"enabled":true}'

# 제안(서버 배포 후): 의견 전송 — 6회째에 429 FEEDBACK_DAILY_LIMIT_EXCEEDED 기대
curl -s -X POST https://linktrip.cloud/api/feedback \
  -H "Authorization: Bearer $TOKEN" -H "Idempotency-Key: $(uuidgen)" \
  -H 'Content-Type: application/json' \
  -d '{"type":"ETC","content":"테스트","appVersion":"1.0","os":"ANDROID","osVersion":"15","deviceModel":"Pixel 8"}'
```

## 완료 기준

- 위 자동 검증 3개 태스크 통과, 스크린샷 골든 diff 없음
- 수동 시나리오 1~5, 9~13 통과 (6~8은 서버 `POST /feedback` 배포 후)
- iOS 시뮬레이터에서 마이페이지 진입·지도 설정·약관 웹뷰·초기화 후 Intro 전환 확인
