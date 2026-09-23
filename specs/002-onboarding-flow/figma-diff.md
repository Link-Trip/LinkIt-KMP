# Figma 대조 결과: 온보딩 플로우 UI (#49)

**Feature**: [spec.md](./spec.md) | [tasks.md](./tasks.md)
**기준 Figma**: [Pingo v3.0.3 - 인트로 / 최종진입](https://www.figma.com/design/Pym5oUSWQjyWVtN86oj6lb/Pingo--v3.0.3?node-id=17789-47165&m=dev) (`17789:47165`)
**대조일**: 2026-09-21
**대조 방법**: Figma 화면 19개의 스크린샷·디자인 컨텍스트(치수·토큰·타이포)와 `[UI]` 태스크 완료 시점의 코드·Roborazzi 골든(`screenshots/`, 2026-09-21 18:29 생성)을 비교했다.
**처리 현황(2026-09-21)**: 우선순위 높음 1건·중간 5건·낮음 항목을 같은 날 반영했다. 각 표의 "비고"에 `[반영]` 표기가 있는 행이 처리된 항목이며, 골든은 `screenshots/` 아래 20:25 이후 파일로 갱신됐다. 디자인 확인이 필요한 항목은 "디자인 확인이 필요한 항목" 절에 남겨 두었다.

## 한눈에 보기

`[UI]` 태스크 범위의 화면 구조·문구·흐름은 Figma와 일치한다. 남은 차이는 세 부류다.

| 부류 | 항목 | 우선순위 | 상태 |
|---|---|---|---|
| 레이아웃 | 튜토리얼 지도 화면의 바텀시트가 Figma는 접힘, 구현은 휴지 높이로 열림 | 높음 | 반영 |
| 컴포넌트 스타일 | 건너뛰기 버튼(지도·영상 링크 화면 간에도 불일치), 코치마크 말풍선 타이포, 링크복사 버튼 크기, 빈 상태 링크 색·chevron, 확인전 카드 그라데이션 색 | 중간 | 반영 |
| 간격·색 미세 차이 | 추천 영상 카드 간격·색, 히어로 상단 여백, 붙여넣기 칩 주변 간격, 빈 상태 텍스트 스타일, 약관 부제 색 | 낮음 | 반영 |

우선순위 "높음"은 화면 구성이 달라 사용자가 바로 알아차리는 항목, "중간"은 컴포넌트 단위로 눈에 띄는 항목, "낮음"은 토큰·간격 수준의 항목이다.

## 화면별 상세

### 1. 인트로 지구 화면 (`17789:47249`)

| 항목 | Figma | 구현 | 비고 |
|---|---|---|---|
| 지구 에셋 | 유리 질감 3D 지구(파랑·초록), 457×812 이미지 중앙 | 도트형 지구 `intro_globe` 375dp | 에셋 교체 여부는 디자인 확정(`~ms` 애니메이션) 시 함께 결정 |
| 상단 텍스트 | 없음 | `인트로 애니메이션` 텍스트(`IntroScreen.kt` `IntroContent`) | #49 이전(HEAD)부터 있던 코드. Figma에 없으므로 제거 후보. 미반영(디자인 확정 시 함께 정리) |

### 2. 온보딩 시작 화면 (`18058:86645`)

차이 없음. 상단 여백 136, 일러스트 240, 배지·제목·설명 간격 24/12/8, 배지(`fill.normal`, 패딩 8/6, radius 8, `caption1Medium`), 제목 `headline1Bold`, 설명 `label1ReadingMedium`, 버튼(주 `#292A2D` radius 12, 보조 Outlined) 모두 Figma 값과 같다.

### 3. 약관 동의 바텀시트 (`18580:37864`)

| 항목 | Figma | 구현 | 비고 |
|---|---|---|---|
| 부제 색 | `#70737C` 원색 | `label.alternative`(#37383C 61%) | `[반영]` `PaletteTokens.CoolNeutral50`으로 교체 |

핸들(8/4 패딩, 48×3, neutral 300 20%), 전체 동의 행(패딩 12, radius 12, `fill.normal`, `body2NormalBold`), 항목 행(높이 44, 좌우 24, `[필수]` `label2Medium` primary, 상세보기 `label2Medium` 밑줄), 비활성 버튼(`interaction.disable`)은 일치한다.

약관 상세는 Figma가 네이티브 텍스트 페이지(`18580:39432`)이고 구현은 WebView다. spec.md Assumptions "약관 본문 출처"에서 결정한 사항이라 차이로 보지 않는다.

### 4. 튜토리얼 지도 화면 1·2단계 (`17789:47492`, `17789:47737`)

| 항목 | Figma | 구현 | 비고 |
|---|---|---|---|
| 바텀시트 상태 | 핸들만 보이게 접힘(시트 상단 y≈716). `일본, 도쿄` 위치 칩과 `일정 생성` 버튼이 하단 한 줄(y≈672)에 나란히 배치 | 저장 일정 시트가 휴지 높이(53%)까지 열려 목록 노출. 말풍선이 목록 카드 위에 겹침 | `[반영]` `MapBottomSheetHost`에서 온보딩 모드면 `Collapsed` 앵커로 고정하고 드래그를 막는다. 튜토리얼 종료 시 `Resting`으로 복귀 |
| 말풍선↔대상 간격 | 8px(1단계 664→672, 2단계 522→530) | 12dp(`CoachMarkDefaults.BubbleGap`) | `[반영]` 8dp |
| 말풍선 가로 위치 | 1단계 x 132~342(버튼 오른쪽 정렬 근처), 2단계 x 89~339(메뉴 왼쪽으로 치우침) | 대상 중앙 정렬 후 화면 안으로 클램프 | 자동 배치 규칙 유지 시 허용 범위로 판단, 디자인 확인 필요 |
| 말풍선 타이포 | NanumSquare Neo Regular 14, 행간 1.45, 검정, 그림자 없음, radius 999, 패딩 20/12 | Wanted Sans `label1NormalMedium`, `label.normal`, 그림자 2dp, `shape.rounded`, 패딩 20/12 | `[반영]` `CoachMarkDefaults.bubbleTextStyle()`(나눔스퀘어 Regular 14, 행간 20.3), `label.strong`, 그림자 제거 |
| 건너뛰기 버튼 | 아래 "건너뛰기 버튼" 표 참조 | | |

1단계에서 프로필 버튼 자리에 건너뛰기, 그 아래 지도 종류 버튼을 두는 구성과 2단계 메뉴(`영상 링크로 만들기` 강조, 나머지 비활성, 닫기 X 밝게 표시)는 일치한다.

### 5. 영상 링크로 만들기 (`17789:47166`, `47177`, `47195`, `47208`, `47221`)

| 항목 | Figma | 구현 | 비고 |
|---|---|---|---|
| 히어로 상단 여백 | 상단 네비 바로 아래 0, 높이 180, radius 20 | 위 12dp(`VideoLinkHero` `padding(vertical = 12)`) | `[반영]` 위 0, 아래 12 |
| `추천영상` 라벨 색 | neutral 700 `#1F2127` | `label.strong` | `[반영]` `PingoNeutral700` |
| 카드 썸네일→제목 간격 | 12 | 6 | `[반영]` 12 |
| 카드 제목→조회수 간격 | 3 | 0 | `[반영]` 3 |
| 카드 제목·조회수 색 | 제목 `#1F2127`, 조회수 neutral 400 `#5D6470` | `label.strong`, `label.alternative` | `[반영]` `PingoNeutral700`, `PingoNeutral400` |
| 링크복사 버튼 | 패딩 8/4, radius 4, 아이콘 16(내부 14), `caption1Medium`(12) 흰색, 배경 `inverse.background` 52% + primary 5% + blur, 우하단 8 | 패딩 5/3, radius 4, 아이콘 14, `caption2Medium`(11), 배경 `material.dimmer`, 우하단 6 | `[반영]` 패딩 8/4, 아이콘 16, `caption1Medium`, `ToastDefaults` 배경 2겹, 우하단 8 |
| 붙여넣기 칩 | 133×30, 패딩 14/7, radius 8, `caption1Regular` `label.neutral`, `fill.normal` | `LinkItButton` Small(패딩 14/7, radius 8) Assistive | 텍스트 스타일은 Small 버튼 기본값. 확인 필요 |
| 목록→칩 간격 | 34(24+10) | 20 | `[반영]` 34 |
| 칩→입력란 간격 | 10 | 8 | `[반영]` 10 |
| 클립보드 토스트 | 폭 343, 하단 20, radius 8, NanumSquare Neo Bold 12/21/-0.3, URL 색 neutral 100 `#D7E1EE`, 그림자 (0,1,1) | 동일(패딩 16/20, `shape.lg`, `caption1Bold` 나눔, `PingoNeutral100`, 그림자 2dp) | 닫기 아이콘만 Figma `Close Square`(24 박스 안 12px X), 구현 `Utility.Close` 24 |
| 상단 네비 | 뒤로가기 없음, 제목 중앙 | 온보딩 모드에서 뒤로가기 숨김 | 일치 |

### 6. 분석 완료 (`17789:48125`)

차이 없음. 일러스트 293×240, 문구 `body1NormalSemibold` 80% 불투명, 패딩 10, 하단 `생성된 일정 확인하기` 버튼 모두 일치한다.

### 7. 메인 빈 상태 (`18425:36106`)

| 항목 | Figma | 구현 | 비고 |
|---|---|---|---|
| `볼만한 영상 찾아보기` 링크 | `primary`(#388AFE) 14 Medium 밑줄 + 오른쪽 chevron 아이콘 9×18 | `label.alternative` 회색 `label2Medium`(13) 밑줄, chevron 없음 | `[반영]` primary `label1ReadingMedium` 밑줄 + `LinkItIcon.Arrow.ChevronRight` 18dp |
| 부제 | `label1ReadingMedium`(14) `label.normal` | `label2Medium`(13) `label.alternative` | `[반영]` |
| 제목 | `headline2Bold`(16) | `body1NormalSemibold`(16) | `[반영]`. 같은 함수를 쓰는 일정 로드 오류 상태에도 적용됨 |
| 제목→부제 간격 | 4 | 6 | `[반영]` 4 |
| 부제→버튼 간격 | 12 | 16 | `[반영]` 12 |
| 버튼 | 117×40, 패딩 20/10, radius 10, `label1NormalBold`, 배경 `#37383C` | `LinkItButton` Medium | 골든에서 배경·크기 유사, 토큰 확인 필요 |
| 시트 휴지 높이 | 542px(812 기준) | 63%(`MapSheetEmptyRestingFraction`) | `[반영]` 75%. 기존 계산이 상태바·위치 칩을 빼지 않아 낮았다(698 컨테이너 기준 523/698). 골든에서 링크까지 보임, 실기기 재확인은 T054에서 |

### 8. 확인전 카드 (`18154:39159`)

| 항목 | Figma | 구현 | 비고 |
|---|---|---|---|
| 강조 배경 | 109.7° 그라데이션 `#62A8FE` 10% → `#BAD9FF` 10% | `primary.normal`(#388AFE) 10% → `primary.light`(PaleBlue80) 10% | `[반영]` `PaleBlue60` 10% → `#BAD9FF` 10% 가로 그라데이션. 끝색은 팔레트에 없어 `MapScreen.kt` 지역 상수(`UncheckedScheduleGradientEnd`)로 두었고 토큰 확정 시 교체 |
| 하단 구분선 | `#E6EDF8`(`PingoNeutral50`) | 기존 카드 구분선 | `[반영]` 확인전 카드만 `PingoNeutral50` |
| 카드 내용 | 배지, 제목, `3박4일`, `82만원`(예상 비용), `AI 한줄 요약` | 기존 카드(배지, 제목, 기간, 장소 수, 지역) | spec.md Assumptions "메인 화면·일정 상세 의존"에 따라 카드 세부는 메인 화면 소유. 예상 비용·한 줄 요약 노출은 별도 판단 |

### 건너뛰기 버튼 (지도·영상 링크 화면 공통, `18058:273713`)

| 항목 | Figma | 지도 구현 (`MapScreen.kt` `TutorialOverlay`) | 영상 링크 구현 (`ScheduleEditScreen.kt` 상단 네비 `actions`) |
|---|---|---|---|
| 크기 | 89×38, radius 10, 패딩 20/9 | 높이 40, radius 8(Small) | 높이 36, radius 8(Small) |
| 배경·테두리 | 흰 배경 + 1px `line.normal.neutral`, 그림자 없음 | `background.elevated.normal` + 그림자, 테두리 없음 | Outlined Assistive |
| 텍스트 | `label1NormalMedium`(14) `label.normal` | Small 버튼 기본 | Small 버튼 기본 |
| 위치 | top 10(상태바 제외), end 20 | top 20, end 16 | end 12 |

`[반영]` `core/ui/.../onboarding/OnboardingSkipButton.kt`를 새로 두고 두 화면이 함께 쓴다. 흰 배경 + 1px `line.normal.neutral`, radius 10, 높이 38, 패딩 20/9, `label1NormalMedium`. 지도는 상태바 아래 10·오른쪽 20, 영상 링크 화면은 상단 네비 `actions` 슬롯(오른쪽 여백 합계 20)에 둔다.

## 확인하지 못한 항목

- Figma 딤 사각형(`Rectangle 34625636`)의 정확한 색·투명도를 조회하지 않아 구현 `material.dimmer`(CoolNeutral10 52%)와 같은지 확인하지 못했다.
- 지도 위 일정 이름 말풍선(`도쿄 신주쿠 여행 [테스트]`)과 확인전 데이터 연동은 `[API]` 태스크(T047) 범위라 비교에서 제외했다.
- 붙여넣기 칩과 빈 상태 버튼의 텍스트 스타일은 디자인시스템 버튼 기본값에 의존하므로 토큰 값까지는 대조하지 않았다.

## 디자인 확인이 필요한 항목

코드 수정 없이 T055와 함께 요청한다.

- 코치마크 말풍선 가로 배치 규칙(대상 중앙 정렬 허용 여부)
- 확인전 카드 그라데이션 끝색 `#BAD9FF`의 토큰 등록 여부
- 확인전 카드의 예상 비용·한 줄 요약 노출 여부
- 빈 상태 시트 높이(542px 기준)의 실기기 확인
- 인트로 지구 에셋 교체와 `인트로 애니메이션` 텍스트 제거

## 반영 내역 (2026-09-21)

| 단계 | 변경 파일 | 갱신 골든 |
|---|---|---|
| 1. 시트 접힘 | `feature/map/.../MapScreen.kt` `MapBottomSheetHost` | `MapScreenshotTest.tutorialStep1CreateButton`, `tutorialStep2VideoLinkOption` |
| 2. 건너뛰기 버튼 | `core/ui/.../onboarding/OnboardingSkipButton.kt`(신규), `MapScreen.kt` `TutorialOverlay`, `ScheduleEditScreen.kt` | 지도 튜토리얼 2종, `ScheduleEditScreenshotTest.tutorialStep3CopyLink`, `tutorialStep4PasteLinkWithClipboardToast` |
| 3. 말풍선 | `core/designsystem/.../coachmark/LinkItCoachMark.kt` | `CoachMarkScreenshotTest` 3종 + 위 4종 |
| 4. 링크복사 버튼 | `ScheduleEditScreen.kt` `RecommendedVideoCard` | `ScheduleEditScreenshotTest` 추천 영상 노출 골든 전부 |
| 5. 빈 상태 | `MapScreen.kt` `ScheduleStateContent`, `MapSheetEmptyRestingFraction` | `MapScreenshotTest.emptyMap`, `emptyMapExpanded` |
| 6. 확인전 카드 | `MapScreen.kt` `ScheduleListRow` | `MapScreenshotTest.uncheckedScheduleCard`, `uncheckedScheduleCardExpanded` |
| 7. 간격·색 | `ScheduleEditScreen.kt`, `TermsConsentSheet.kt` | `ScheduleEditScreenshotTest` 전부, `TermsConsentSheetScreenshotTest` 3종 |

검증: `:core:designsystem`, `:feature:map`, `:feature:schedule`, `:feature:intro`의 `recordRoborazziDebug`를 실행해 전 테스트 통과(2026-09-21 20:25). 커밋은 #49 UI 변경 전체가 아직 미커밋 상태라 함께 묶어 올린다.
