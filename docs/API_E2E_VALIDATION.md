# API 연결 E2E 검증 기록

> 2026-09-21 검증 기록. 통과 항목은 해당 시나리오의 관찰 결과이며, 전체 기능의 완전한 E2E 통과를 의미하지 않는다. 미검증·서버 실패 항목은 별도로 기록한다.

## 실행 환경과 범위

- Android `Medium_Phone` AVD, API 35, arm64에서 앱을 실행했다. 물리 기기 검증과는 구분한다.
- 실제 서버 `https://linktrip.cloud/api/`에 연결한 release APK를 사용했다. `debuggable=false`이며 설치용 서명만 debug 키를 사용했다. debug용 목업 지도·일정 데이터 검증이 아니다.
- APK와 화면 캡처: `/tmp/linkit-e2e-20260921.aaTAAD/`. 아래 링크는 검증한 로컬 환경의 임시 증거 경로다. 저장소에 이미지를 포함하지 않았으므로 장기 보관이 필요하면 별도 보관해야 한다.
- 기존 앱 데이터와 계정을 유지했다. 앱 데이터 삭제, 앱 초기화·회원 탈퇴, 기존 일정 삭제, 의견 전송은 수행하지 않았다.
- 첫 알림 안내 재검증에는 별도 임시 Android 게스트 사용자(id 10)를 만들었다. 기존 사용자(id 0)의 설정을 초기화하지 않았다.
- 검증 후 원래 사용자(id 0)로 복귀하고 이번에 만든 게스트(id 10)만 삭제했다. 게스트의 로컬 데이터는 복구 불가하며 서버 분석 요청 이력은 삭제하지 않았다. 기존 사용자 데이터와 최종 release 앱은 유지했고 에뮬레이터도 실행 상태로 남겼다.
- 오프라인 시험에서 바꾼 Wi-Fi·모바일 데이터는 모두 다시 켰고 최종 값1을 확인했다. 재부팅 이후 `AndroidRuntime:E` 크래시 로그는 발견되지 않았다.
- 일정 생성 검증에는 실제 `POST /video/analyze`를 사용했다. 읽기 전용 검증만 수행한 것은 아니다.
- API 계약·미제공 기능·FCM 설정 범위는 [메인·탐색 화면 API 연결 현황](MAIN_SCREEN_API_STATUS.md)을 참고한다.

## 실행 시나리오

| 시나리오 | 상태 | 관찰 결과 / 증거 |
| --- | --- | --- |
| 홈 진입·저장 일정 목록·지도 | 통과 | 저장 일정 0개 응답의 빈 상태와 실제 Google 지도 표시. [01-home-empty.png](/tmp/linkit-e2e-20260921.aaTAAD/01-home-empty.png) |
| 최종 release 설치·재시작 | 통과 | 기존 앱 데이터를 유지한 업데이트 설치 후 force-stop·재실행 및 AVD 재부팅 후 홈·실제 지도 정상 로드. [16-home-fixed.png](/tmp/linkit-e2e-20260921.aaTAAD/16-home-fixed.png) |
| 현재 위치 권한·재시도 | 통과 | 거부 → 재요청 → 이번 실행에만 허용 → 에뮬레이터 GPS 도쿄역(35.6812, 139.7671)으로 지도 카메라 이동 및 ‘일본, 도쿄’ 표시. 실제 휴대폰 GPS 측정과는 구분. [18-location-moved.png](/tmp/linkit-e2e-20260921.aaTAAD/18-location-moved.png) |
| 탐색 전체 목록 | 통과 | 서버 추천 영상 목록 표시. [02-explore-all.png](/tmp/linkit-e2e-20260921.aaTAAD/02-explore-all.png) |
| 국가 필터 | 통과 | 전체에서 일본으로 변경 후 해당 목록 표시. [03-explore-japan.png](/tmp/linkit-e2e-20260921.aaTAAD/03-explore-japan.png) |
| 지역 필터 빈 결과 | 통과 | 유럽 선택 시 조회 실패와 구분되는 빈 상태 표시. [04-explore-region-empty.png](/tmp/linkit-e2e-20260921.aaTAAD/04-explore-region-empty.png) |
| 테마 필터 | 통과 | 미식 여행·힐링 여행·액티비티의 빈 결과를 정상 표시. 빈 목록은 화면 연결 실패로 처리하지 않음 |
| 네트워크 실패·복구 | 통과 | 오프라인에서 오류 안내, 네트워크 복구 후 재시도로 정상 빈 결과 복귀. [05-explore-offline-error.png](/tmp/linkit-e2e-20260921.aaTAAD/05-explore-offline-error.png), [06-explore-retry-recovered.png](/tmp/linkit-e2e-20260921.aaTAAD/06-explore-retry-recovered.png) |
| 원본 영상 열기·복귀 | 통과 | 추천 영상의 실제 URL로 YouTube를 열고 앱으로 복귀. 전용 캡처는 별도 저장하지 않음 |
| 일정 생성 화면 추천 목록 | 통과 | 서버 추천 노출, 더보기·접기, URL 복사·붙여넣기 확인. [07-schedule-recommendations.png](/tmp/linkit-e2e-20260921.aaTAAD/07-schedule-recommendations.png) |
| 추천 카드 선택 | 통과 | 선택한 영상의 실제 URL이 입력란에 반영됨. [09-selected-video.png](/tmp/linkit-e2e-20260921.aaTAAD/09-selected-video.png) |
| 잘못된 링크 검증 | 통과 | 유효하지 않은 URL에 입력 오류를 표시. [08-invalid-link.png](/tmp/linkit-e2e-20260921.aaTAAD/08-invalid-link.png) |
| 중국 추천 영상 생성 요청 | 요청·화면 전환 통과, 분석은 서버 실패 | 영상 ID `PEprsGRAorM` 요청 후 실제 영상 제목·썸네일이 있는 분석 화면에 진입. [10-analysis-state.png](/tmp/linkit-e2e-20260921.aaTAAD/10-analysis-state.png) |
| 분석 실패 안내·다시 만들기 | 통과 | 홈에서 “영상 분석에 실패했어요. 다시 시도해 주세요.” 및 “다시 만들기” 표시, 다시 만들기로 생성 화면 진입 확인. [11-analysis-failed.png](/tmp/linkit-e2e-20260921.aaTAAD/11-analysis-failed.png) |
| Swagger 예시 영상 생성 | 요청·화면 전환 통과, 분석은 서버 실패 | `2oLfUjAqEcM`의 정확한 URL 입력 → 실제 후쿠오카 영상 제목·썸네일 표시 → 알림 안내 닫기 → 홈에서 동일한 분석 실패 안내. [23-example-url.png](/tmp/linkit-e2e-20260921.aaTAAD/23-example-url.png), [26-example-result.png](/tmp/linkit-e2e-20260921.aaTAAD/26-example-result.png) |
| 수정된 첫 알림 안내 닫기 | 통과 | 임시 게스트 첫 실행의 안내에서 ‘나중에 할게요’ 1회 탭 → 팝업 즉시 제거, 메인 복귀 버튼 사용 가능. [24-notification-before.png](/tmp/linkit-e2e-20260921.aaTAAD/24-notification-before.png), [25-notification-dismissed.png](/tmp/linkit-e2e-20260921.aaTAAD/25-notification-dismissed.png) |
| 분석 상태의 재시작 복원 | 통과 | 최종 APK 재설치·재시작 후 저장된 작업 ID의 분석 실패 상태를 재조회하고 같은 안내를 표시. [27-analysis-restored.png](/tmp/linkit-e2e-20260921.aaTAAD/27-analysis-restored.png) |
| 작은 화면 빈 상태 스크롤·버튼 | 통과 | 3버튼 내비게이션 상태에서 빈 본문을 스크롤 → 생성 버튼 전체 노출 → 버튼 탭으로 영상/보관함/직접 만들기 메뉴 열림. 시트 핸들 위치는 유지. [28-empty-cta-fixed.png](/tmp/linkit-e2e-20260921.aaTAAD/28-empty-cta-fixed.png) |

두 영상(`PEprsGRAorM`, `2oLfUjAqEcM`)의 실패 문구는 `ObserveVideoScheduleCreationUseCase.readState()`의 `VideoAnalysisStatus.FAILED` 분기와 일치한다. 네트워크 조회 오류·조회 시간 초과·유효하지 않은 영상의 문구와 구분된다. 인증 헤더·토큰을 추출하지 않았으며 UI와 해당 분기 코드로 결과를 확인했다. 앱의 실패 처리 흐름은 확인했지만 서버 내부 분석 실패 원인은 확인하지 않았고, 이 사례를 생성 성공으로 간주하지 않는다. 백엔드 분석 로그 확인과 성공 가능한 테스트 영상/일정이 필요하다.

## 발견한 이슈와 수정 상태

| 이슈 | 원인 / 수정 | 현재 검증 상태 |
| --- | --- | --- |
| 분석 화면 알림 안내가 X·“나중에 할게요”로 닫히지 않음 | Navigation3 `1.1.0-alpha01`은 동일 백스택의 entry를 캐시한다. 호스트에서 미리 계산한 Boolean이 최초 값으로 고정되어 상태 변경이 반영되지 않았다. `ScheduleNavDisplay`에서 상태를 직접 읽는 람다를 전달하고 `ScheduleEditEntry` 콘텐츠에서 호출하도록 최소 수정 | 실제 `LinkItNavDisplay` 경유 회귀 테스트 3개 통과: X 닫기, 나중에 하기, 설정 읽기가 지연된 뒤 안내 노출. schedule31개·iOS 컴파일 통과. 임시 게스트 AVD 첫 안내에서도 ‘나중에 할게요’ 닫기 및 메인 복귀 재검증 통과. X 버튼은 회귀 테스트로 검증 |
| 탐색·일정 생성 화면의 Figma 배치 차이 | 국가 칩34dp·국가 카드128×160dp·국가 영상112dp/테마168dp, 섹션 간격·테마 제목 순서, 일정 생성 hero와 입력 여백 보정. 기존 디자인 토큰·컴포넌트 재사용 | 치수·순서 회귀 테스트 3개 통과. 최신 원본 스크린샷 6종 육안 확인 및 탐색 release 실데이터 화면 재확인. [19-explore-fixed.png](/tmp/linkit-e2e-20260921.aaTAAD/19-explore-fixed.png). 국가 API에 사진이 없어 현재 영상 썸네일을 활용하며 목업 도시 사진·미제공 통계는 넣지 않음 |
| 메인 현재 위치 버튼 누락 | 기존 `RequestCurrentLocation`에 Figma 규격40dp 버튼·20dp 기존 Material 아이콘 연결 | 위치 회귀3개·기존 시트 제스처19개 포함 최종 map105개 통과. release에서 권한 거부·재요청·일회 허용·GPS 카메라 이동 확인 |
| 작은 높이에서 빈 일정의 생성 버튼 하단 잘림 | 빈 상태도 실제 보이는 시트 높이로 측정하고, 기존 그림·간격을 유지한 채 빈 상태 본문만 세로 스크롤 가능하게 변경 | 375×640 회귀에서 버튼40dp 전체 접근·클릭·시트 Resting 유지 검증 통과. 최종 release AVD에서도 내부 스크롤·버튼 전체 노출·메뉴 열기 통과 |
| 최초 분석 요청 응답 대기 중 링크 변경으로 작업 추적 유실 가능 | 추천 카드 선택·붙여넣기가 활성화되어 `UpdateVideoLink`가 제출 코루틴을 취소했다. 제출 중에는 ViewModel에서 입력 변경을 무시하고 선택·붙여넣기도 비활성화. 링크 복사는 계속 허용 | 지연된 POST 응답을 사용한 ViewModel 회귀와 UI 회귀 2개 통과. 요청 1회·원래 URL 유지·작업 ID 저장 및 복사 허용 검증. 실서버에서 해당 경합을 재현한 결과와는 구분 |

알림 수정은 `ScheduleNavDisplay.kt`와 `ScheduleEditEntry.kt` 두 파일의 상태 전달만 변경했다. 화면을 강제로 다시 생성하거나 백스택을 변경하는 방식은 사용하지 않았다. `ScheduleNotificationNavigationTest`에 회귀 검증을 추가했다.

디자인 확인에 사용한 Figma 노드:

- 탐색: [17789:52248](https://www.figma.com/design/Pym5oUSWQjyWVtN86oj6lb/Pingo--v3.0.3?node-id=17789-52248), [17789:52268](https://www.figma.com/design/Pym5oUSWQjyWVtN86oj6lb/Pingo--v3.0.3?node-id=17789-52268), [17789:52288](https://www.figma.com/design/Pym5oUSWQjyWVtN86oj6lb/Pingo--v3.0.3?node-id=17789-52288)
- 일정 생성: [17789:47166](https://www.figma.com/design/Pym5oUSWQjyWVtN86oj6lb/Pingo--v3.0.3?node-id=17789-47166)
- 메인: [18112:43338](https://www.figma.com/design/Pym5oUSWQjyWVtN86oj6lb/Pingo--v3.0.3?node-id=18112-43338)

## 자동 검증과 E2E의 구분

- 알림 수정 후 `:feature:schedule:testDebugUnitTest`: 8개 suite, 31개 테스트 통과, 실패·오류·스킵 0. 새 알림 회귀 3개, 추천 ViewModel 3개, 추천 UI 5개를 포함한다.
- 알림 수정 후 schedule iOS Simulator Arm64 컴파일 통과. 이는 iOS 앱 실행 검증이 아니다.
- 제출 중 입력 수정까지 포함한 최종 전체 테스트 236개(domain64/data18/map105/schedule33/explore16), 실패·오류·스킵0. schedule 추천 ViewModel 4개·추천 UI 6개를 포함한다. 최종 Android `assembleRelease`, app-shared/map/schedule/explore iOS Simulator Arm64 컴파일 통과.
- 마지막 제출 중 입력 수정 후에도 release APK를 기존 사용자 데이터 보존 상태로 업데이트 설치하고 홈·실제 지도 정상 시작을 재확인했다. [30-final-submission-guard-home.png](/tmp/linkit-e2e-20260921.aaTAAD/30-final-submission-guard-home.png). 추가 분석 요청은 하지 않았다.
- Robolectric·가짜 저장소 기반 검증은 실제 서버·Google 지도 SDK·외부 YouTube 앱을 사용한 위 Android 실행 결과와 별도로 관리한다.
- Figma 정량 이미지 일치율은 측정하지 않았다.

## 남은 확인 항목

- [x] 최신 작업 트리 전체 테스트·release 빌드 결과와 최종 테스트 수 반영
- [x] 수정 후 release의 탐색·일정 생성 화면 배치 확인
- [x] 현재 위치 버튼 노출, 위치 권한, 에뮬레이터 GPS 기반 카메라 이동 확인
- [x] 기존 데이터를 보존한 상태에서 첫 알림 안내의 닫기 동작 AVD 재검증
- [x] 최종 release에서 작은 높이의 빈 상태 생성 버튼 스크롤 재검증
- [ ] 다른 영상으로 분석 `COMPLETED` → 실제 저장 일정 → 상세·영상 요약까지 성공 경로 확인
- [ ] iOS 런타임 검증
- [ ] Firebase/APNs 설정 후 실제 토큰 등록·푸시 수신 검증

실서버 추천 채널 목록과 테마별 목록이 비어 있어, 채널 상세·테마의 실제 다음 페이지는 단위/화면 테스트로만 검증했다. 의견 전송·계정 초기화·기존 일정 삭제 같은 외부 쓰기/파괴 시나리오는 수행하지 않았다. 실제 완료 일정이 없어 일정 이름 변경·일정 상세의 실제 데이터 탐색도 이번 AVD에서 검증하지 못했다.

현재 서버에 없는 보관함·장소 저장·수동 생성 등의 API를 임의로 추가하거나 성공 처리하지 않았다. 해당 목록은 [API 미제공 기능](MAIN_SCREEN_API_STATUS.md#현재-swagger에-없는-기능--백엔드-추가-필요)에 정리되어 있다. FCM은 등록 API 구현과 실제 플랫폼 토큰 발급·푸시 수신 검증을 구분하며, 이번 E2E에서 후자는 검증하지 않았다.
