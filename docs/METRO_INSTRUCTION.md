# Metro DI 가이드

이 프로젝트는 [Metro](https://zacsweers.github.io/metro/latest/) DI 프레임워크를 사용합니다.
Metro 버전은 `0.10.4`이며, Kotlin `2.2.20`과 호환됩니다.

## DI 그래프 구조

```
core:common
└── AppGraph : ViewModelGraph                    ← ViewModel 주입 기반 (commonMain)

app-android
├── AndroidAppGraph : AppGraph, MetroAppComponentProviders  ← Android DI 그래프
├── InjectedViewModelFactory : MetroViewModelFactory        ← ViewModel 팩토리 구현체
└── LinkitApplication : MetroApplication                    ← AppComponentFactory 활성화

app-shared
└── IosAppGraph : AppGraph                                  ← iOS DI 그래프 (iosMain, 수동 주입)
```

> `AndroidAppGraph`는 `app-android` 모듈에 위치합니다. Metro 컴파일러가 `@ContributesIntoMap`을 수집하려면 `@DependencyGraph`가 선언된 모듈에서 모든 feature 모듈을 의존해야 하기 때문입니다.

### AppGraph (`core:common/commonMain`)

모든 DI 그래프의 최상위 인터페이스입니다. `ViewModelGraph`를 상속하여 `metroViewModelFactory` 프로퍼티를 제공합니다.

```kotlin
interface AppGraph : ViewModelGraph
```

### AndroidAppGraph (`app-android`)

Android 플랫폼의 DI 그래프입니다. `MetroAppComponentProviders`를 상속하여 Activity 생성자 주입을 지원합니다.

```kotlin
@DependencyGraph(
    scope = AppScope::class,
    additionalScopes = [DataScope::class],
)
interface AndroidAppGraph : AppGraph, MetroAppComponentProviders {
    @DependencyGraph.Factory
    fun interface Factory {
        fun createAndroidAppGraph(
            @Provides applicationContext: Context,
        ): AndroidAppGraph
    }
}
```

### IosAppGraph (`app-shared/iosMain`)

iOS 플랫폼의 DI 그래프입니다. Kotlin 2.2.20에서는 iOS 멀티모듈 `@ContributesBinding` 자동 수집이 지원되지 않으므로 ([Metro 이슈 #460](https://github.com/ZacSweers/metro/issues/460)), `@Binds`와 `@Provides`를 수동으로 정의합니다.

`MetroViewModelFactory`도 수동으로 제공합니다:

```kotlin
@Provides
fun provideMetroViewModelFactory(
    viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>,
    assistedFactoryProviders: Map<KClass<out ViewModel>, Provider<ViewModelAssistedFactory>>,
    manualAssistedFactoryProviders: Map<KClass<out ManualViewModelAssistedFactory>, Provider<ManualViewModelAssistedFactory>>,
): MetroViewModelFactory = object : MetroViewModelFactory() {
    override val viewModelProviders = viewModelProviders
    override val assistedFactoryProviders = assistedFactoryProviders
    override val manualAssistedFactoryProviders = manualAssistedFactoryProviders
}
```

> Kotlin 2.3.20+로 업그레이드하면 `@ContributesBinding` 자동 수집이 가능해져 수동 등록이 불필요해집니다.

### InjectedViewModelFactory (`app-android`)

Android에서 Metro multibinding으로 수집된 ViewModel provider들을 `MetroViewModelFactory`에 주입하는 구현체입니다.

```kotlin
@ContributesBinding(AppScope::class)
@Inject
class InjectedViewModelFactory(
    override val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>,
    override val assistedFactoryProviders: Map<KClass<out ViewModel>, Provider<ViewModelAssistedFactory>>,
    override val manualAssistedFactoryProviders: Map<KClass<out ManualViewModelAssistedFactory>, Provider<ManualViewModelAssistedFactory>>,
) : MetroViewModelFactory()
```

`@ContributesBinding(AppScope::class)`은 `MetroViewModelFactory` 타입으로 바인딩됩니다. Activity에서 `MetroViewModelFactory`를 생성자로 주입받을 때 이 클래스가 제공됩니다.

### LinkitApplication (`app-android`)

`MetroApplication`을 구현하여 `MetroAppComponentFactory`를 활성화합니다. 이를 통해 `@ActivityKey`로 등록된 Activity가 생성자 주입으로 생성됩니다.

```kotlin
class LinkitApplication : Application(), MetroApplication {
    private val _appGraph: AppGraph by lazy {
        createGraphFactory<AndroidAppGraph.Factory>().createAndroidAppGraph(
            applicationContext = this,
        )
    }

    override val appComponentProviders: MetroAppComponentProviders
        get() = _appGraph as MetroAppComponentProviders
}
```

---

## Activity 추가하기

### 1. Activity 클래스 생성

`feature:xxx/src/androidMain/`에 생성합니다.

```kotlin
@ContributesIntoMap(AppScope::class, binding<Activity>())
@ActivityKey(XxxActivity::class)
@Inject
class XxxActivity(
    private val viewModelFactory: MetroViewModelFactory,
) : ComponentActivity() {

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = viewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
                LinkItTheme {
                    // Screen Composable
                }
            }
        }
    }
}
```

| 어노테이션 | 역할 |
|-----------|------|
| `@ContributesIntoMap(AppScope::class, binding<Activity>())` | AppScope 그래프의 Activity multibinding map에 `Activity` 타입으로 등록 |
| `@ActivityKey(XxxActivity::class)` | multibinding map의 key |
| `@Inject` | Metro가 생성자 주입 대상으로 인식 |

> **주의**: `binding<Activity>()`를 반드시 명시해야 합니다. 생략하면 `ComponentActivity` 타입으로 바인딩되어 `MetroAppComponentProviders.activityProviders` map에 수집되지 않고, 런타임에 "Couldn't call constructor" 에러가 발생합니다.

`viewModelFactory`는 `InjectedViewModelFactory` 인스턴스(`MetroViewModelFactory` 타입)가 주입됩니다. Android의 기본 `viewModel()` 경로를 위해 `defaultViewModelProviderFactory`를 override하고, Metro Compose의 `metroViewModel()` / `assistedMetroViewModel()` 경로를 위해 `LocalMetroViewModelFactory`에도 같은 인스턴스를 제공합니다. 후자를 누락하면 런타임에 `No MetroViewModelFactory registered`가 발생합니다.

### 2. AndroidManifest.xml 등록

```xml
<activity
    android:name="com.linkit.company.feature.xxx.XxxActivity"
    android:exported="false"
    tools:ignore="Instantiatable" />
```

`tools:ignore="Instantiatable"`은 기본 생성자가 없는 Activity에 대한 lint 경고를 무시합니다. 런타임에는 `MetroAppComponentFactory`가 생성자 주입으로 Activity를 생성하므로 문제 없습니다.

### 3. app-android/build.gradle.kts

```kotlin
dependencies {
    implementation(project(":feature:xxx"))
}
```

`@DependencyGraph`가 있는 `app-android`는 ViewModel의 `@ContributesIntoMap`을 수집할 수 있도록 **ViewModel이 선언된 모든 feature 모듈을 직접 의존**해야 합니다. 누락하면 빌드는 성공해도 해당 화면 진입 시 `Unknown model class`가 발생합니다.

### 4. feature:xxx/build.gradle.kts

```kotlin
androidMain.dependencies {
    implementation(libs.metrox.android)
    implementation(libs.metrox.viewmodel)
    implementation(libs.androidx.activity.compose)
}
```

---

## ViewModel 추가하기

### SavedStateHandle이 필요 없는 경우

```kotlin
@ContributesIntoMap(AppScope::class)
@ViewModelKey(XxxViewModel::class)
@Inject
class XxxViewModel(
    private val someRepository: SomeRepository,
) : ViewModel() {
    // ...
}
```

Compose에서 사용:

```kotlin
@Composable
fun XxxScreen(
    viewModel: XxxViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

### SavedStateHandle이 필요한 경우

Navigation route 인자를 받거나, 프로세스 복원 시 상태를 유지해야 할 때 `ViewModelAssistedFactory` 패턴을 사용합니다.

```kotlin
@AssistedInject
class XxxViewModel(
    @Assisted val savedStateHandle: SavedStateHandle,
    private val someRepository: SomeRepository,
) : ViewModel() {

    // Navigation route 인자 꺼내기
    val itemId: String = savedStateHandle.get<String>("itemId") ?: ""

    @AssistedFactory
    @ViewModelAssistedFactoryKey(XxxViewModel::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ViewModelAssistedFactory {
        override fun create(extras: CreationExtras): XxxViewModel {
            return create(extras.createSavedStateHandle())
        }
        fun create(@Assisted savedStateHandle: SavedStateHandle): XxxViewModel
    }
}
```

> **주의**: `SavedStateHandle`을 `@Assisted`로 받는 ViewModel은 클래스 어노테이션이 `@Inject`가 아닌 `@AssistedInject`여야 합니다.

Compose에서 사용:

```kotlin
@Composable
fun XxxScreen(
    viewModel: XxxViewModel = assistedMetroViewModel(),
) {
    // ...
}
```

#### Factory 동작 원리

내부 `Factory` 인터페이스는 개발자가 직접 호출하지 않습니다. Metro가 컴파일 타임에 자동으로 처리합니다.

1. `@ContributesIntoMap` → `InjectedViewModelFactory`의 `assistedFactoryProviders` map에 자동 등록
2. `assistedMetroViewModel<XxxViewModel>()` 호출
3. `MetroViewModelFactory`가 map에서 `XxxViewModel::class`에 해당하는 `Factory`를 찾음
4. `Factory.create(extras)` → `extras.createSavedStateHandle()` → `Factory.create(savedStateHandle)` → ViewModel 생성

### ViewModel 모듈 의존성

ViewModel이 있는 feature 모듈에 추가:

```kotlin
commonMain.dependencies {
    implementation(libs.metrox.viewmodel)           // @ViewModelKey, ViewModelAssistedFactory
    implementation(libs.metrox.viewmodel.compose)   // metroViewModel(), assistedMetroViewModel()
}
```

---

## iOS ViewController 추가하기

iOS에는 `MetroAppComponentFactory`가 없으므로, `LocalMetroViewModelFactory` CompositionLocal로 factory를 제공합니다.

```kotlin
// feature:xxx/src/iosMain/
fun XxxViewController(appGraph: AppGraph) = ComposeUIViewController {
    CompositionLocalProvider(
        LocalMetroViewModelFactory provides appGraph.metroViewModelFactory
    ) {
        LinkItTheme {
            // Screen Composable
        }
    }
}
```

`appGraph.metroViewModelFactory`는 `AppGraph`가 `ViewModelGraph`를 상속하므로 사용 가능합니다.

`metroViewModel()` / `assistedMetroViewModel()`은 내부적으로 `LocalMetroViewModelFactory.current`를 사용하므로, **commonMain의 Compose Screen 코드는 Android/iOS 동일**합니다.

### iOS ViewController 모듈 의존성

ViewController에서 `LocalMetroViewModelFactory`를 사용하는 모듈에 추가:

```kotlin
commonMain.dependencies {
    implementation(libs.metrox.viewmodel.compose)   // LocalMetroViewModelFactory
}
```

### iOS 주의사항

현재 Kotlin 2.2.20에서는 iOS 멀티모듈 `@ContributesIntoMap` 자동 수집이 지원되지 않습니다 ([Metro 이슈 #460](https://github.com/ZacSweers/metro/issues/460)). 새 ViewModel을 추가할 경우 `IosAppGraph`에 수동으로 등록이 필요할 수 있습니다.

---

## Navigator 바인딩

Feature 모듈 간 Activity 전환을 위한 Navigator는 Metro DI로 바인딩됩니다.

### Navigator 구현체 등록

각 feature 모듈의 `androidMain`에 Navigator 구현체를 생성하고 `@ContributesBinding`으로 바인딩합니다:

```kotlin
// feature:home/androidMain/navigator/HomeNavigatorImpl.kt
@ContributesBinding(AppScope::class)
@Inject
class HomeNavigatorImpl : HomeNavigator {
    override fun navigateWithLauncher(
        activity: ComponentActivity,
        intentBuilder: (Intent.() -> Intent)?,
        launcher: ActivityResultLauncher<Intent>?,
    ) = startActivity<HomeActivity>(activity, intentBuilder, launcher)
}
```

`startActivity<T>()`는 `Navigator`의 확장 함수로, Intent 생성 및 launcher 분기 로직을 공통 처리합니다.

### Activity에서 Navigator 주입

Activity 생성자에 Navigator를 추가하면 Metro가 자동으로 구현체를 주입합니다:

```kotlin
@ContributesIntoMap(AppScope::class, binding<Activity>())
@ActivityKey(IntroActivity::class)
@Inject
class IntroActivity(
    private val viewModelFactory: MetroViewModelFactory,
    private val homeNavigator: HomeNavigator,  // Metro가 HomeNavigatorImpl을 주입
) : ComponentActivity()
```

> Navigator 구현체는 stateless이므로 별도의 스코프 어노테이션 없이 바인딩됩니다.

---

## 의존성 요약

| 모듈 | sourceSet | 의존성 | 용도 |
|------|-----------|--------|------|
| `core:common` | commonMain | `libs.metrox.viewmodel` | AppGraph : ViewModelGraph |
| `app-shared` | commonMain | `libs.metrox.viewmodel` | IosAppGraph에서 ViewModelGraph 타입 참조 |
| `app-android` | - | `libs.metrox.viewmodel` | AndroidAppGraph, InjectedViewModelFactory |
| `app-android` | - | `libs.metrox.android` | MetroApplication, MetroAppComponentProviders |
| feature 모듈 | commonMain | `libs.metrox.viewmodel` | @ViewModelKey, ViewModelAssistedFactory |
| feature 모듈 | commonMain | `libs.metrox.viewmodel.compose` | metroViewModel(), assistedMetroViewModel() |
| feature 모듈 | androidMain | `libs.metrox.android` | @ActivityKey (Activity가 있는 모듈만) |
| feature 모듈 | androidMain | `libs.metrox.viewmodel` | MetroViewModelFactory (Activity 생성자 주입) |
| `core:navigation` | androidMain | `libs.androidx.activity.compose` | Navigator base interface (ComponentActivity 참조) |

## 참고 문서

- [Metro 공식 문서](https://zacsweers.github.io/metro/latest/)
- [MetroX Android](https://zacsweers.github.io/metro/latest/metrox-android/)
- [MetroX ViewModel](https://zacsweers.github.io/metro/latest/metrox-viewmodel/)
- [MetroX ViewModel Compose](https://zacsweers.github.io/metro/latest/metrox-viewmodel-compose/)
