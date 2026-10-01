# iOS 실행·E2E 검증 기록

## 최신화 후 재확인 — 2026-09-30

- `origin/develop`의 `67279d1`까지 현재 `feature/#42-main_map_screen`에 병합했다. 병합 커밋은 `1f58ba4`이며, `HEAD..origin/develop` 미반영 커밋은 0개다. 원격 push는 하지 않았다.
- 기존 iOS 미커밋 작업은 stash `76f0f529d85d6e23ac21d541840034852f24ed93`에 보존한 후 복원했다. 복원본은 최신 코드에 맞춰 수정했고 원본 stash는 안전 복사본으로 남겼다.
- 새 온보딩과 기존 실제 API 흐름을 모두 유지했다. iOS `IntroViewModel` 등록·공유 그래프 전달, `TermsViewModel`의 `core:ui` 이동, 일정 공통 호스트의 온보딩 완료·종료 콜백, 튜토리얼 중 알림 안내 미노출을 반영했다. 현재 iOS ViewModel 등록 수는 10개다.
- 지도 분석 완료 안내에서 상세를 여는 경로도 `ScheduleOpened`를 전달하여 최신 확인전 상태를 해제한다. 기존 API 폴링·위치 요청·빈 본문 스크롤과 새 온보딩 코치마크를 함께 보존했다.
- SCC 기능 82개와 기존 추적 상태를 유지했다. 스펙 재생성·sync·audit에서 오류·경고 0개이며, 완료/검증 상태를 새로 올리지 않았다.

### 이번 실행 결과

| 검증 | 결과 |
| --- | --- |
| 도메인·데이터 회귀 | domain 96개 + data 39개 통과, 실패·스킵 0 |
| 약관 공통 UI·온보딩 회귀 | core:ui 3개 + intro 28개 통과, 실패·스킵 0 |
| 지도 회귀 | 126개 중 29개가 디스크 부족으로 초기화 실패. 29개 모두 Robolectric 폰트 추출/임시 디렉터리 생성의 `No space left on device`; 전체 통과 아님 |
| 일정 회귀 | 최초 59개 중 28개 실패. 27개는 같은 디스크 부족, 1개는 반복 조회수 문구에 대한 테스트 선택자 범위 문제. 선택자를 첫 영상 카드로 좁혀 수정한 뒤 추천 화면 테스트 6개를 별도 실행하여 모두 통과. 일정 전체 재실행은 아직 미완료 |
| 일정 내비게이션 회귀 | 기존 알림·종료 5개와 신규 온보딩 제출 → 완료 → 종료 1개 통과(위 일정 테스트 수에 포함) |
| 탐색·iOS 컴파일/네이티브·신규 앱 E2E | 이번 전체 실행에서 완료하지 못함. 이전 실행 결과로 대체하지 않음 |

전체 회귀 명령은 지도·일정 테스트 실패로 종료됐다. 디스크 여유는 실행 전 약 4GB였으나 실행 중 소진되었고, 종료 후 약 600MB였다. 이번 검증에서 생성한 유휴 Gradle/Kotlin 프로세스만 종료했으며 사용자 파일·다른 프로젝트 캐시·시뮬레이터 데이터는 삭제하지 않았다. 로그 위치는 `/tmp/linkit-refresh-20260930.oX6dqY/`다.

추천 화면 단독 재검증은 `:feature:schedule:testDebugUnitTest --tests '*ScheduleRecommendationsTest' --max-workers=1`에 Gradle 1536MB/Kotlin 1024MB 메모리 제한을 주어 통과했다. 광범위한 실패를 감추거나 테스트를 제외한 것이 아니라, 코드상으로 확인된 선택자 문제의 수정 검증만 따로 수행한 것이다.

시뮬레이터 UI는 잠금 해제되어 접근 가능하지만, 설치된 앱은 9/21 검증본이다. 최신 앱을 빌드·설치한 것으로 간주하지 않는다. Xcode 26.3/SDK 26.2와 설치 런타임 26.0.1 불일치도 여전하다. 충분한 디스크 공간 확보 후 남은 회귀·iOS 컴파일·앱 실행을 재개해야 하며, 정상 asset catalog 패키징에는 호환 런타임이 필요하다.

아래는 **9/21~22 당시 결과**이며 최신 병합본의 통과 수가 아니다.

---

> 2026-09-21~22(KST) 작업 기록. Kotlin 모듈 컴파일, iOS 앱 전체 빌드, 시뮬레이터 실행, 실서버 성공 경로를 구분한다. 아직 실행하지 않은 항목을 통과로 간주하지 않는다.

## 실행 환경

- Xcode 26.3, iOS Simulator SDK 26.2, 설치된 iOS 런타임 26.0.1.
- iPhone 17 Pro 시뮬레이터 `F9B1C62C-C0F6-4209-B1FE-F7C8C4BC95E6`를 기존 데이터 초기화 없이 부팅했다.
- 임시 빌드·로그 경로: `/tmp/linkit-ios-e2e-20260921.KfuJoy/`.
- iOS 지도는 기존 MapKit을 사용한다. `rememberMapDebugData()`는 iOS에서 `null`이므로 Android debug용 일정 fixture를 쓰지 않는다.
- 플랫폼 FCM 발급·APNs 설정은 없으며 이번 작업에서 가짜 토큰이나 SDK 설정을 만들지 않았다.

## 수정한 연결 누락

| 항목 | 수정 |
| --- | --- |
| Swift 진입점 누락 | `app-shared/iosMain/MainViewController`에서 기존 Intro/Home/Schedule ViewController를 조립. Swift의 중복 온보딩 분기 제거 |
| ViewModel DI 누락 | iOS 전용 feature 의존성 및 9개 ViewModel map 등록. 앱 단일 그래프로 DataStore 중복 생성 방지 |
| 일정 화면 전환 누락 | 홈에서 일정 생성·실제 상세 ID 전달. 공통 Navigation3 일정 호스트 공유, iOS 알림 권한·닫기·홈 복귀 연결 |
| 위치 요청 미구현 | CoreLocation WhenInUse 단발 조회, 거부·실패·취소·늦은 콜백 처리 및 사용 목적 문구 추가 |
| 무관한 분석 영상 표시 | 메타데이터가 없으면 실제와 무관한 샘플 제목·영상 사진 대신 중립 제목·기존 일러스트 사용 |
| Xcode framework 검색·링크 누락 | 기존 Gradle `embedAndSignAppleFrameworkForXcode` 경로를 검색하고 `ComposeApp` 링크 |

`ponytail` 원칙에 따라 feature framework 전체 export나 별도 DI·라우팅 계층을 추가하지 않고 기존 화면·factory·플랫폼 내비게이션을 재사용했다.

## 검증 현황

| 검증 | 결과 |
| --- | --- |
| app-shared iOS Simulator Arm64 컴파일 | 통과. 홈·일정·지도·탐색 등 의존 feature와 iOS DI 포함 |
| Android 공통 일정 테스트 | 36개 통과, 실패·오류·스킵 0. 새 호스트 종료 2개·중립 fallback 1개 포함 |
| 전체 공통·Android 회귀 | 239개 통과(domain64/data18/map105/schedule36/explore16), 실패·오류·스킵 0 |
| iOS 도메인 회귀 | 64개 통과, 실패·오류·스킵 0. 아래 위치·DI 6개를 합하면 iOS 네이티브 70개 |
| iOS 위치 회귀 5개 실행 | 통과, 실패·오류·스킵 0. 성공 1회 전달, 거부·제한, 빈/부정확한 좌표, 실패 후 늦은 콜백, 취소 후 콜백 검증 |
| iOS DI 등록 회귀 1개 실행 | 통과, 실패·오류·스킵 0. 9개 화면 ViewModel 등록 검증. 화면 생성 자체의 검증은 E2E와 구분 |
| 기본 설정의 iOS 앱 전체 빌드 | 환경 차단. SDK 26.2와 설치 런타임 26.0.1 불일치로 destination/actool 실패 |
| 검증용 앱 빌드·설치·실행 | 통과. 명령행에서 런처 asset catalog만 제외한 Debug 앱. 앱 코드·Compose 화면 리소스는 동일 |
| 실서버 API·디자인 E2E | 일부 확인. 홈·MapKit 지도·탐색 실데이터·국가 필터·빈 결과 확인. 나머지는 아래에 구분 |

## 환경 진단

scheme과 지정 기기 또는 generic Simulator destination을 사용하는 `xcodebuild`는 “iOS 26.2 is not installed”로 목적지 선택에 실패했다. 직접 target 빌드는 Kotlin/Native framework 생성 후 `actool`의 `No simulator runtime version from ["23A8464"] available to use with iphonesimulator SDK version 23C57` 오류로 중단됐다.

검증을 진행하기 위해 아래 명령의 **일회성 CLI 옵션으로 런처 `.xcassets`만 제외**했다. 저장소의 asset catalog나 빌드 설정을 우회용으로 변경하지 않았다. 완성 앱에는 Compose 화면·일러스트·폰트 리소스가 포함되며, 런처 아이콘은 없다. 이 결과를 기본 설정의 빌드 통과나 배포용 빌드 검증으로 간주하지 않는다. 정상 전체 패키징 재검증에는 호환되는 iOS 26.2 런타임 설치가 필요하다.

디스크 부족(`build.db: database or disk is full`)도 발생했다. 이번 작업의 재생성 가능한 Android intermediates(약 412MB)와 링크 완료 후 중복 복사된 iOS framework(약 367MB)만 정리했다. Kotlin framework 원본, 완성 앱/APK, 검증 증거, 사용자 파일·기존 시뮬레이터 데이터·다른 프로젝트 캐시는 보존했다.

```sh
./gradlew :app-shared:compileKotlinIosSimulatorArm64 :feature:map:compileTestKotlinIosSimulatorArm64 :feature:schedule:testDebugUnitTest
./gradlew :app-shared:iosSimulatorArm64Test :feature:map:iosSimulatorArm64Test
./gradlew :domain:allTests :data:testDebugUnitTest :feature:map:testDebugUnitTest :feature:schedule:testDebugUnitTest :feature:explore:testDebugUnitTest
xcodebuild -project app-ios/app-ios.xcodeproj -target app-ios \
  -configuration Debug -sdk iphonesimulator CODE_SIGNING_ALLOWED=NO \
  'EXCLUDED_SOURCE_FILE_NAMES=*.xcassets' ASSETCATALOG_COMPILER_APPICON_NAME= \
  ASSETCATALOG_COMPILER_GENERATE_ASSET_SYMBOLS=NO \
  SYMROOT=/tmp/linkit-ios-e2e-20260921.KfuJoy/Build \
  OBJROOT=/tmp/linkit-ios-e2e-20260921.KfuJoy/Intermediates build
```

## 실행 검증 범위

- [x] 앱 실행 → 홈, 실제 MapKit 지도·저장 일정 0개 빈 상태
- [x] 탐색 서버 목록, 일본 필터, 유럽 빈 결과
- [ ] 위치 권한 거부·허용과 시뮬레이션 좌표로 카메라 이동
- [ ] 테마 필터·원본 URL 열기·복귀
- [ ] 일정 생성 추천·복사·입력 검증·뒤로가기
- [ ] 분석 요청 → 알림 안내 닫기 → 홈 진행/결과 및 재실행 복원
- [ ] Figma 대비 safe area·하단 탭·시트·입력·키보드 배치

탐색의 실제 응답에서 일본 29개/중국 1개 일정, 전체 추천 ‘여자혼자 필리핀 여행하기’, 일본 필터 추천 ‘알다가도 모르겠는 일본’ 및 썸네일을 확인했다. 유럽 선택 시 서버 오류 대신 ‘조건에 맞는 여행 영상이 아직 없어요.’를 표시했다. 홈·탐색의 상단 safe area와 하단 탭에 눈에 띄는 잘림은 없었다. 이는 육안 확인이며 Figma 픽셀 일치율은 측정하지 않았다. 이후 Mac 잠금으로 UI 조작이 중단되어 나머지 시나리오는 미검증 상태다.

Android에서 확인한 서버 분석 `FAILED`와 API 미제공 목록은 [API 현황](MAIN_SCREEN_API_STATUS.md) 및 [Android E2E 기록](API_E2E_VALIDATION.md)에 있다. 해당 결과를 iOS 실서버 실행 결과로 대체하지 않는다. 계정 초기화·기존 일정 삭제·의견 전송은 테스트 범위에서 제외한다.
