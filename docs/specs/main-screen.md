# 메인화면 스펙

> 기준 Figma: [Pingo v3.0.3](https://www.figma.com/design/Pym5oUSWQjyWVtN86oj6lb/Pingo--v3.0.3?node-id=17789-48296&m=dev)  
> 기준 영역: `메인화면 / 지도`, `지도 / 일정마커`, `일정 마커 선택`, `장소 마커 선택`, `일정 생성`, `2-1 일정마커 표현방식`, `3-1 마커 겹침`  
> 스펙 버전: `v0.2.1`  
> 작성일: 2026-06-08
> 최종 수정일: 2026-07-05

## 1. 한눈에 보기

메인화면은 `지도탭`의 루트 화면이다. 저장한 여행 일정이 지도 위의 `일정마커`로 표시되고, 사용자는 바텀시트에서 저장 일정을 훑거나 지도 위 마커를 직접 선택해 일정/장소 맥락으로 들어간다.

이 화면에서 가장 중요한 UX는 “지도와 바텀시트가 같은 저장 일정 데이터를 다른 방식으로 보여준다”는 점이다. 지도는 위치와 분포를 보여주고, 바텀시트는 목록과 필터를 제공한다. 일정마커를 선택하면 지도 중심이 해당 일정으로 이동하고, 장소마커를 선택하면 장소 카드가 뜬다.

| 큰 덩어리 | 역할 | 사용자가 기대하는 것 |
|---|---|---|
| 지도 | 저장한 일정과 장소의 위치 탐색 | 확대/축소/이동해도 선택 맥락이 함부로 사라지지 않는다. |
| 일정마커 | 여러 장소를 포함한 일정의 대표 위치 | 누르면 그 일정이 지도 중앙에 온다. |
| 장소마커 | 일정 안의 단일 장소 위치 | 누르면 장소 카드와 상세 이동 액션이 뜬다. |
| 바텀시트 | 저장 일정 목록과 필터 | 기본 진입 시 화면 절반 높이에 있고, 일정 목록을 탐색할 수 있다. |
| 일정 생성 버튼 | 새 일정 생성 진입 | 영상 링크, 보관함, 직접 만들기 중 하나를 선택할 수 있다. |

## 2. 화면 상태별 읽기

### 2.1 기본 진입

![메인 기본 상태](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-default.png)

메인화면 최초 진입 상태다. Figma description 기준으로 바텀시트는 디바이스 높이의 50% 지점에 위치한다. 지도에는 저장한 일정 기반의 일정마커가 표시된다.

| 보이는 것 | 의미 |
|---|---|
| 지도 | 저장 일정이 위치한 지역을 보여주는 기본 탐색 영역 |
| 일정마커 | 저장한 일정의 대표 위치 |
| 바텀시트 | 저장한 일정 목록과 필터 |
| 우측 상단 플로팅 버튼 | 프로필/검색/지도뷰/현재위치 계열 지도 오버레이 액션 |
| 일정 생성 버튼 | 새 일정 생성 방식 선택 진입 |

### 2.2 일정마커 선택

![일정마커 선택 상태](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-schedule-marker-selected.png)

일정마커를 선택한 상태다. 선택 시 지도 배율은 유지하고, 선택한 일정마커가 화면 중앙에 위치하도록 지도 중심만 이동한다. Figma description에서 확정된 범위는 “지도 이동만으로 선택 포커스가 해제되지 않는다”까지다. 뒤로가기, 지도 빈 영역 선택, 다른 마커 선택 시의 최종 정책은 별도 메모에 질문형으로 남아 있어 정책 확인이 필요하다.

| 사용자가 한 일 | 화면 반응 |
|---|---|
| 일정마커 탭 | 해당 일정마커가 화면 중앙으로 이동 |
| 지도 드래그 | 지도는 이동하지만 선택 포커스는 유지 |
| 다른 일정마커 탭 | 새 일정마커로 포커스를 변경하는 흐름으로 추정되나 최종 정책 확인 필요 |
| 뒤로가기/지도 빈 영역 선택 | 마지막 선택 마커 표시 여부가 Figma 메모상 미정 |

### 2.3 일정마커 선택 후 지도 이동

![지도 이동 상태](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-map-moved.png)

일정마커가 선택된 상태에서 지도를 움직인 상태다. Figma description에 따르면 지도 이동만으로 선택 포커스가 해제되지 않는다. 즉, 사용자가 지도를 조금 둘러봐도 “내가 어떤 일정을 보고 있던 중인지”는 유지되어야 한다.

### 2.4 지도 확대/축소

![지도 축소 상태](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-map-zoom-out.png)

![지도 확대 상태](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-map-zoom-in.png)

지도 확대/축소 시 마커의 화면상 크기는 유지되고 지도 배율만 바뀐다. Google Map API 기준 줌 레벨은 최소 3, 최대 21로 명시되어 있다.

| 상황 | 화면 반응 |
|---|---|
| 지도 확대 | 장소/일정 위치가 더 자세히 보인다. 마커 크기는 유지된다. |
| 지도 축소 | 넓은 지역이 보인다. 마커가 겹치면 `+숫자` 묶음 표현이 필요하다. |

### 2.5 장소마커 선택

![장소마커 선택 상태](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-place-marker-selected.png)

장소마커는 하나의 장소 정보만 담는다. 장소마커를 선택하면 해당 마커가 화면 중앙으로 이동하고, 장소 정보 카드가 노출된다. 동시에 해당 장소가 포함된 일정마커가 상단 중앙으로 이동하며 포커스된다.

| 카드 액션 | 동작 |
|---|---|
| 이전 일정 이동 | 해당 장소 이전의 일정/장소를 노출. 처음이면 disabled |
| 다음 일정 이동 | 해당 장소 이후의 일정/장소를 노출. 마지막이면 disabled |
| 닫기 | 해당 장소가 포함된 일정마커 선택 상태로 이동 |
| 일정에서 보기 | 해당 일정 탭 페이지로 이동하고, 일정 내 해당 위치로 이동 |
| 장소 상세보기 | 장소 상세보기 페이지로 이동 |

장소 정보 카드의 CTA는 Figma description과 해당 프레임 텍스트에서 `일정에서 보기`로 확인된다. 다만 다른 일정 목록/카드 계열 프레임에는 `지도에서 보기` 텍스트도 반복되어 나타나므로, 카드 종류별 CTA 라벨을 분리해서 관리해야 한다.

| CTA 라벨 | 확인 위치 | 해석 |
|---|---|---|
| `일정에서 보기` | 장소마커 선택 상태의 장소 정보 카드 | 선택한 장소를 해당 일정 화면의 위치에서 확인하는 액션 |
| `지도에서 보기` | 다른 일정 목록/카드 계열 프레임 | 목록 또는 일정 카드에서 지도 위치로 이동하는 액션으로 추정. 메인 장소 카드와 혼용 금지 |

### 2.6 장소 선택 닫기/뒤로가기

![뒤로가기 상태](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-back.png)

장소 카드의 닫기 버튼을 누르면 장소 선택 상태를 종료하고, 해당 장소가 포함된 일정마커 선택 상태로 돌아간다.

## 3. 핵심 UX 규칙

| 규칙 | 내용 |
|---|---|
| 바텀시트 초기 위치 | 최초 진입 시 디바이스 높이의 50%에 위치한다. |
| 바텀시트 최소 높이 | Figma에 미정으로 표시되어 있어 별도 정책이 필요하다. |
| 일정마커 위치 | 일정에 포함된 장소들의 가장 바깥 범위를 기준으로 사각형을 만들고, 그 중앙에 표시한다. |
| 일정마커 선택 | 지도 배율은 유지하고, 선택한 일정마커가 중앙에 오도록 지도 중심만 이동한다. |
| 일정마커 포커스 | 지도 이동만으로 해제되지 않는다. 뒤로가기/지도 빈 영역 선택/다른 마커 선택 시 정책은 확인 필요하다. |
| 장소마커 선택 | 장소마커가 중앙으로 이동하고 장소 카드가 표시된다. 소속 일정마커도 상단 중앙에 포커스된다. |
| 장소 선택 중 지도 이동 | 포커스가 해제되지 않는다. |
| 마커 확대/축소 | 지도 배율만 바뀌고 마커 크기는 유지된다. |
| 마커 겹침 | 줌 레벨에 따라 겹친 마커를 `+숫자`로 표시한다. 묶음 기준은 미정이다. |
| 지도 제공자 | 최신 Figma의 `Map` 컴포넌트와 기존 줌 레벨 명세 기준으로 Google Map SDK/API 기반 지도를 사용한다. |
| 우측 상단 플로팅 버튼 | 최신 Figma 기준 기본 지도 화면에는 `My page`, `Map type` 40px 원형 버튼이 우측 상단에 세로 배치된다. 지도 액션 확장 상태에서는 `Person`, `Search`, `Map`, `Gps fixed` 아이콘 버튼이 52px 간격으로 노출된다. |

## 4. 참조 정책 이미지

### 4.1 일정마커 위치 산정

![일정마커 표현방식](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-marker-expression.png)

| 단계 | 내용 |
|---|---|
| 1 | 일정 A에 포함된 장소 좌표를 지도에 표시한다. |
| 2 | 가장 바깥쪽 끝에 위치한 장소마커들을 직선으로 연결한다. |
| 3 | 연결된 영역의 바깥 범위를 다시 사각형으로 잡는다. |
| 4 | 만들어진 사각형의 중앙에 일정마커를 표시한다. |

### 4.2 일정마커와 장소마커

![일정마커 장소마커](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-marker-types.png)

| 마커 | 의미 |
|---|---|
| 일정마커 | 여러 개의 장소와 일정 정보를 담고 있는 마커 |
| 장소마커 | 하나의 장소 정보만 담고 있는 마커 |

### 4.3 마커 겹침

![마커 겹침 표현](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-marker-overlap.png)

| 줌 레벨 예시 | 표현 |
|---|---|
| 줌레벨 20 | 개별 일정마커/장소마커가 상대적으로 분리되어 보인다. |
| 줌레벨 15 | 마커 간 간격이 좁아져 겹침 가능성이 높아진다. |
| 줌레벨 10 | 겹친 마커를 `+2`, `+3`처럼 `+숫자`로 표시한다. |

### 4.4 일정생성 버튼 애니메이션

![일정생성 버튼 애니메이션](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-create-button-animation.png)

일정 생성 버튼은 텍스트가 있는 확장형 버튼과 아이콘형 버튼 사이의 전환 이미지가 있다. 다만 이 애니메이션이 어떤 상황에서 발생하는지는 Figma description에 명확히 적혀 있지 않아 정책 확인이 필요하다.

### 4.5 이미지 참조 매핑

상세 기능 명세의 `관련 이미지` 컬럼은 아래 ID를 기준으로 참조한다.

| Image ID | 파일 | 설명 |
|---|---|---|
| IMG-01 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-default.png` | 메인 기본 진입 |
| IMG-02 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-schedule-marker-selected.png` | 일정마커 선택 |
| IMG-03 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-map-moved.png` | 일정마커 선택 후 지도 이동 |
| IMG-04 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-map-zoom-out.png` | 지도 축소 |
| IMG-05 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-map-zoom-in.png` | 지도 확대 |
| IMG-06 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-place-marker-selected.png` | 장소마커 선택 |
| IMG-07 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-back.png` | 뒤로가기/선택 복귀 후보 상태 |
| IMG-08 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-marker-expression.png` | 일정마커 위치 산정 방식 |
| IMG-09 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-marker-types.png` | 일정마커/장소마커 의미 |
| IMG-10 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-create-button-animation.png` | 일정생성 버튼 애니메이션 |
| IMG-11 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/main-marker-overlap.png` | 마커 겹침 표현 |

## 5. 사용자 흐름

| 흐름 | 사용자가 하는 일 | 결과 |
|---|---|---|
| 저장 일정 둘러보기 | 메인 진입 후 바텀시트와 지도를 확인 | 저장 일정 목록과 일정마커를 함께 확인 |
| 지도에서 일정 보기 | 일정마커 선택 | 해당 일정마커가 중앙으로 이동하고 선택 상태 유지 |
| 선택한 일정 주변 탐색 | 일정마커 선택 후 지도 이동 | 지도는 움직이지만 선택 포커스는 유지 |
| 장소 정보 보기 | 장소마커 선택 | 장소마커 중앙 이동, 장소 카드 노출, 소속 일정마커 포커스 |
| 장소 카드 닫기 | 닫기 버튼 선택 | 소속 일정마커 선택 상태로 복귀 |
| 일정 상세페이지 이동 | 바텀시트 일정 선택 또는 마커/영역 선택 계열 액션 | sitemap 기준으로 해당 일정 상세페이지로 이동. 단, 지도 내 마커 탭과 상세 이동 트리거의 관계는 정책 확인 필요 |
| 지도 축소해서 분포 보기 | 지도 축소 | 겹친 마커는 `+숫자`로 묶어 표시 |
| 일정 생성 시작 | 일정 생성 버튼 선택 | `영상 링크로 만들기`, `보관함에서 가져오기`, `직접 만들기` 노출 |

## 6. 상세 기능 명세

### 6.1 지도

| Feature ID | 기능 | Trigger | 화면 반응 | 확정 수준 | 관련 이미지 |
|---|---|---|---|---|---|
| `MAP_GOOGLE_MAP_INTEGRATION` | Google Map 연동 | 메인 지도 탭 진입 | Google Map SDK/API로 지도 타일을 렌더링하고, 카메라 중심/줌/지도 타입 변경을 메인 지도 상태와 연결한다. | 확정 | IMG-01 |
| `MAP_UI_IMPLEMENTATION` | 메인지도 UI 구현 | 메인 지도 탭 진입 | Google Map 기반 지도 배경, 우측 상단 플로팅 버튼, 일정마커/장소마커, 저장한 일정 목록 패널, 일정 생성 버튼을 메인 지도 화면 구성으로 표시한다. | 확정 | IMG-01, IMG-06, IMG-10 |
| `MAP_TOP_FLOATING_BUTTONS` | 우측 상단 플로팅 버튼 | 메인 지도 탭 진입 | 지도 우측 상단에 `My page`, `Map type` 원형 플로팅 버튼을 고정 표시한다. 지도 액션 확장 상태에서는 프로필, 검색, 지도뷰, 현재위치 아이콘 버튼 묶음을 표시한다. | 확정 | IMG-01, IMG-06 |
| `MAP_INITIAL_FOCUS` | 최초 지도 위치 결정 | 메인화면 최초 진입 | Figma 메모상 지구가 줌인되며 내 위치로 확장하는 연출이 고려되어 있다. 위치 권한이 없으면 한국/서울이 보이는 위치를 기본값으로 둔다. | 메모 기반, 연출 상세 미정 | - |
| `MAP_PAN` | 지도 이동 | 지도 드래그 | 지도 중심이 이동한다. 일정마커 선택 상태라면 포커스는 유지된다. | 확정 | IMG-03 |
| `MAP_ZOOM_IN` | 지도 확대 | 핀치/지도 SDK 확대 | Google Map API 기준 최대 줌 레벨 21. 마커 크기는 유지된다. | 확정 | IMG-05 |
| `MAP_ZOOM_OUT` | 지도 축소 | 핀치/지도 SDK 축소 | Google Map API 기준 최소 줌 레벨 3. 마커 크기는 유지된다. | 확정 | IMG-04 |
| `MAP_VIEW_CHANGE` | 지도뷰 변경 | 우측 상단 지도뷰 버튼 선택 | 기본값/위성뷰를 전환한다. | sitemap/Figma 기반 | - |
| `MAP_CURRENT_LOCATION` | 현재 위치 이동 | 우측 상단 현재위치 버튼 선택 | 위치 권한이 있으면 GPS 기준 이동, 없으면 권한 팝업을 띄운다. | sitemap/Figma 기반 | - |

### 6.2 저장한 일정 바텀시트

| Feature ID | 기능 | Trigger | 화면 반응 | 확정 수준 | 관련 이미지 |
|---|---|---|---|---|---|
| `SHEET_INITIAL_HALF` | 초기 높이 | 메인화면 진입 | 바텀시트는 최초 진입 시 디바이스 높이의 50% 지점에 위치한다. | 확정 | IMG-01 |
| `SHEET_DRAG` | 높이 조절 | 핸들 드래그/스크롤 | 바텀시트가 위/아래로 이동한다. 최소 노출 영역은 미정이다. | 일부 미정 | IMG-01 |
| `SHEET_COLLAPSE_ON_PLACE_SELECT` | 장소 선택 시 하향 이동 | 바텀시트가 올라와 있는 상태에서 장소마커 선택 | 바텀시트가 아래로 스크롤된다. 이미 내려가 있으면 변화 없다. | 확정 | IMG-06 |
| `SHEET_SCHEDULE_SELECT` | 일정 상세 이동 | 바텀시트의 일정 선택 | 선택한 일정 상세페이지로 이동한다. | sitemap 기반 | IMG-01 |
| `SHEET_COUNTRY_FILTER` | 국가 필터 | 국가 필터 선택 | 저장된 일정의 국가에 따라 국가 필터가 생성된다. | sitemap 기반 | IMG-01 |
| `SHEET_DETAIL_FILTER` | 상세 필터 | 필터 선택 | 지역 필터, 여행 테마 필터, 비용 필터로 일정을 좁힌다. | sitemap 기반 | IMG-01 |

### 6.3 일정마커

| Feature ID | 기능 | Trigger | 화면 반응 | 확정 수준 | 관련 이미지 |
|---|---|---|---|---|---|
| `SCHEDULE_MARKER_SHOW` | 일정마커 노출 | 저장한 일정 로드 완료 | 메인화면에서 일정마커가 노출된다. | 확정 | IMG-01, IMG-09 |
| `SCHEDULE_MARKER_CENTER_RULE` | 일정마커 위치 산정 | 일정에 포함된 장소 좌표 계산 | 장소마커의 가장자리 끝부분을 잇고, 그 범위의 x/y축 중앙에 일정마커를 표시한다. | 확정 | IMG-08 |
| `SCHEDULE_MARKER_SELECT` | 일정마커 선택 | 일정마커 탭 | 현재 지도 배율은 유지하고, 선택한 일정마커가 화면 중앙에 위치하도록 이동한다. | 확정 | IMG-02 |
| `SCHEDULE_MARKER_FOCUS_KEEP` | 포커스 유지 | 선택 후 지도 이동 | 지도 이동만으로 포커스가 해제되지 않는다. | 확정 | IMG-03 |
| `SCHEDULE_MARKER_SWITCH` | 다른 일정마커 선택 | 선택 상태에서 다른 일정마커 탭 | 기존 포커스를 새 일정마커로 교체하는 흐름으로 추정된다. 최종 정책 확인 필요. | 추론/정책 필요 | IMG-02 |
| `SCHEDULE_MARKER_BACK` | 뒤로가기/지도선택 처리 | 선택 상태에서 뒤로가기 또는 지도 빈 영역 선택 | 마지막 선택 마커를 표시할지, 선택 이전 상태로 돌아갈지 정책 확인 필요. 장소 선택 상태의 닫기 동작과도 분리해 정의해야 한다. | 정책 필요 | IMG-07 |
| `SCHEDULE_DETAIL_FROM_MARKER` | 일정 상세 이동 후보 | 마커/영역 선택 계열 액션 | sitemap에는 마커/영역 선택 시 해당 마커가 속한 일정 상세페이지로 이동한다고 정의되어 있다. Figma의 지도 내 마커 선택 상태와 같은 액션인지 별도 CTA인지 확인 필요. | sitemap 기반/정책 필요 | - |

### 6.4 장소마커

| Feature ID | 기능 | Trigger | 화면 반응 | 확정 수준 | 관련 이미지 |
|---|---|---|---|---|---|
| `PLACE_MARKER_SHOW` | 장소마커 노출 | 저장한 일정의 장소 데이터 로드 | 저장한 일정의 장소/식당 등이 지도상에 노출된다. | 확정 | IMG-06, IMG-09 |
| `PLACE_MARKER_SELECT` | 장소마커 선택 | 장소마커 탭 | 선택한 장소마커가 중앙으로 이동하고 장소 정보 카드가 노출된다. | 확정 | IMG-06 |
| `PLACE_MARKER_SCHEDULE_FOCUS` | 소속 일정 포커스 | 장소마커 선택 | 해당 장소가 속한 일정마커가 상단 중앙으로 이동하며 포커스된다. | 확정 | IMG-06 |
| `PLACE_MARKER_SELECT_ANOTHER` | 다른 장소 선택 | 장소 선택 상태에서 다른 장소마커 탭 | 새 장소 정보 카드로 교체된다. | 확정 | IMG-06 |
| `PLACE_MARKER_FOCUS_KEEP` | 포커스 유지 | 장소 선택 상태에서 지도 이동 | 지도 이동 시에도 장소 선택 포커스는 해제되지 않는다. | 확정 | IMG-06 |
| `PLACE_PREV_NEXT` | 이전/다음 이동 | 장소 카드의 이전/다음 버튼 선택 | 처음이면 이전 disabled, 마지막이면 다음 disabled. | 확정 | IMG-06 |
| `PLACE_CLOSE` | 장소 카드 닫기 | 닫기 버튼 선택 | 해당 장소마커가 포함된 일정마커 선택 상태로 이동한다. | 확정 | IMG-07 |
| `PLACE_VIEW_IN_SCHEDULE` | 일정에서 보기 | `일정에서 보기` 선택 | 해당 일정 탭 페이지로 이동하고, 해당 일정이 있는 위치로 이동한다. | 확정 | IMG-06 |
| `PLACE_DETAIL_OPEN` | 장소 상세보기 | `장소 상세보기` 선택 | 장소 상세보기 페이지로 이동한다. | 확정 | IMG-06 |
| `PLACE_DETAIL_FROM_MARKER` | 일정 상세 이동 후보 | 마커/영역 선택 계열 액션 | sitemap 기준으로 마커 선택에서 일정 상세페이지로 이동하는 흐름이 존재한다. 장소 카드의 `일정에서 보기`/`장소 상세보기`와 어떤 관계인지 확인 필요. | sitemap 기반/정책 필요 | IMG-06 |

### 6.5 일정 생성

| Feature ID | 기능 | Trigger | 화면 반응 | 확정 수준 | 관련 이미지 |
|---|---|---|---|---|---|
| `CREATE_BUTTON_EXPAND` | 생성 방식 펼침 | 일정 생성 버튼 선택 | `영상 링크로 만들기`, `보관함에서 가져오기`, `직접 만들기` 버튼이 생성된다. | 확정 | IMG-10 |
| `CREATE_VIDEO_LINK` | 영상 링크로 만들기 | `영상 링크로 만들기` 선택 | 영상 링크 입력 기반 일정 생성 상세로 진입한다. | sitemap/description 기반 | IMG-10 |
| `CLIPBOARD_LINK_ACTIVE` | 붙여넣기 활성화 | 클립보드에 복사한 정보가 있음 | `복사한 링크 붙여넣기` 버튼이 active 상태가 된다. | 확정 | - |
| `CLIPBOARD_LINK_DISABLED` | 붙여넣기 비활성화 | 클립보드에 복사한 정보가 없음 | `복사한 링크 붙여넣기` 버튼이 disabled 상태가 된다. | 확정 | - |
| `VIDEO_LINK_INPUT` | 링크 입력 | 영상 링크 입력란에 텍스트 입력 | 최대 입력 자수 제한은 없다. 1자 이상 입력되면 `일정 생성하기` 버튼이 active 된다. | 확정 | - |
| `CREATE_SUBMIT_INITIAL` | 생성 버튼 초기 상태 | 영상 링크 생성 화면 최초 진입 | `일정 생성하기` 버튼은 disabled 상태다. | 확정 | - |
| `CREATE_SUBMIT_INVALID` | 유효하지 않은 링크 처리 | 링크 검증 실패 | `유효한 영상 링크가 아닙니다` 토스트를 노출하고 버튼을 disabled 한다. 링크가 수정되면 다시 active 된다. | 확정 | - |

## 7. 데이터 모델 초안

아래 모델은 Figma에 직접 명시된 스펙이 아니라, 위 UX 규칙을 구현하기 편하게 정리한 초안/추론이다. 실제 구현 시 프로젝트의 아키텍처와 기존 UI state 모델에 맞게 조정한다.

| Model | Field | Type | 설명 |
|---|---|---|---|
| `MainMapUiState` | `mapCamera` | `MapCameraUiModel` | 중심 좌표, 줌 레벨, 지도뷰 타입 |
| `MainMapUiState` | `sheetState` | `MainSheetState` | 바텀시트 높이/확장 상태 |
| `MainMapUiState` | `schedules` | `List<SavedScheduleUiModel>` | 저장한 일정 목록 |
| `MainMapUiState` | `scheduleMarkers` | `List<ScheduleMarkerUiModel>` | 일정마커 목록 |
| `MainMapUiState` | `placeMarkers` | `List<PlaceMarkerUiModel>` | 장소마커 목록 |
| `MainMapUiState` | `clusterMarkers` | `List<ClusterMarkerUiModel>` | `+숫자` 묶음 마커 목록 |
| `MainMapUiState` | `selectedScheduleMarkerId` | `String?` | 선택된 일정마커 ID |
| `MainMapUiState` | `selectedPlaceMarkerId` | `String?` | 선택된 장소마커 ID |
| `MainMapUiState` | `topFloatingActions` | `List<MapFloatingActionUiModel>` | 우측 상단 지도 오버레이 버튼 목록 |
| `MapCameraUiModel` | `zoomLevel` | `Float` | Google Map API 기준 3~21 범위 |
| `MapCameraUiModel` | `mapProvider` | `GoogleMap` | 메인 지도 구현 기준 제공자 |
| `ScheduleMarkerUiModel` | `centerLatLng` | `LatLng` | 장소마커 경계 박스의 중앙 좌표 |
| `ScheduleMarkerUiModel` | `placeMarkerIds` | `List<String>` | 포함된 장소마커 ID 목록 |
| `PlaceMarkerUiModel` | `latLng` | `LatLng` | 장소 좌표 |
| `ClusterMarkerUiModel` | `count` | `Int` | 겹친 마커 수 |

## 8. 미정/정책 필요

| TBD ID | 항목 | 내용 |
|---|---|---|
| `TBD-01` | 바텀시트 최소 높이 | Figma description에 “핸들 스크롤시 최소 보이는 영역 미정”으로 적혀 있다. |
| `TBD-02` | 장소마커 선택 시 줌 배율 | 장소마커 선택 시 지도 이동은 확정이나, 이동 시 지도 배율은 미정이다. |
| `TBD-03` | 마커 클러스터 기준 | 마커가 겹칠 때 어떤 기준으로 뭉칠지 정책이 필요하다. |
| `TBD-04` | 일정생성 버튼 애니메이션 트리거 | 텍스트 버튼과 아이콘 버튼 전환 이미지는 있으나 정확한 트리거가 명시되어 있지 않다. |
| `TBD-05` | 보관함에서 가져오기 상세 | Figma description 문구가 반복되어 있어 실제 진입 화면/동작을 별도 확인해야 한다. |
| `TBD-06` | 직접 만들기 상세 | sitemap에는 직접 만들기가 있으나 메인화면 문맥의 상세 UI는 별도 스펙이 필요하다. |
| `TBD-07` | 뒤로가기/지도선택 정책 | Figma 메모에 “뒤로가거나 지도선택시 마지막 선택한 마커가 표시되도록?”이라고 되어 있어 최종 정책 확인이 필요하다. |
| `TBD-08` | 위치권한 거부 기본 위치 | 메모상 한국/서울 기준이 제안되어 있으나 최종 기본 좌표와 줌 레벨은 정해야 한다. |
| `TBD-09` | 마커 선택과 일정 상세페이지 이동 관계 | sitemap에는 마커/영역 선택 시 일정 상세페이지 이동이 있으나, Figma 화면 상태는 지도 내 포커스/카드 노출을 보여준다. 동일 탭인지 별도 CTA인지 확인이 필요하다. |
| `TBD-10` | CTA 라벨 위치별 정책 | 장소 정보 카드에는 `일정에서 보기`, 다른 카드 계열에는 `지도에서 보기`가 보여 카드 종류별 라벨 기준을 확정해야 한다. |

## 9. 변경 이력

| Version | Date | Author | 변경 사항 | 근거 |
|---|---|---|---|---|
| `v0.1.0` | 2026-06-08 | Codex | 메인화면 스펙 최초 작성. 화면 상태별 이미지, 핵심 UX 규칙, 상세 기능 명세 정리 | Figma description / 화면 캡처 / sitemap |
| `v0.2.0` | 2026-06-14 | Codex | 서브에이전트 리뷰 결과 반영. 뒤로가기/지도선택 정책을 미정으로 분리, 마커/영역 선택과 일정 상세페이지 이동 관계 보강, CTA 라벨 위치별 구분, 이미지 매핑표 추가, 데이터 모델 초안의 구현 추론 성격 명시 | Spec Document Reviewer Agent 재검토 `Pass` |
| `v0.2.1` | 2026-07-05 | Codex | 최신 `메인화면 / 지도` Figma 섹션 기준으로 Google Map 연동과 우측 상단 플로팅 버튼 Feature를 추가하고 메인지도 UI Feature 범위를 보강 | Figma node `17789:48296`, `17789:48299`, `17789:50456`, `17789:50508` 메타데이터 재검토 |
