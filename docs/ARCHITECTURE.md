# LinkIt-KMP Architecture

> 최종 업데이트: 2026-04-06

## 변경 이력

| 날짜 | 내용 |
|------|------|
| 2026-09-21 | 마이페이지 데이터 연동: AppSettings/Member/Feedback/Terms/AppInfo 저장소, IntroNavigator 추가 (이슈 #41/#45) |
| 2026-04-06 | Feature 모듈 간 Navigator 패턴 추가 (이슈 #21) |
| 2026-04-03 | 멀티 액티비티 구조, Navigation3 멀티 백스택, Metro DI ViewModel 연동 반영 |

---

## 프로젝트 구조

이 프로젝트는 **Clean Architecture** + **멀티모듈** + **KMP (Kotlin Multiplatform)** 기반으로 설계되었습니다.

```
LinkIt-KMP/
├── app-android/                  # Android 애플리케이션 진입점
├── app-shared/                   # KMP 공유 모듈 (DI 그래프)
├── app-ios/                      # iOS 애플리케이션 (Xcode)
├── core/
│   ├── common/                   # 공통 유틸리티, MVI 아키텍처, AppGraph
│   ├── ui/                       # 공통 UI 유틸리티
│   ├── designsystem/             # Material3 테마, 컬러, 타이포그래피
│   └── navigation/               # Navigation3 멀티 백스택, Route 정의
├── domain/                       # 도메인 레이어 (Repository 인터페이스, UseCase, 모델)
├── data/                         # 데이터 레이어 (Repository 구현, DataSource, DTO)
├── feature/
│   ├── intro/                    # 온보딩 (IntroActivity / IntroViewController)
│   ├── home/                     # 홈 - 바텀네비 호스트 (HomeActivity / HomeViewController)
│   ├── map/                      # 지도 탭
│   ├── storage/                  # 보관함 탭
│   ├── explore/                  # 탐색 탭
│   └── schedule/                 # 일정 (ScheduleActivity)
└── build-logic/                  # Convention Plugins
```

---

## 모듈 의존성

### 의존성 그래프

```
app-android
├── app-shared
├── core:common
├── feature:intro
├── feature:home
└── feature:schedule

app-shared
├── core:common
├── domain
└── data

feature:home (바텀네비 호스트)
├── core:common, core:ui, core:designsystem, core:navigation
├── domain
├── feature:map        ← 탭
├── feature:storage    ← 탭
└── feature:explore    ← 탭

feature:map, feature:storage, feature:explore, feature:schedule, feature:intro
├── core:common, core:ui, core:designsystem
└── domain
```

### 의존성 규칙

**허용:**
- `app-android` → `app-shared`, `feature:*`, `core:*`
- `app-shared` → `core:common`, `domain`, `data`
- `feature:home` → `feature:map`, `feature:storage`, `feature:explore` (탭 호스팅 관계)
- `feature:*` → `core:*`, `domain`
- `data` → `domain`, `core:*`

**금지:**
- feature 간 의존 (home의 탭 호스팅 제외)
- domain → data, feature
- data → feature
- 순환 의존

---

## 멀티 액티비티 구조

각 feature 모듈이 자체 Activity(Android) / ViewController(iOS)를 소유합니다.

| feature 모듈 | Android | iOS |
|-------------|---------|-----|
| `feature:intro` | `IntroActivity` | `IntroViewController` |
| `feature:home` | `HomeActivity` | `HomeViewController` |
| `feature:schedule` | `ScheduleActivity` | - |

- Activity는 각 feature 모듈의 `androidMain`에 위치
- ViewController는 각 feature 모듈의 `iosMain`에 위치
- Activity 간 이동은 Navigator 패턴을 통해 수행 (아래 섹션 참고)
- Activity는 Metro DI를 통해 생성자 주입 (`@ActivityKey` + `@Inject`)

### Navigator 패턴 (Android)

Feature 모듈 간 순환참조 없이 Activity 전환을 수행하는 패턴입니다.

```
core:navigation/androidMain/navigator/
├── Navigator              ← base interface (navigate, navigateWithLauncher)
└── feature/
    ├── HomeNavigator      ← interface : Navigator (마커)
    ├── ScheduleNavigator  ← interface : Navigator (마커)
    └── IntroNavigator     ← interface : Navigator (마커, 앱 초기화 후 온보딩 복귀)

feature:home/androidMain/navigator/
└── HomeNavigatorImpl      ← HomeNavigator 구현 (HomeActivity 직접 참조)

feature:schedule/androidMain/navigator/
└── ScheduleNavigatorImpl  ← ScheduleNavigator 구현 (ScheduleActivity 직접 참조)

feature:intro/androidMain/navigator/
└── IntroNavigatorImpl     ← IntroNavigator 구현 (IntroActivity 직접 참조, NEW_TASK|CLEAR_TASK + 완료 토스트 extra)
```

- **인터페이스**는 `core:navigation`에 정의 → 모든 feature 모듈에서 의존 가능
- **구현체**는 각 feature 모듈에 배치 → 자신의 Activity 클래스를 직접 참조 (문자열 기반 Intent 불필요)
- **DI 바인딩**: `@ContributesBinding(AppScope::class)`으로 Metro가 자동 수집
- **사용**: Activity 생성자에 Navigator를 주입받아 사용

```kotlin
// IntroActivity에서 HomeNavigator를 주입받아 HomeActivity로 이동
@Inject
class IntroActivity(
    private val viewModelFactory: MetroViewModelFactory,
    private val homeNavigator: HomeNavigator,
) : ComponentActivity()
```

> iOS에서는 Navigator 패턴 대신 ViewController 함수의 콜백 람다 파라미터로 화면 전환을 처리합니다.

---

## Navigation3 멀티 백스택

`core:navigation` 모듈에서 탭별 독립 백스택을 관리합니다.

### 핵심 컴포넌트

| 클래스 | 역할 |
|--------|------|
| `LinkItNavKey` | Route 정의 (Map, Storage, Explore, ScheduleEdit) |
| `NavigationState` | 탭별 독립 백스택 + 탭 전환 스택 관리 |
| `LinkItNavigator` | 탭 전환 / 탭 내 push / back 처리 |
| `LinkItNavDisplay` | NavDisplay 래퍼 (전환 애니메이션 포함) |
| `LinkItSavedStateConfiguration` | Route polymorphic serializer 등록 |

### Route 정의

```kotlin
interface LinkItNavKey : NavKey {
    data object Map : LinkItNavKey          // 탭
    data object Storage : LinkItNavKey      // 탭
    data object Explore : LinkItNavKey      // 탭
    data object ScheduleEdit : LinkItNavKey // 서브 라우트
}
```

### 네비게이션 흐름

- **탭 전환**: `navigator.navigate(LinkItNavKey.Storage)` → 탭 스택 전환, 각 탭 상태 보존
- **같은 탭 재선택**: 해당 탭의 백스택을 루트로 초기화
- **서브 라우트 push**: `navigator.navigate(LinkItNavKey.ScheduleEdit)` → 현재 탭 백스택에 push
- **Back**: 탭 내 화면이면 pop, 탭 루트면 이전 탭으로 전환

### Entry Provider 패턴

각 feature 모듈이 `navigation/` 패키지에 자체 entry를 정의합니다:

```kotlin
// feature:map/navigation/MapEntry.kt
fun EntryProviderScope<NavKey>.mapEntry(...) {
    entry<LinkItNavKey.Map> { MapScreen(...) }
}
```

호스트 모듈(`feature:home`)에서 조립:

```kotlin
val entryProvider = entryProvider {
    mapEntry(...)
    storageEntry()
    exploreEntry()
}
```

---

## DI (Metro)

Metro DI 프레임워크를 사용합니다. 자세한 내용은 [METRO_INSTRUCTION.md](METRO_INSTRUCTION.md)를 참고하세요.

### 그래프 구조

```
core:common/AppGraph : ViewModelGraph                    ← commonMain
app-shared/AndroidAppGraph : AppGraph, MetroAppComponentProviders  ← androidMain
app-shared/IosAppGraph : AppGraph                        ← iosMain (수동 주입)
app-shared/InjectedViewModelFactory : MetroViewModelFactory        ← androidMain
app-android/LinkitApplication : MetroApplication
```

### 스코프

| 스코프 | 용도 |
|--------|------|
| `AppScope` | 앱 전역 싱글톤 (Application 생명주기) |
| `DataScope` | 데이터 레이어 (Repository, DataSource, Ktor) |

---

## MVI 아키텍처

`core:common`에 정의된 MVI 패턴을 사용합니다.

| 컴포넌트 | 역할 |
|---------|------|
| `Intent` | 사용자 의도 (sealed interface) |
| `UiState` | UI 상태 (data class) |
| `SideEffect` | 일회성 이벤트 (sealed interface) |
| `MviContainer` | 상태 관리 컨테이너 (`reduce`, `postSideEffect`, `intent`) |
| `MviContext` | Intent 핸들러 컨텍스트 |
| `PopupEffectManager` | Toast/Popup 처리 위임 |

---

## Convention Plugins

`build-logic/`에 정의된 Gradle Convention Plugins:

| 플러그인 | 대상 | 특징 |
|---------|------|------|
| `kmp.shared.convention` | `app-shared` | iOS Framework 생성, Metro 플러그인, Serialization |
| `android.application.convention` | `app-android` | Android Application 설정 |
| `kmp.library.convention` | core, domain, data | KMP 라이브러리, Metro 플러그인 |
| `kmp.feature.convention` | feature 모듈 | `kmp.library.convention` + Compose |
| `kmp.core.convention` | core 모듈 | `kmp.library.convention` 기반 |

모든 KMP 모듈은 `iosArm64`, `iosSimulatorArm64` 타겟을 포함합니다.

---

## 플랫폼별 차이

| 항목 | Android | iOS |
|------|---------|-----|
| 진입점 | Activity (feature 모듈) | ViewController (feature 모듈) |
| DI 그래프 | AndroidAppGraph (자동 수집) | IosAppGraph (수동 @Provides) |
| Activity 주입 | MetroAppComponentFactory | 해당 없음 |
| ViewModel Factory | defaultViewModelProviderFactory override | LocalMetroViewModelFactory CompositionLocal |
| 빌드 | Gradle → APK | Gradle → Framework → Xcode |

---

## 참고 문서

- [Metro DI 가이드](METRO_INSTRUCTION.md)
- [Navigation 구조](navigation-structure.md)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Navigation3](https://developer.android.com/jetpack/androidx/releases/navigation3)
- [Metro](https://zacsweers.github.io/metro/latest/)
