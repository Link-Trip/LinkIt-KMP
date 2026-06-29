# 보관함 스펙

> 기준 Figma: [Pingo v3.0.3](https://www.figma.com/design/Pym5oUSWQjyWVtN86oj6lb/Pingo--v3.0.3?node-id=13008-1945&m=dev)  
> 기준 영역: `보관함`  
> 기준 section: `15091:152408`  
> 스펙 버전: `v0.1.3`  
> 작성일: 2026-06-14

## 1. 한눈에 보기

보관함은 저장된 항목을 폴더/보관 묶음 단위로 관리하고, 검색 결과에서는 리스트 형태로 항목을 탐색하는 탭이다. Figma에는 `보관함 A안 - 보관된(저장된) 항목만 보여주는 안`과 `보관함 B안 - 지도와 함께 보여지는 안`이 함께 배치되어 있어 최종안 확정이 필요하다.

| 큰 덩어리 | 역할 |
|---|---|
| 보관함 A안 기본 | `수원`, `숙소6개` 같은 폴더/보관 묶음 그리드 확인 |
| 검색 | 키워드 입력과 검색 결과 표시 |
| 검색 결과 리스트 | 검색 후 지역/여행 테마/비용 필터와 항목 리스트 표시 |
| 항목 추가 | 저장 항목 추가하기, 직접 추가하기 |
| 폴더명 입력 | 저장 항목 추가하기 화면 안에서 폴더명 입력과 중복 이름 검증 |
| 상세/장소 정보 | 상세 화면은 존재하지만 목록에서의 진입 정책은 미정 |

## 2. 화면 상태별 읽기

### 2.1 보관함 A안 기본: 폴더 그리드

![보관함 기본](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/library-default.png)

보관함 A안 기본 상태다. 하단 탭에서 보관함이 선택되어 있고, `수원`, `숙소6개` 같은 폴더/보관 묶음 카드와 추가 타일, 더보기 아이콘이 보인다. 이 상태는 리스트라기보다 폴더 그리드에 가깝다.

### 2.2 검색 입력

![보관함 검색](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/library-search.png)

검색 필드에 `일본`이 입력된 상태다. 검색어 입력 후 결과 화면으로 이어진다.

### 2.3 검색 결과

![보관함 검색 결과](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/library-search-result.png)

`“일본” 검색결과`, `총 8개 항목`, 지역/여행 테마/비용 필터가 표시된다. 검색 결과 카드에는 태그, 제목, 일정/비용성 정보, 요약 문구가 노출된다. Figma 텍스트 일부는 placeholder(`토지주소`)로 보이며, 기본 카드의 `수원`, `숙소6개`, 폴더 입력의 `내용 입력`, `n/n`도 실제 데이터/카운트 정책 확인이 필요하다.

### 2.4 추가 메뉴 열림

![보관함 추가 메뉴 열림](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/library-filtered-list.png)

보관함 기본 폴더 그리드에서 추가 버튼을 누르면 화면이 dim 처리되고 `저장 항목 추가하기`, `직접 추가하기` 메뉴가 떠 있다. 이 이미지는 필터 결과가 아니라 추가 메뉴 오픈 상태의 근거로 사용한다.

### 2.5 저장 항목 추가하기와 폴더명 입력

![저장 항목 추가하기 입력 전](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/folder-add-empty.png)

![폴더 추가 입력 상태](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/folder-add-filled.png)

화면 헤더는 `저장 항목 추가하기`이며, 본문에 추천 영상 둘러보기와 폴더명 입력 영역이 함께 배치되어 있다. `폴더명`, `내용 입력`, `n/n`, `같은 이름의 폴더가 있어요` 문구가 확인된다. 폴더명 입력과 중복 검증이 필요하지만 오류 문구의 실제 노출 조건은 확인해야 한다.

### 2.6 보관함 상세/긴 화면

![보관함 상세 긴 화면](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/library-detail-long.png)

긴 상세 화면에는 일차별 일정, 장소 정보, AI 분석 정보, 추천 영상, 장소 상세보기 등 여러 상세 구성요소가 포함된다. 단, 보관함 목록에서 어떤 항목을 선택했을 때 이 상세로 이동하는지는 명시 근거가 부족해 추론으로 둔다.

### 2.7 보관함 B안: 지도와 함께 보여지는 안

![보관함 B안 지도 포함](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/library-map-variant.png)

Figma에는 `보관함 B안 - 지도와 함께 보여지는 안` 메모와 함께 지도 + 바텀시트 형태의 보관함 대체안이 있다. 빈 상태로 볼 근거는 없으므로 B안 후보로만 다룬다.

## 3. 핵심 UX 규칙

| 규칙 | 내용 |
|---|---|
| 보관함 탭 | 하단 탭의 보관함 선택 시 진입한다. |
| 검색 | 검색어 입력 후 결과 제목을 `“검색어” 검색결과` 형태로 표시한다. |
| 필터 | 지역, 여행 테마, 비용 필터 UI를 제공한다. 적용 결과 정책은 확인 필요하다. |
| 정렬 | 결과 목록에 `최신순` 정렬이 보인다. |
| 항목 추가 | 저장 항목 추가하기와 직접 추가하기 진입점을 제공한다. |
| 폴더명 검증 | 같은 이름의 폴더가 있을 때의 오류 문구가 준비되어 있다. 실제 노출 조건은 확인 필요하다. |
| A/B안 | 보관 항목만 보여주는 안과 지도 포함 안 중 최종안 결정 필요 |

## 4. 사용자 흐름

| 흐름 | 사용자가 하는 일 | 결과 |
|---|---|---|
| 보관함 진입 | 하단 `보관함` 탭 선택 | A안 기준 폴더/보관 묶음 그리드 표시 |
| 검색 | 검색어 입력 | 검색 결과 화면 표시 |
| 필터 적용 | 지역/여행 테마/비용 선택 | 필터 UI 선택. 결과 갱신 정책은 확인 필요 |
| 항목 상세 보기 | 보관 항목 선택 | 상세 화면 이동 후보. 타입별 진입 정책 확인 필요 |
| 추가 메뉴 열기 | 추가 버튼 선택 | `저장 항목 추가하기`, `직접 추가하기` 메뉴 노출 |
| 저장 항목 추가 | 저장 항목 추가하기 선택 | 저장 항목 추가하기 화면으로 진입 |
| 직접 추가 | 직접 추가하기 선택 | 수동 항목 추가 흐름 진입 |
| 폴더명 입력 | 저장 항목 추가하기 화면에서 폴더명 입력 | 폴더명 입력값과 글자 수 표시 |
| 중복 폴더명 입력 | 기존 폴더명 입력 | `같은 이름의 폴더가 있어요` 오류 문구 노출 후보 |

## 5. 상세 기능 명세

| Feature ID | 기능 | Trigger | 화면 반응 | 확정 수준 | 관련 이미지 |
|---|---|---|---|---|---|
| `LIBRARY_OPEN` | 보관함 진입 | 하단 `보관함` 선택 | A안 기준 폴더/보관 묶음 그리드 표시 | 확정 | IMG-01 |
| `LIBRARY_SEARCH_INPUT` | 검색어 입력 | 검색 필드 입력 | 입력값 표시 | 확정 | IMG-02 |
| `LIBRARY_SEARCH_RESULT` | 검색 결과 표시 | 검색 실행 | `“검색어” 검색결과`, 총 항목 수 표시 | 확정 | IMG-03 |
| `LIBRARY_FILTER_OPEN` | 필터 선택 | 지역/여행 테마/비용 선택 | 필터 UI 선택. 옵션 표시/결과 갱신 정책 확인 필요 | 정책 필요 | IMG-03 |
| `LIBRARY_SORT_SELECT` | 정렬 | `최신순` 선택 | 정렬 옵션 변경 | 정책 필요 | IMG-03 |
| `LIBRARY_ITEM_OPEN` | 항목 상세 | 목록 항목 선택 | 상세/긴 화면으로 이동 | 추론 | IMG-07 |
| `LIBRARY_ADD_MENU_OPEN` | 추가 메뉴 열기 | 추가 버튼 선택 | `저장 항목 추가하기`, `직접 추가하기` 메뉴 표시 | 확정 | IMG-04 |
| `LIBRARY_ADD_SAVED` | 저장 항목 추가 | 저장 항목 추가하기 선택 | 저장 항목 추가하기 화면으로 진입 | 확정 | IMG-05 |
| `LIBRARY_ADD_MANUAL` | 직접 추가 | 직접 추가하기 선택 | 수동 추가 흐름 진입 후보 | 정책 필요 | IMG-04 |
| `FOLDER_NAME_INPUT` | 폴더명 입력 | 저장 항목 추가하기 화면 입력 | `폴더명`, `내용 입력`, `n/n` 표시 | 확정 | IMG-05 |
| `FOLDER_NAME_DUPLICATE` | 중복 폴더명 검증 | 기존 폴더명 입력 | `같은 이름의 폴더가 있어요` 문구 표시 후보. 실제 노출 조건 확인 필요 | 정책 필요 | IMG-06 |
| `LIBRARY_MAP_VARIANT` | B안 지도 포함 보관함 | B안 선택 | 지도와 함께 보여지는 보관함 후보 표시 | 대체안/정책 필요 | IMG-08 |

## 6. 이미지 참조 매핑

| Image ID | 파일 | 설명 |
|---|---|---|
| IMG-01 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/library-default.png` | 보관함 기본 |
| IMG-02 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/library-search.png` | 검색 입력 |
| IMG-03 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/library-search-result.png` | 검색 결과 |
| IMG-04 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/library-filtered-list.png` | 추가 메뉴 열림 |
| IMG-05 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/folder-add-empty.png` | 저장 항목 추가하기 입력 전 |
| IMG-06 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/folder-add-filled.png` | 폴더 추가 입력 상태 |
| IMG-07 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/library-detail-long.png` | 보관함 상세 긴 화면 |
| IMG-08 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/library/library-map-variant.png` | 보관함 B안: 지도와 함께 보여지는 안 |

## 7. 미정/정책 필요

| TBD ID | 항목 | 내용 |
|---|---|---|
| `TBD-01` | 보관함 A/B안 | 보관 항목만 보여주는 안과 지도 포함 안 중 최종안 결정 필요 |
| `TBD-02` | 항목 타입 | 일정, 장소, 영상, 폴더가 같은 목록에 섞이는지 타입별 탭이 있는지 확인 필요 |
| `TBD-03` | 정렬 옵션 | `최신순` 외 정렬 옵션 확인 필요 |
| `TBD-04` | 상세 이동 | 목록 항목 선택 시 어떤 상세 화면으로 이동하는지 타입별 정책 필요 |
| `TBD-05` | 폴더 저장 조건 | 폴더명 최소/최대 길이와 저장 버튼 활성 조건 확인 필요 |
| `TBD-06` | placeholder 데이터 | `토지주소`, `수원`, `숙소6개`, `내용 입력`, `n/n`의 실제 데이터 매핑과 카운트 정책 확인 필요 |
| `TBD-07` | 필터 적용 결과 | 지역/여행 테마/비용 필터 UI는 있으나 결과 갱신 방식은 확인 필요 |

## 8. 변경 이력

| Version | Date | Author | 변경 사항 | 근거 |
|---|---|---|---|---|
| `v0.1.0` | 2026-06-14 | Codex | 보관함 스펙 최초 작성 | Figma metadata / 화면 캡처 / sitemap |
| `v0.1.1` | 2026-06-14 | Codex | 서브에이전트 리뷰 반영. A안 폴더 그리드/B안 지도 포함 보관함 구분, 상세 이동/필터 동작 확정 수준 조정, 추가 메뉴와 placeholder 정책 보강 | Spec Document Reviewer Agent 1차 리뷰 |
| `v0.1.2` | 2026-06-14 | Codex | 재리뷰 대비. B안 지도 포함 보관함 asset 파일명을 의미 기준으로 정리 | 본 에이전트 검토 |
| `v0.1.3` | 2026-06-14 | Codex | 재리뷰 반영. IMG-04를 필터/목록이 아닌 추가 메뉴 열림 상태로 정정 | Spec Document Reviewer Agent 2차 리뷰 |
