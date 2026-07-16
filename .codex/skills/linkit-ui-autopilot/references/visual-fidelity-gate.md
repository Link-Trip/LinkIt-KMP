# 시각 충실도 게이트

전체 화면 유사도는 작은 영역의 의미적 오류를 숨길 수 있다. 대표 Figma 상태마다 아래 계약과 검사를 적용하라.

## 1. 원본 고정

`.figma-workspace/evidence/`에 라이브 원본을 저장하고 체크포인트에 다음을 기록하라.

| 항목 | 값 |
|---|---|
| Figma URL | 사용자 제공 URL |
| File key / Root node | URL에서 추출한 값 |
| Frame parent path / node / name | 지정 루트의 자손 경로 |
| Frame size / 상태명 | width × height / 초기 상태 |
| 조회 시각 / 캡처 SHA-256 | ISO-8601 / hash |
| 증거 수준 | `live` 또는 `degraded evidence` |

- 지정 루트 밖의 동명·유사 프레임을 대체 원본으로 사용하지 마라.
- 문서 캡처는 라이브 노드가 실패했을 때만 보조 근거로 사용하라.
- 라이브 조회가 실패한 상태에서 최신 Figma 일치를 확정하지 마라.

## 2. 영역별 의미 계약

각 대표 상태에서 다음 표를 채워라.

| 영역 | Source node | 존재 요소와 순서 | 정확한 문구 | 상태 | 시각 속성 |
|---|---|---|---|---|---|
| Toolbar |  | title, visible action |  | action visibility | 높이, padding, icon |
| Tab |  | tabs, indicator |  | selected index | weight, color, opacity, indicator geometry |
| Section header |  | title, description, more |  | visible/hidden | font, line height, gap, position |
| Card/List |  | image, badge, text, action |  | order/count | size, radius, crop |
| Bottom navigation |  | items |  | selected item | icon, label, indicator |

다음을 허용 오차 0으로 검사하라.

1. Figma에 없는 구현 요소가 없어야 한다.
2. Figma에 있는 요소가 누락되지 않아야 한다.
3. 문구, 띄어쓰기, 조사, 숫자, 구두점, 강제 줄바꿈이 일치해야 한다.
4. 초기 selected/disabled/visible 상태가 일치해야 한다.
5. 목록의 내용과 순서가 일치해야 한다.

## 3. 취약 영역 규칙

### Toolbar

- visible child node가 확인된 action만 구현하라.
- 과거 캡처, 다른 화면, 컴포넌트 master에 아이콘이 있다는 이유로 버튼을 추가하지 마라.
- 네비게이션에 필요한 동작이 Figma에 없으면 화면 장식을 만들지 말고 기존 gesture, system back 또는 host callback으로 해결하라.

### Tab

- 초기 MVI 상태를 정확한 Figma frame/variant와 연결하라.
- 선택된 label의 색상·weight와 비선택 opacity를 각각 읽어라.
- indicator의 x/y, width, height가 Tab 전체 너비인지 content 영역 너비인지 측정하라.
- 논리적으로 첫 번째가 기본일 것이라는 추론을 금지하라.

### Section header

- Text layer 문구를 그대로 fixture에 옮겨라.
- font family, size, line height, weight, letter spacing, fill, title-description gap을 기록하라.
- 토큰 이름이 의미상 맞더라도 계산된 값이 다르면 사용하지 마라. 기존 토큰 매핑을 바로잡거나 정확한 대응 토큰을 추가하라.

## 4. 비교 절차

1. 라이브 Figma와 구현을 같은 viewport, density, system-bar crop으로 캡처하라.
2. 전체 화면과 함께 Toolbar, Tab, 각 Section header, 첫 Card/List, Bottom navigation crop을 만들어라.
3. 각 crop을 나란히 보고 overlay와 diff를 확인하라.
4. 먼저 추가/누락 요소, 문구, 선택 상태를 고치고 이후 geometry, typography, color, spacing, shape, asset을 고쳐라.
5. 최종 검증 직전에 같은 node를 다시 조회하고 해시와 의미 계약을 비교하라.
6. 원본이 바뀌었으면 Baseline을 덮어쓰기 전에 계약부터 갱신하고 구현을 다시 검증하라.

전체 점수가 목표를 넘더라도 영역별 의미 계약 하나라도 실패하면 완료하지 마라.
