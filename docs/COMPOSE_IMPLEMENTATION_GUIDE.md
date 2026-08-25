# Compose 구현 가이드

Figma 디자인을 Compose UI 코드로 구현할 때 따라야 할 규칙과 참고사항.

## 테마 사용

모든 UI 코드는 `LinkItTheme`으로 감싸야 한다:

```kotlin
LinkItTheme {
    // UI 코드
}
```

## 토큰 우선순위

1. **시맨틱 토큰 우선**: `ButtonEnabled`, `CardBackground` 등 용도가 명확한 토큰을 먼저 사용
2. **스케일 토큰**: 시맨틱 토큰이 없으면 `G1`~`G9`, `Slate50`~`Slate700` 등 사용
3. **토큰 추가**: 매핑되는 토큰이 없으면 `Color.kt`, `Type.kt`, `Shape.kt`에 추가
4. **하드코딩 금지**: `Color(0xFF...)` 직접 사용 금지

토큰 상세 매핑은 `docs/DESIGN_TOKEN_MAPPING.md` 참고.

## 기존 컴포넌트 활용

새 UI를 만들기 전에 `core/designsystem/component/`의 기존 컴포넌트 확인:

| 컴포넌트 | 용도 |
|---------|------|
| `LinkItButton` | 풀 너비 pill 버튼 |
| `LinkItTag` / `LinkItTagRow` | 태그 (5가지 색상) |
| `LinkItTextField` | 텍스트 입력 |
| `LinkItTopAppBar` | 상단 앱바 |
| `LinkItDivider` | 구분선 |
| `LinkItFilterChip` | 필터 칩 |
| `LinkItTab` | 탭 전환 |
| `LinkItDaySelector` | 일차 선택 |
| `LinkItBottomNavigationBar` | 하단 네비게이션 |
| `LinkItTransportInfo` | 교통 정보 |
| `LinkItScheduleCard` | 일정 카드 |
| `LinkItScheduleListItem` | 일정 목록 아이템 |
| `LinkItFolderCard` | 폴더 카드 |
| `LinkItVideoCard` | 영상 카드 |
| `LinkItDialog` | 제목·설명·단일 확인 액션의 모달 다이얼로그 |

## KMP 제약사항

- **commonMain**: 공유 Compose UI 코드 배치
- **androidMain**: `@Preview` 함수, Android 전용 코드
- **androidUnitTest**: Roborazzi 스크린샷 테스트
- Platform-specific API (`Context`, `Activity` 등) 사용 금지 (commonMain)

## Figma → Compose 변환 패턴

| Figma | Compose |
|-------|---------|
| Auto Layout (가로) | `Row` |
| Auto Layout (세로) | `Column` |
| Fill Container | `Modifier.fillMaxWidth()` |
| Fixed Size | `Modifier.size(dp)` / `width(dp)` / `height(dp)` |
| Spacing | `Arrangement.spacedBy(dp)` |
| Padding | `Modifier.padding(dp)` |
| Corner Radius | `RoundedCornerShape(dp)` 또는 `LinkItShape.*` |
| Drop Shadow | `Modifier.shadow(elevation)` |
| Clip Content | `Modifier.clip(shape)` |

## 스크린샷 테스트 작성법

### 1. 모듈에 플러그인 적용

```kotlin
// build.gradle.kts
plugins {
    id("kmp.core.convention")      // 또는 kmp.feature.convention
    id("kmp.screenshot.test.convention")
}
```

### 2. 테스트 작성

`src/androidUnitTest/kotlin/.../screenshot/` 디렉토리에 작성:

```kotlin
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-xxhdpi")
class MyScreenScreenshotTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun myScreen_default() {
        composeRule.setContent {
            LinkItTheme { MyScreen() }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

### 3. 실행

```bash
# 골든 이미지 생성 (최초 또는 의도적 변경 시)
./gradlew :모듈:recordRoborazziDebug

# 골든 이미지와 비교 검증
./gradlew :모듈:verifyRoborazziDebug

# 변경 사항 diff 이미지 생성
./gradlew :모듈:compareRoborazziDebug
```
