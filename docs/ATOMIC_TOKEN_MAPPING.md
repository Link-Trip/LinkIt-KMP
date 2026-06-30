# Atomic 토큰 매핑

Figma **Atomic(프리미티브) 티어** 변수(W3C DTCG export: `$type`/`$value`/`{alias}`)와
프로젝트 Compose `foundation` 토큰 간 매핑 테이블.

## 기준 (Source of Truth)

- **값 충돌 시 Atomic(Figma) 값이 정답이다.** 코드 값이 다른 항목은 ⚠️ 로 표시했으며, 추후 코드 동기화 대상이다.
- 표기: ✅ 값 일치 · ⚠️ 값 근사(불일치, 동기화 필요) · ❌ Atomic에 대응 토큰 없음

## Color 토큰

> 파일: `core/designsystem/.../foundation/color/token/ColorTokens.kt`
> Atomic: `Atomic - colors / Mode 1`

### Primary

| Compose 토큰 | 코드 값 | Atomic 변수 | Atomic 값 | 상태 |
|-------------|---------|-------------|-----------|------|
| `primary500` | #666FFF | (정확 매칭 없음) `Lavendar/50`~`/60` 사이 | #5667ff ~ #7885ff | ❌ |
| `primary100` | #F6F6FF | `Lavendar/99` | #f6f7ff | ⚠️ |

> `primary`는 Atomic의 `Lavendar` 색상군 계열로 보이나 정확히 일치하지 않는다. 정확한 매핑은 잘린 시맨틱 티어(Primary 정의)를 받아야 확정 가능.

### Grayscale (코드 `gray` ↔ Atomic `Cool Neutral`)

| Compose 토큰 | 코드 값 | Atomic 변수 | Atomic 값 | 상태 |
|-------------|---------|-------------|-----------|------|
| `gray900` | #27292C | `Cool Neutral/20` | #292a2d | ⚠️ |
| `gray800` | #2B2D34 | `Cool Neutral/22` | #2e2f33 | ⚠️ |
| `gray700` | #404449 | `Cool Neutral/30` | #46474c | ⚠️ |
| `gray600` | #707276 | `Cool Neutral/50` | #70737c | ⚠️ |
| `gray500` | #878E99 | `Cool Neutral/60` | #878a93 | ⚠️ |
| `gray400` | #C0C5CC | `Cool Neutral/90` | #c2c4c8 | ⚠️ |
| `gray300` | #E6E7EB | `Cool Neutral/97` | #eaebec | ⚠️ |
| `gray200` | #F5F5F6 | `Cool Neutral/98` | #f4f4f5 | ⚠️ |
| `gray100` | #FAFAFB | `Cool Neutral/99` | #f7f7f8 | ⚠️ |

> 코드 `gray` 램프 전체가 Atomic `Cool Neutral`과 값이 미세하게 다르다. Atomic 기준으로 동기화 시 위 Atomic 값으로 교체.
> 참고: Atomic에는 별도 `Neutral`(순수 회색) 램프도 존재하나, 코드 `gray`의 푸른 기 도는 톤은 `Cool Neutral`에 더 가깝다.

### 기타

| Compose 토큰 | 코드 값 | Atomic 대응 | 상태 |
|-------------|---------|-------------|------|
| `dimmed` | #4D121212 (≈ black 30%) | `Common/_0`(#000000) + `Opacity` 합성 | ❌ (합성 필요) |
| `surfaceBlack` | #F727292C (≈ CoolNeutral20 97%) | `Cool Neutral/20` + `Opacity/97` | ❌ (합성 필요) |
| `gradient2` | linearGradient 4색 | 없음 | ❌ |
| `gradient3` | linearGradient 4색 | 없음 | ❌ |

## Typography 토큰

> 파일: `core/designsystem/.../foundation/typography/token/TypeScaleTokens.kt`
> Atomic: `Atomic - font / Mode 1`

### Font Family

| 코드 | Atomic `Font family` | 상태 |
|------|----------------------|------|
| NanumSquare Neo (`nanum_400/500/700`) | `NanumSquare Neo variable` | ✅ |

> Atomic에는 `Paperlogy`, `Wanted Sans Variable`도 정의돼 있으나 현재 코드 미사용.

### Font Size (`Atomic - font / Font size`)

| Compose 스케일 | 코드 값 | Atomic 변수 | Atomic 값 | 상태 |
|---------------|---------|-------------|-----------|------|
| `XS` | 10sp | `Font size/30` | 10 | ✅ |
| `S` | 11sp | `Font size/50` | 11 | ✅ |
| `Caption1` | 12sp | `Font size/100` | 12 | ✅ |
| `Caption2` | 13sp | `Font size/200` | 13 | ✅ |
| `Caption3` | 14sp | `Font size/300` | 14 | ✅ |
| `Base2` | 15sp | `Font size/400` | 15 | ✅ |
| `Base1` | 16sp | `Font size/500` | 16 | ✅ |
| `Subtitle2` | 18sp | `Font size/600` | 18 | ✅ |
| `Subtitle1` | 20sp | `Font size/700` | 20 | ✅ |
| `Title` | 24sp | `Font size/900` | 24 | ✅ |
| `Headline6` | 28sp | `Font size/1000` | 28 | ✅ |
| `Headline5` | 32sp | (없음) | — | ❌ |
| `Headline4` | 34sp | (없음) | — | ❌ |
| `Headline3` | 48sp | `Font size/1400` | 48 | ✅ |

> Atomic Font size는 28 → 36 → 40 → 44 → 48 → 60으로 점프하여 32/34sp가 없다. Headline4/5는 Atomic에 대응 토큰이 없음.

### Line Height (`Atomic - font / Line heigh`)

| Compose 스케일 | 코드 값 | Atomic 변수 | Atomic 값 | 상태 |
|---------------|---------|-------------|-----------|------|
| `XS` | 14sp | `Line heigh/100` | 14 | ✅ |
| `S` | 21sp | (없음, 20/22 사이) | — | ❌ |
| `Caption1/2/3` | 22sp | `Line heigh/500` | 22 | ✅ |
| `Base1/2` | 24sp | `Line heigh/600` | 24 | ✅ |
| `Subtitle2` | 28sp | `Line heigh/800` | 28 | ✅ |
| `Title`, `Subtitle1` | 34sp | (없음, 32/38 사이) | — | ❌ |
| `Headline5/6` | 38sp | `Line heigh/1100` | 38 | ✅ |
| `Headline4` | 46sp | (없음, 48) | — | ❌ |
| `Headline3` | 54sp | (없음, 52) | — | ❌ |

### Letter Spacing (`Atomic - font / Letter spacing`)

| 코드 값 | 사용 스케일 | Atomic 변수 | Atomic 값 | 상태 |
|---------|------------|-------------|-----------|------|
| -0.1sp | `S`, `XS` | `tight` | -0.1 | ✅ |
| -0.3sp | `Title`, `Base2`, `Caption1/2/3` | (없음, tighter -0.25 / tightest -0.5 사이) | — | ❌ |

### Font Weight (`Atomic - font / Font weight`)

| 코드 | Atomic 변수 | 값 | 상태 |
|------|-------------|-----|------|
| `Bold` (W700) | `Bold` | 700 | ✅ |
| `Medium` (W500) | `Medium` | 500 | ✅ |
| `Normal` (W400) | `Regular` | 400 | ✅ |

## Radius / Shape 토큰

> 파일: `core/designsystem/.../foundation/radius/token/RadiusTokens.kt`
> Atomic 전용 radius 램프는 없음. 수치는 `Atomic - appearance / Unit` 및 시맨틱 `Modal/Radius`(12)와 대응.

| Compose 토큰 | 값 | Atomic `Unit` | 상태 |
|-------------|-----|---------------|------|
| `Round4` | 4dp | `Unit/4` | ✅ |
| `Round8` | 8dp | `Unit/8` | ✅ |
| `Round12` | 12dp | `Unit/12` (= `Modal/Radius`) | ✅ |
| `Round16` | 16dp | `Unit/16` | ✅ |
| `Round20` | 20dp | `Unit/20` | ✅ |
| `Round24` | 24dp | `Unit/24` | ✅ |
| `Round50` | 50dp | (없음) | ❌ |
| `Round99` | 99dp | (없음) | ❌ |
| `TopRound12/16`, `BottomRound12` | — | (방향 변형, Atomic 없음) | ❌ |

## Spacing 토큰 (코드 미구현)

Atomic `appearance / Unit` 스케일: `0, 2, 4, 8, 12, 16, 20, 24, 32, 40, 44, 48, 52, 64`
현재 `foundation`에 별도 spacing 토큰이 없다. 필요 시 신규 토큰으로 추가 검토.

## 미반영 / 주의 사항

- Atomic 색상군 다수(`Neutral`, `Red`, `Red Orange`, `Orange`, `Green`, `Lime`, `Cyan`, `Light Blue`, `Blue`, `Pale Blue`, `Violet`, `Purple`, `Pink`)는 현재 코드에 대응 토큰이 없다.
- `Opacity` 스케일(5~100)도 코드 미사용.
- 제공된 JSON이 `Modal` 섹션에서 잘려, **시맨틱 티어(Component/Modal 등)** 매핑은 일부 누락. 전체 export를 받으면 `primary`/`gray` 등의 정확한 시맨틱 매핑을 확정해 보강 가능.
