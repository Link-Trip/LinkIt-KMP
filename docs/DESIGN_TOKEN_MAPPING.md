# 디자인 토큰 매핑

Figma 디자인 변수와 프로젝트 Compose 토큰 간 매핑 테이블.
Figma 디자인을 Compose로 구현할 때 이 문서를 참고하여 기존 토큰을 우선 사용한다.

## Color 토큰

> 파일: `core/designsystem/src/commonMain/kotlin/.../theme/Color.kt`

### Grayscale

| Figma 변수 | Compose 토큰 | Hex |
|-----------|-------------|-----|
| White | `White` | #FFFFFF |
| Gray/1 | `G1` | #FAFAFB |
| Gray/2 | `G2` | #F5F5F6 |
| Gray/3 | `G3` | #E6E7EB |
| Gray/4 | `G4` | #C0C5CC |
| Gray/6 | `G6` | #707276 |
| Gray/7 | `G7` | #404449 |
| Gray/8 | `G8` | #878E99 |
| Gray/9 | `G9` | #27292C |
| Black | `Black` | #222222 |
| Heading Black | `HeadingBlack` | #0F172A |
| Header Text | `HeaderText` | #3E3E3E |
| Title Black | `TitleBlack` | #171719 |
| Meta Gray | `MetaGray` | #676767 |
| Chip Background | `ChipBackground` | #F3F3F3 |
| Line/Solid/Normal | `LineSolidNormal` | #E1E2E4 |

### Primary

| Figma 변수 | Compose 토큰 | Hex |
|-----------|-------------|-----|
| Primary/Blue | `PrimaryBlue` | #137FEC |
| Primary/Blue Light | `PrimaryBlueLight` | #E8F3FE |
| Primary/Blue Border | `PrimaryBlueBorder` | #8EC1F9 |
| Primary/Blue Background | `PrimaryBlueBackground` | #EFF7FF |

### Tag

| Figma 변수 | Compose 토큰 | Hex |
|-----------|-------------|-----|
| Tag/Green/Bg | `TagGreenBg` | #ECFDF5 |
| Tag/Green/Text | `TagGreenText` | #059669 |
| Tag/Red/Bg | `TagRedBg` | #FFF1F2 |
| Tag/Red/Text | `TagRedText` | #E11D48 |

### Slate

| Figma 변수 | Compose 토큰 | Hex |
|-----------|-------------|-----|
| Slate/50 | `Slate50` | #F1F5F9 |
| Slate/400 | `Slate400` | #94A3B8 |
| Slate/500 | `Slate500` | #64748B |
| Slate/600 | `Slate600` | #475569 |
| Slate/700 | `Slate700` | #48536D |

### Semantic

| Figma 변수 | Compose 토큰 | Hex |
|-----------|-------------|-----|
| Button/Enabled | `ButtonEnabled` | #2B2B2F |
| Button/Disabled/Bg | `ButtonDisabledBg` | #F5F5F6 |
| Button/Disabled/Text | `ButtonDisabledText` | #C0C5CC |
| Card/Background | `CardBackground` | #F6F7F8 |
| Card/Border | `CardBorder` | #F1F5F9 |
| Border/Default | `BorderDefault` | #EBEBEB |

## Typography 토큰

> 파일: `core/designsystem/src/commonMain/kotlin/.../theme/Type.kt`

### LinkItTextStyle (커스텀 스타일)

| Figma 텍스트 스타일 | Compose 토큰 | Size / Weight / LineHeight |
|-------------------|-------------|---------------------------|
| Caption 1 | `LinkItTextStyle.caption1` | 12sp / Medium / 21sp |
| Small | `LinkItTextStyle.small` | 11sp / Normal / 21sp |
| Base 2 | `LinkItTextStyle.base2` | 15sp / Medium / 24sp |
| XS | `LinkItTextStyle.xs` | 10sp / Medium / 14sp |
| Heading | `LinkItTextStyle.heading` | 16sp / Bold / 24sp |
| Body | `LinkItTextStyle.body` | 12sp / Normal / 19.5sp |
| Section Title | `LinkItTextStyle.sectionTitle` | 14sp / Bold / 20sp |
| Tag Text | `LinkItTextStyle.tagText` | 10sp / Bold / 13sp |
| Chip Label | `LinkItTextStyle.chipLabel` | 13sp / Bold / 18.2sp |
| Keyword Tag | `LinkItTextStyle.keywordTag` | 10sp / Normal / 14sp |
| Meta Info | `LinkItTextStyle.metaInfo` | 12sp / Bold / 16.2sp |

### Material3 Typography 매핑

| Material3 슬롯 | Compose 토큰 | Size |
|---------------|-------------|------|
| headlineLarge | `LinkItTypography.headlineLarge` | 24sp Bold |
| headlineMedium | `LinkItTypography.headlineMedium` | 18sp Bold |
| headlineSmall | = `LinkItTextStyle.heading` | 16sp Bold |
| titleLarge | `LinkItTypography.titleLarge` | 16sp Bold |
| titleMedium | = `LinkItTextStyle.sectionTitle` | 14sp Bold |
| titleSmall | `LinkItTypography.titleSmall` | 12sp Bold |
| bodyLarge | = `LinkItTextStyle.base2` | 15sp Medium |
| bodyMedium | `LinkItTypography.bodyMedium` | 14sp Normal |
| bodySmall | = `LinkItTextStyle.body` | 12sp Normal |
| labelLarge | = `LinkItTextStyle.caption1` | 12sp Medium |
| labelMedium | = `LinkItTextStyle.xs` | 10sp Medium |
| labelSmall | = `LinkItTextStyle.small` | 11sp Normal |

## Shape 토큰

> 파일: `core/designsystem/src/commonMain/kotlin/.../theme/Shape.kt`

| Figma 속성 | Compose 토큰 | 값 |
|-----------|-------------|-----|
| Corner Radius 4 | `LinkItShape.tag` | 4.dp |
| Corner Radius 8 | `LinkItShape.input` | 8.dp |
| Corner Radius 12 | `LinkItShape.card` | 12.dp |
| Corner Radius 16 | `LinkItShape.scheduleCard` | 16.dp |
| Corner Radius 999 (Pill) | `LinkItShape.pill` | 999.dp |

## 컴포넌트 카탈로그

> 파일: `core/designsystem/src/commonMain/kotlin/.../component/`

| 컴포넌트 | 용도 |
|---------|------|
| `LinkItButton` | 풀 너비 pill 버튼 (enabled/disabled) |
| `LinkItTag` | 태그 (Blue, Green, Red, Slate, Keyword 5가지 색상) |
| `LinkItTagRow` | 태그 목록 가로 배치 |
| `LinkItTextField` | 텍스트 입력 (label, placeholder, maxLength, error) |
| `LinkItTopAppBar` | 상단 앱바 (뒤로가기 버튼 옵션) |
| `LinkItDivider` | 구분선 |
| `LinkItFilterChip` | 선택 가능한 필터 칩 |
| `LinkItTab` | 탭 전환 |
| `LinkItDaySelector` | 일차 선택 |
| `LinkItBottomNavigationBar` | 하단 네비게이션 바 |
| `LinkItTransportInfo` | 교통 정보 |
| `LinkItScheduleCard` | 일정 카드 (이미지, 태그, 제목, 주소, 설명) |
| `LinkItScheduleListItem` | 일정 목록 아이템 |
| `LinkItFolderCard` | 폴더 카드 |
| `LinkItVideoCard` | 영상 카드 |
| `LinkItDialog` | 제목·설명·단일 확인 액션의 모달 다이얼로그 |
