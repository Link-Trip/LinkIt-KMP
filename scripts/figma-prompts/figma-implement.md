# Figma 디자인 구현 (Phase 2)

이 프롬프트는 `figma-orchestrate.sh`에서 `claude -p`로 실행됩니다.
스크립트가 프롬프트 앞에 `## 모드: INITIAL` 또는 `## 모드: FIX`를 삽입합니다.

## 입력

- `.figma-workspace/analysis.md` — 분석 결과 (필수)
- `.figma-workspace/verification.md` — 검증 결과 (FIX 모드에서만)

## 진행 순서 (INITIAL 모드)

### 1. 분석 결과 읽기

`.figma-workspace/analysis.md`를 읽고 화면 정보, 상태 목록, 토큰 매핑, 컴포넌트 매핑, 아이콘 목록을 파악한다.

### 2. 아이콘 처리

- **`FIGMA_TOKEN` 사전 확인**: 아이콘이 1개라도 다운로드 필요하면 즉시 `source ~/.zshrc && echo $FIGMA_TOKEN`으로 토큰 존재 여부를 확인한다. 없으면 `~/.zshrc`에서 로드를 재시도한다. 그래도 없으면 Material 아이콘 대체로 진행한다 (사용자에게 묻지 않는다).
- **아이콘 처리 우선순위** (Figma 원본 다운로드를 기본으로 한다):
  1. **기존 다운로드 확인**: `core/designsystem/src/commonMain/composeResources/drawable/`에 동일 이름(`ic_{아이콘명}.xml`)의 파일이 이미 있으면 그대로 재활용한다.
  2. **Figma에서 SVG 다운로드** (기본 방식): 기존 파일이 없으면 아래 절차로 Figma에서 직접 가져온다.
  3. **Material 아이콘 대체** (최후 수단): `FIGMA_TOKEN`이 없는 경우에만, `get_screenshot`으로 Figma 아이콘 스크린샷을 찍고 Material Icons와 **시각적으로 비교**하여 형태가 동일한 경우에 한해 대체한다. 이름만 같고 형태가 다르면 대체하지 않는다.
- **Figma SVG 다운로드 절차**:
  1. 아이콘 노드의 nodeId를 확인한다 (분석 결과의 아이콘 목록에서)
  2. Figma API로 SVG를 다운로드한다:
     ```
     curl -s -H "X-Figma-Token: $FIGMA_TOKEN" \
       "https://api.figma.com/v1/images/{fileKey}?ids={nodeId}&format=svg" \
       | python3 -c "import sys,json; print(list(json.load(sys.stdin)['images'].values())[0])"
     ```
     반환된 URL에서 SVG 파일을 다운로드한다.
  3. SVG를 Android Vector Drawable XML로 변환한다:
     ```
     python3 scripts/svg2vector.py input.svg output.xml
     ```
     스크립트가 없으면 수동으로 변환하거나, 간단한 아이콘은 path 데이터를 직접 추출한다.
  4. 변환된 XML을 `core/designsystem/src/commonMain/composeResources/drawable/`에 `ic_{아이콘명}.xml`로 저장한다.
  5. `LinkItIcons`에 Compose Resource로 등록한다:
     ```kotlin
     val CustomIcon: DrawableResource get() = Res.drawable.ic_custom_icon
     ```
- 여러 아이콘의 nodeId를 쉼표로 연결하여 한 번의 API 호출로 일괄 다운로드한다:
  ```
  curl -s -H "X-Figma-Token: $FIGMA_TOKEN" \
    "https://api.figma.com/v1/images/{fileKey}?ids={nodeId1},{nodeId2},{nodeId3}&format=svg"
  ```
- 이미지(사진, 썸네일 등)는 아이콘이 아니므로 플레이스홀더로 대체한다.
- 아이콘 파일명은 `ic_` 접두사 + snake_case로 작성한다 (예: `ic_train.xml`, `ic_location_pin.xml`).

### 3. MVI 아키텍처 생성

CLAUDE.md의 참고 문서 지침에 따라 `docs/ARCHITECTURE.md`, `docs/METRO_INSTRUCTION.md`를 읽고 기존 패턴(`feature/map/main/MapViewModel.kt` 참조)을 따른다.

**3a. XxxUiState.kt 생성**

Figma 상태 간 시각적 차이를 분석하여 UiState 필드를 도출한다. 정확한 Figma node가 있는 상태만 추가한다:
- 로딩 스피너가 있는 Figma frame이 확인된 경우에만 → `isLoading: Boolean`
- 빈 화면 일러스트가 있는 Figma frame이 확인된 경우에만 → 리스트/콘텐츠 필드가 비어있을 때 표시
- 에러 메시지/재시도 frame이 확인된 경우에만 → `errorMessage: String?`
- 데이터가 있는 "Default" 상태에서 콘텐츠 필드의 타입을 결정

```kotlin
data class XxxUiState(
    val items: List<...> = emptyList(),
    // ... 상태 차이에서 도출된 필드
) : UiState
```

**3b. XxxIntent.kt 생성**

SITEMAP.md의 사용자 액션 목록과 Figma 인터랙션 요소에서 Intent를 도출한다:
```kotlin
sealed interface XxxIntent : Intent {
    data object Initialize : XxxIntent
    // SITEMAP.md 액션 + Figma 버튼/인터랙션에서 도출
}
```

**3c. XxxSideEffect.kt 생성**

화면 전환, 토스트 등 일회성 이벤트를 정의한다:
```kotlin
sealed interface XxxSideEffect : SideEffect {
    // Navigation, Toast 등
}
```

**3d. XxxViewModel.kt 생성**

기본적으로 `feature/map/main/MapViewModel.kt`의 단순 주입 패턴을 따른다. Route 인자나 상태 복원이 실제로 필요한 경우에만 `@AssistedInject`와 `SavedStateHandle`을 사용한다:
```kotlin
@ContributesIntoMap(AppScope::class)
@ViewModelKey(XxxViewModel::class)
@Inject
class XxxViewModel : ViewModel() {
    private val container = MviContainer<XxxIntent, XxxSideEffect, XxxUiState>(
        initialState = XxxUiState(),
        onIntent = { handleIntent(it) },
    )

    val uiState = container.uiState

    fun onIntent(intent: XxxIntent) = container.intent(intent)

    private fun MviContext<XxxUiState, XxxSideEffect>.handleIntent(intent: XxxIntent) {
        when (intent) {
            XxxIntent.Initialize -> Unit
            // ...
        }
    }

    override fun onCleared() {
        container.close()
        super.onCleared()
    }
}
```

- Metro DI 어노테이션 필수: `@Inject`, `@ViewModelKey`, `@ContributesIntoMap(AppScope::class)`
- 실제 데이터 로직(Repository/UseCase 호출)은 `// TODO` 주석으로 남긴다
- **Metro DI 설정 확인** (빠지면 런타임 크래시 발생):
  1. **feature 모듈 `build.gradle.kts`**: `commonMain`에 `libs.metrox.viewmodel`, `libs.metrox.viewmodel.compose` 의존성이 있는지 확인하고, 없으면 추가한다. `androidMain`에 `libs.metrox.android`, `libs.metrox.viewmodel` 의존성도 확인한다.
  2. **호스트 Activity (예: HomeActivity)**: `setContent` 블록에서 `CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory)`로 factory를 제공하는지 확인한다. 이것이 없으면 Navigation3 서브 라우트의 `assistedMetroViewModel()` 호출 시 `No MetroViewModelFactory registered` 에러가 발생한다. 누락되어 있으면 추가한다.
  3. **호스트 모듈 `build.gradle.kts`**: `androidMain`에 `libs.metrox.viewmodel` 의존성이 있는지 확인한다 (`LocalMetroViewModelFactory` import에 필요).
  4. **`app-android/build.gradle.kts`**: ViewModel이 있는 feature 모듈을 `implementation(project(":feature:xxx"))`로 직접 의존하는지 확인한다. Metro `@ContributesIntoMap`으로 등록된 ViewModel Factory는 `@DependencyGraph`가 있는 `app-android` 모듈에서 직접 의존해야 자동 수집된다. 누락 시 `Unknown model class` 런타임 에러 발생.

### 4. Compose UI 코드 생성

Screen Composable은 **Stateless**로 작성하며, `UiState`와 콜백 람다를 파라미터로 수신한다.
또한, 스크린샷 테스트에서 전체 스크롤 콘텐츠를 캡처하기 위해 **Screen과 Content를 분리**한다:

- `XxxContent`: 스크롤/fillMaxSize 없이 순수 콘텐츠만 렌더링하는 `internal` Composable. 스크린샷 테스트에서 사용.
- `XxxScreen`: `XxxContent`를 스크롤 래퍼 + 오버레이 헤더로 감싸는 실제 화면. Navigation Entry에서 사용.

```kotlin
/** 스크롤 없이 전체 콘텐츠를 렌더링. 스크린샷 테스트에서 사용. */
@Composable
internal fun XxxContent(
    uiState: XxxUiState,
    onAction: (XxxIntent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(White)) {
        // 헤더, 탭, 탭 콘텐츠 등 전체 UI
    }
}

@Composable
fun XxxScreen(
    uiState: XxxUiState,
    onAction: (XxxIntent) -> Unit,
    onBack: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            XxxContent(uiState, onAction)
        }
        // 오버레이 헤더 (그라데이션, 앱바 등)
    }
}
```

- `LinkItTheme` 내에서 구현한다
- `commonMain`에 배치하여 KMP 호환을 유지한다
- `@Preview` 함수는 `androidMain`에 **상태별로** 생성한다:
  ```kotlin
  @Preview @Composable private fun XxxScreenDefaultPreview() { ... }
  @Preview @Composable private fun XxxScreenLoadingPreview() { ... }
  @Preview @Composable private fun XxxScreenEmptyPreview() { ... }
  ```

### 5. Navigation 연결

`docs/NAVIGATION_STRUCTURE.md`의 체크리스트를 따른다:

1. **Route 추가**: `core/navigation/.../LinkItRoute.kt`의 `LinkItNavKey`에 `data object` 추가
   ```kotlin
   @Serializable
   data object Xxx : LinkItNavKey
   ```
2. **Serializer 등록**: `LinkItSavedStateConfiguration`의 `polymorphic(NavKey::class)` 블록에 추가
   ```kotlin
   subclass(LinkItNavKey.Xxx::class, LinkItNavKey.Xxx.serializer())
   ```
3. **Entry 파일 생성**: `feature/{module}/navigation/XxxEntry.kt`
   ```kotlin
   fun EntryProviderScope<NavKey>.xxxEntry(
       onBack: () -> Unit,
   ) {
       entry<LinkItNavKey.Xxx> {
           val viewModel = metroViewModel<XxxViewModel>()
           val uiState by viewModel.uiState.collectAsStateWithLifecycle()
           XxxScreen(
               uiState = uiState,
               onAction = viewModel::intent,
               onBack = onBack,
           )
       }
   }
   ```
4. **호스트 entryProvider에 등록**: `feature/home/.../HomeNavDisplay.kt`의 `entryProvider` 블록에 추가
   ```kotlin
   xxxEntry(onBack = navigator::navigateBack)
   ```
   - SITEMAP.md 계층 구조를 기반으로 **탭(top-level) vs 서브 라우트** 결정
   - 서브 라우트인 경우 해당 탭의 backstack에 push, 독립 화면이면 별도 Activity 고려

### 6. 스크린샷 테스트 작성

Roborazzi로 **상태별 개별 테스트 함수**를 작성한다.
- **`XxxContent`를 사용**하여 스크롤 없이 전체 콘텐츠를 캡처한다.
- **뷰포트 높이를 충분히 크게 설정**하여 콘텐츠가 잘리지 않도록 한다 (예: `h5000dp`).

```kotlin
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w375dp-h5000dp-xxhdpi", application = Application::class)
class XxxScreenScreenshotTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun xxxScreen_default() {
        composeRule.setContent {
            LinkItTheme {
                XxxContent(
                    uiState = XxxUiState(items = sampleItems),
                    onAction = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // 정확한 Figma frame이 있는 상태만 별도 테스트로 추가한다.

    // ... 상태 수만큼 테스트 함수 생성
}
```

- 테스트 화면 크기는 Figma 프레임에서 system chrome을 제외한 콘텐츠 영역으로 설정한다
  - 예: Figma 375x812dp, status bar 38dp, home indicator 34dp → `w375dp-h740dp-xxhdpi`
- `./gradlew :모듈:recordRoborazziDebug`로 **모든 상태의 골든 이미지를 한 번에** 생성한다
- 골든 이미지는 `screenshots/` 폴더에 저장된다
- feature 모듈에서 Metro DI 관련 `MetroAppComponentFactory` 오류가 발생하면:
  - `src/androidUnitTest/AndroidManifest.xml`을 생성하여 오버라이드한다:
    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools">
        <application
            android:appComponentFactory="androidx.core.app.CoreComponentFactory"
            tools:replace="android:appComponentFactory" />
    </manifest>
    ```
  - `@Config`에 `application = Application::class`를 추가한다
- 해당 모듈에 `kmp.screenshot.test.convention` 플러그인이 적용되어 있는지 확인하고, 없으면 추가한다

### 7. 구현 현황 작성

`.figma-workspace/implementation.md`에 아래 형식으로 작성한다:

```markdown
# 구현 현황

## 생성 파일
- feature/{module}/src/commonMain/.../XxxUiState.kt
- feature/{module}/src/commonMain/.../XxxIntent.kt
- feature/{module}/src/commonMain/.../XxxSideEffect.kt
- feature/{module}/src/commonMain/.../XxxViewModel.kt
- feature/{module}/src/commonMain/.../XxxScreen.kt
- feature/{module}/navigation/XxxEntry.kt
- feature/{module}/src/androidUnitTest/.../XxxScreenScreenshotTest.kt

## 모듈 정보
- module: feature:{module}
- package: com.linkit.company.feature.{module}

## 테스트 정보
- testClass: XxxScreenScreenshotTest
- tests:
  - xxxScreen_default (State: Default, fileKey: ..., nodeId: ...)
  - xxxScreen_loading (State: Loading, fileKey: ..., nodeId: ...)
  - xxxScreen_empty (State: Empty, fileKey: ..., nodeId: ...)

## Navigation 현황
- Route: 등록 완료
- Serializer: 등록 완료
- Entry: 생성 완료
- 호스트: 등록 완료
```

## 진행 순서 (FIX 모드)

### 1. 검증 결과 읽기

`.figma-workspace/verification.md`를 읽고 미달성 상태의 수정 지침을 파악한다.

### 2. 수정 우선순위

diff 이미지에서 차이가 큰 영역부터 먼저 수정한다:
1. 레이아웃/크기 차이 (높이, 너비, 위치)
2. 색상/배경 차이
3. 간격/여백 차이 (padding, margin, gap)
4. 미세 차이 (1~2px, 모서리 둥글기)

### 3. Figma 원본 값 검증

간격/여백 또는 글자 크기/스타일 차이 발견 시, 감으로 수정하지 말고 반드시 Figma 원본 값을 확인한다:
1. `.figma-workspace/analysis.md`의 designContext 원본 데이터를 다시 참조한다.
   해당 영역의 `data-node-id`로 노드를 찾고, CSS 클래스에서 정확한 값을 읽는다.
   - 간격: `p-[17px]` → 17dp, `gap-[8px]` → 8dp, `px-[24px]` → horizontal 24dp
   - 글자 크기: `text-[16px]` → 16sp, `text-[12px]` → 12sp
   - 행간: `leading-[24px]` → lineHeight 24sp, `leading-[1.35]` → lineHeight = fontSize x 1.35
   - 자간: `tracking-[0.5px]` → letterSpacing 0.5sp
   - 굵기: `font-['...:Bold']` → FontWeight.Bold, `'...:ExtraBold'` → FontWeight.ExtraBold
2. 확인된 Figma 값을 `docs/DESIGN_TOKEN_MAPPING.md`의 Typography 토큰과 대조한다.
   일치하는 `LinkItTextStyle.*`이 있으면 해당 토큰을 사용하고, 없으면 새 토큰 추가를 검토한다.
3. 값이 불명확하거나 더 정밀한 정보가 필요하면, 해당 노드의 nodeId로 `get_design_context`를 다시 호출하여 세부 레이아웃을 확인한다.
4. 확인된 Figma 값과 현재 Compose 코드의 값을 대조하여, 차이가 있는 부분만 정확히 수정한다.

### 4. 회귀 방지

- 코드 수정 후 `recordRoborazziDebug`로 **전체** 골든 이미지를 재생성한다.
- 이미 통과한 상태가 퇴화하지 않았는지 확인한다.
- 회귀 발견 시 수정을 롤백하거나 조건부 렌더링을 조정한다.

### 5. 구현 현황 업데이트

`.figma-workspace/implementation.md`를 수정 내용 반영하여 업데이트한다.

## 규칙

- **사용자에게 절대 질문하지 않는다.** 모든 판단을 자율적으로 내리고 진행한다. 토큰 추가, 컴포넌트 수정, 아이콘 대체, 구조 결정, 상태 이름 부여 등 어떤 상황에서도 확인을 구하지 않고 최선의 판단으로 진행한다.
- 프로젝트의 기존 디자인 토큰과 컴포넌트를 최대한 재사용한다.
- 새 색상/스타일을 하드코딩하지 않고, 필요시 직접 판단하여 토큰 파일에 추가한다.
- `commonMain`에 작성하여 KMP 호환을 유지한다.
- 한국어로 커밋 메시지와 코드 주석을 작성한다.
- 해당 모듈에 `kmp.screenshot.test.convention` 플러그인이 적용되어 있는지 확인하고, 없으면 추가한다.
- 반복되는 UI 패턴은 재사용 가능한 Composable로 추출한다. 화면 내에서 2회 이상 등장하거나 다른 화면에서도 쓸 수 있는 구조라면 `core/designsystem/component/`에 공통 컴포넌트로 분리한다.
- **아이콘 처리 우선순위** (Figma 원본 우선):
  1. `composeResources/drawable/`에 이미 다운로드된 아이콘(`ic_{이름}.xml`)이 있으면 재활용
  2. Figma API로 SVG 다운로드 → Vector Drawable XML 변환 → `composeResources/drawable/`에 추가 → `LinkItIcons`에 등록
  3. `FIGMA_TOKEN`이 없는 경우, Figma 아이콘 스크린샷과 Material Icons를 **시각적으로 비교**하여 형태가 동일한 경우 Material 아이콘으로 대체 (사용자에게 묻지 않고 직접 판단)
- 이미지(사진, 썸네일 등)는 플레이스홀더로 대체하고 반영하지 않는다.
- 디자인에서 인터랙션을 유추하여 구현한다. 스크롤, 스와이프, 풀다운 리프레시, 바텀시트 등 Figma 레이아웃과 컴포넌트 구조에서 암시되는 동작을 파악하고 적용한다.
- **MVI 아키텍처**: `feature/map/main/`의 기존 패턴을 따른다. Figma node가 없는 로딩·빈 화면·오류 상태를 임의로 추가하지 않는다. Metro assisted injection은 Route 인자나 상태 복원이 필요한 경우에만 사용한다.
- **Navigation 연동**: `docs/NAVIGATION_STRUCTURE.md`의 "새 Route 추가 체크리스트"를 따른다.

## 참고 문서

필요 시 다음 문서를 참조한다:
- `docs/SITEMAP.md` — 화면 구조 및 사용자 액션
- `docs/DESIGN_TOKEN_MAPPING.md` — 디자인 토큰 매핑
- `docs/COMPOSE_IMPLEMENTATION_GUIDE.md` — Compose 구현 가이드
- `docs/NAVIGATION_STRUCTURE.md` — Navigation 구조
- `docs/ARCHITECTURE.md` — 프로젝트 아키텍처
- `docs/METRO_INSTRUCTION.md` — Metro DI 설정
