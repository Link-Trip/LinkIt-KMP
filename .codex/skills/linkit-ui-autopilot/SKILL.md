---
name: linkit-ui-autopilot
description: LinkIt KMP에서 Pingo v3.0.3 Figma 전체 UI를 자율적으로 계획, 구현, 재개, 시각 검증하고 배포할 때 사용한다. Compose Multiplatform, MVI, Metro DI, Navigation3, SCC, 테스트, 브랜치, 커밋, PR 규칙을 유지한다. 사용자가 전체 Pingo UI, 여러 화면 일괄 구현, 무개입 또는 자율 구현, 네비게이션 구조 완성, 전체 UI 작업 재개를 요청할 때 사용한다. 작고 독립적인 단일 화면에는 linkit-figma-compose를 사용한다.
---

# LinkIt UI 자율 구현

Pingo 전체 UI를 작고 검증 가능한 화면 묶음으로 나누어 구현하고 중단된 작업을 재개하라. 런타임 기능은 의도적으로 얕게 유지하되, 모든 화면과 네비게이션 경로는 실제로 사용할 수 있게 만들어라.

## 필수 문서 읽기

애플리케이션 코드를 수정하기 전에 다음 파일을 읽어라.

- `AGENTS.md`
- `docs/DESIGN_TOKEN_MAPPING.md`
- `docs/COMPOSE_IMPLEMENTATION_GUIDE.md`
- `docs/NAVIGATION_STRUCTURE.md`
- `docs/ARCHITECTURE.md`
- `docs/METRO_INSTRUCTION.md`
- `docs/SITEMAP.md`
- `docs/specs/spec-command-center-policy.md`
- `docs/specs/scc-action-bridge.md`
- `docs/specs/feature-list.schema.md`
- 관련된 모든 `docs/specs/*-screen.md`
- `references/visual-fidelity-gate.md`

문서와 현재 코드가 일치한다고 가정하지 말고 실제 구현 상태를 조사하라.

## 작업 범위 지키기

다음을 구현 범위에 포함하라.

- `commonMain`의 Compose Multiplatform UI
- 고정 fixture 기반의 재현 가능한 화면 상태
- 기존 MVI 계약과 Metro ViewModel
- 완전한 Navigation3 Route, Entry, 뒤로가기, Android Activity 경계
- iOS 호환 공통 UI와 호스트 콜백
- 내보낼 수 있는 Figma 아이콘, 일러스트, 썸네일, 정적 지도 이미지
- Preview, 스크린샷 테스트, Figma 비교, 네비게이션 검사

사용자가 범위를 넓히지 않는 한 다음은 제외하라.

- Repository, 네트워크 호출, 영속성, 인증, 분석 도구, 운영 SDK 연동
- 실제 지도, GPS, 클립보드, YouTube, AI 분석 기능
- 추측에 기반한 Domain/Data 추상화

화면 조작을 확인할 수 있도록 로컬 상태 또는 fixture를 사용하라. 실제 미래 연동 경계에만 짧은 `TODO`를 남겨라.

Figma 프레임마다 MVI를 만들지 말고 독립적인 화면 흐름마다 MVI 소유자 하나를 만들어라. 시각적 변형은 하위 Composable을 재사용하라. Route 인자나 상태 복원이 필요한 경우에만 `SavedStateHandle`과 Assisted Injection을 추가하라.

## 승인 경계 지키기

사용자가 검토한 작업 계획을 승인하지 않았다면 코드 상태를 조사하고 계획만 제시하라. 애플리케이션 코드와 SCC tracking은 수정하지 마라. 사용자가 검토된 계획에 대해 진행, 시작, 구현 계속, 실행을 명시하면 승인된 것으로 간주하라.

사용자가 외부 작업을 명시적으로 허용한 경우에만 GitHub Issue를 만들고, 브랜치를 push하거나 PR을 생성하라. 권한을 받은 뒤에는 설치된 `gh` CLI를 사용하고 GitHub 플러그인을 요구하지 마라.

## 새로 시작하기 전에 재개하기

Git에서 무시되는 `.figma-workspace/autopilot.md`를 실행 체크포인트로 사용하라. 호출될 때마다 다음 순서로 처리하라.

1. 체크포인트가 있으면 읽어라.
2. `git status`, 최근 커밋, 구현된 Route, 테스트 결과를 조사하라.
3. 마지막 완료 묶음이 실제로 유효한지 확인할 수 있는 최소 검사를 다시 실행하라.
4. 완료된 작업을 다시 만들지 말고 첫 번째 미완료 묶음부터 계속하라.

체크포인트에 Figma URL, 정확한 루트/프레임 node ID, 라이브 캡처 시각과 SHA-256, 목표 일치율, Issue, 브랜치, 작업 큐, 완료된 검사, 남은 작업, 해결되지 않은 시각 차이를 기록하라. 검증된 화면 묶음을 완료할 때마다 갱신하라.

## 실행 절차

### 1. 사전 점검

1. 저장소 루트를 확인하고 사용자의 관련 없는 변경을 보존하라.
2. GitHub 작업 권한을 받았다면 `gh auth status`와 `gh repo view`를 실행하라.
3. Figma 접근 권한을 확인하고 사용자가 전달한 URL의 file key와 node ID를 그대로 조사하라. 전체 보드 링크를 단일 화면이 아닌 화면 인벤토리의 루트로 취급하되, 그 루트 바깥의 유사 프레임으로 대체하지 마라.
4. `node docs/specs/scc-action.mjs audit`을 실행하라.
5. `./gradlew :app-android:assembleDebug`를 실행하고 기존 오류를 수정하기 전에 기록하라.
6. 기존 프로젝트 코드, 디자인 시스템 컴포넌트, 플랫폼 API로 해결할 수 없음을 확인하기 전에는 의존성을 추가하지 마라.

권한을 받았고 작업 브랜치가 없다면 다음을 실행하라.

1. `gh issue create`로 전체 UI 작업용 Issue 하나를 만들어라.
2. 파괴적인 reset 없이 로컬 `develop`을 `origin/develop`과 동기화하라.
3. `develop`에서 `feature/#<issue>-pingo_ui` 브랜치를 만들어라.

셸 명령에서 `#`이 포함된 브랜치 이름은 반드시 따옴표로 감싸라.

### 2. 화면 인벤토리 만들기

현재 Figma 보드를 시각적 원천으로 사용하고 기존 스펙을 색인된 대체 근거로 사용하라. 사용자가 지정한 루트의 자손 프레임만 인벤토리에 넣고, 이름이 같거나 더 완성돼 보인다는 이유로 다른 페이지·섹션의 프레임을 선택하지 마라. 다음 화면군을 대조하라.

1. 공통 디자인 시스템과 네비게이션 셸
2. 메인 지도와 바텀시트, 마커, 카드, 생성 오버레이, 상세 화면
3. 탐색
4. 보관함/Storage
5. 마이페이지/설정
6. 온보딩, 일정 생성, 분석, 여행 상세, 저장, 지원 Dialog

대표 Figma 프레임마다 부모 경로, node ID, 프레임명, 크기, 시각 상태, 대상 모듈, Route, MVI 소유자, fixture, 스크린샷 테스트, 선행 작업 묶음을 체크포인트에 기록하라. 별도의 커밋 대상 Feature Registry를 만들지 마라.

자료가 충돌하면 다음 우선순위를 적용하라.

1. 사용자가 전달한 정확한 node ID 아래에서 방금 조회한 라이브 Pingo Figma 프레임과 Description
2. 동일 node ID에서 작업 시작 시 저장한 라이브 캡처와 구조 정보
3. `docs/specs/*-screen.md`와 캡처된 Asset
4. 디자인 시스템 토큰과 컴포넌트
5. `docs/SITEMAP.md`
6. 가장 작고 보수적인 추론

라이브 노드에 접근할 수 없으면 기존 캡처를 사용해 계속할 수 있지만 이를 `degraded evidence`로 체크포인트에 표시하라. 이 상태에서는 최신 Figma와 일치한다고 보고하거나 시각 완료로 확정하지 마라.

### 2-1. 프레임 증거 계약 고정하기

코드를 작성하기 전에 `references/visual-fidelity-gate.md`의 프레임 증거 계약을 대표 상태마다 작성하라.

1. 정확한 프레임의 라이브 스크린샷과 구조/디자인 컨텍스트를 `.figma-workspace/evidence/`에 저장하고 SHA-256을 기록하라.
2. Toolbar, Tab, Section header, Card/List, Bottom navigation 영역을 각각 잘라 별도 비교 대상으로 만들어라.
3. 각 영역의 **존재 요소**, 정확한 문구, 순서, 선택 상태, 글꼴 속성, 색상, 경계와 주요 좌표를 기록하라.
4. 컴포넌트 인스턴스는 원본 컴포넌트 기본값이나 이웃 Variant가 아니라 해당 프레임에서 해석된 현재 속성과 visible layer를 사용하라.
5. 구현 요소와 Figma 요소를 양방향 대조하라. 대응 node가 없는 아이콘, 버튼, 장식, 문구를 편의상 추가하지 마라.
6. MVI 초기 상태와 비교 대상 Figma 상태를 명시적으로 연결하라. 첫 번째 Tab이 선택됐을 것이라고 추론하지 마라.
7. 코딩 직전과 최종 검증 직전에 같은 node ID를 다시 조회하라. 크기, 문구, 자식 구조, 캡처 해시가 달라졌으면 이전 증거 계약을 폐기하고 다시 작성하라.

### 3. 기반 복구하기

먼저 기본 빌드를 복구하라. 화면을 만들기 전에 기존 디자인 시스템을 재사용하고 필요한 부분만 바로잡아라.

- Figma 변수를 기존 색상, 타이포그래피, 간격, Shape, Border, Icon 기반에 매핑하라.
- 새 컴포넌트를 만들기 전에 기존 컴포넌트를 재사용하라.
- 같은 시각 패턴이 두 번 이상 나타날 때만 공통 컴포넌트를 추가하라.
- 가능한 경우 Figma 원본 Asset을 내보내고, 형태가 다른 Material Icon으로 대체하지 마라.
- UI 전용 범위에서는 지도 SDK를 추가하지 말고 Figma 정적 지도 이미지를 사용하라.

별도 Claude CLI 프로세스에 작업을 위임하는 `scripts/figma-orchestrate.sh`는 실행하지 마라. Codex의 Figma 접근 기능과 `scripts/compare-figma.sh`를 직접 사용하라.

### 4. 세부 스타일링 전에 네비게이션 완성하기

문서화된 멀티 Activity와 Navigation3 멀티 백스택 구조를 유지하라.

각 화면 목적지에 대해 다음을 실행하라.

1. `LinkItNavKey` Route를 추가하라.
2. Serializer를 등록하라.
3. Feature가 소유하는 Entry를 추가하라.
4. 올바른 호스트에 Entry를 등록하라.
5. 화면의 CTA를 Route, 로컬 상태 변경, 뒤로가기에 연결하라.
6. Screen Composable에는 Navigator 타입 대신 콜백을 전달하라.

Map, Storage/Library, Explore의 최상위 백스택을 독립적으로 유지하라. 확인된 Figma 흐름과 아키텍처 규칙이 요구하지 않는 한 기존 Intro, Home, Schedule Activity 경계를 사용하라. 필요한 Android 전용 전환은 iOS 호스트 콜백으로 대응하라.

Route 도달 가능성, 탭 상태 보존, 중복 이동, 뒤로가기에 대한 집중 테스트를 추가하라.

### 5. 검증 가능한 화면 묶음으로 구현하기

컴포넌트 재사용을 높이기 위해 다음 순서로 구현하라.

1. 빌드 수정, 테마, Asset, 네비게이션 셸
2. 메인 지도와 공통 카드/바텀시트
3. 탐색
4. 보관함/Storage
5. 마이페이지/설정
6. 온보딩과 일정/상세 흐름

각 묶음에서 다음을 실행하라.

1. 프레임 증거 계약의 정확한 문구와 초기 선택 상태로 fixture를 먼저 고정하라.
2. 화면 흐름의 MVI 계약을 추가하거나 수정하라.
3. Fixture를 사용하는 Stateless Screen/Content Composable을 만들어라.
4. Toolbar action은 증거 계약에 visible node가 있을 때만 추가하라. 기능상 유용하다는 이유로 화면에 새 버튼을 만들지 마라.
5. Tab의 선택 색상, 비선택 opacity, 글꼴 weight, indicator 위치·너비·두께를 실제 인스턴스 값으로 구현하라.
6. Section 제목은 문구뿐 아니라 font family, size, line height, weight, letter spacing, 색상, 제목-설명 간격을 대조하라. 의미가 비슷한 Typography token을 임의로 선택하지 마라.
7. 화면 검사에 도움이 되는 경우에만 플랫폼 Preview를 추가하라.
8. 대표 시각 상태마다 스크린샷 테스트 하나를 추가하라.
9. 네비게이션과 로컬 조작을 연결하라.
10. 다음 묶음으로 넘어가기 전에 빌드, 비교, 수정, 체크포인트, 커밋을 완료하라.

Git에서 무시되는 생성 스크린샷을 실수로 커밋하지 마라. 저장소 정책이 골든 이미지 추적을 허용하도록 바뀐 경우에만 의도한 테스트 Baseline을 커밋하라.

### 6. 시각적·구조적으로 검증하기

각 스크린샷 테스트에 Figma 프레임 크기와 System Bar crop 값을 사용하라. 각 화면 묶음에서 다음을 실행하라.

1. 의도적으로 Baseline을 갱신할 때만 모듈의 `recordRoborazziDebug`를 실행하라.
2. 회귀 검증에는 `verifyRoborazziDebug`를 실행하라.
3. 대표 프레임마다 `scripts/compare-figma.sh`를 실행하라.
4. 전체 화면 점수와 별개로 Toolbar, Tab, 각 Section header, Card/List, Bottom navigation 영역별 diff와 overlay를 직접 확인하라.
5. **추가 요소 없음**, **누락 요소 없음**, **문구 완전 일치**, **선택 상태 일치**를 픽셀 점수보다 먼저 검사하라. 이 네 항목은 허용 오차 0이다.
6. 레이아웃, 크기, 색상, 타이포그래피, 간격, Shape, Asset 순서로 차이를 수정하라.
7. 코드의 고정 문구와 프레임 증거 계약을 검색 또는 테스트로 대조하고, Figma Text layer를 읽을 수 없을 때만 고해상도 캡처를 보조 근거로 사용하라.

사용자가 다른 값을 지정하지 않으면 목표 일치율은 90%로 설정하라. 다음 조건 중 하나를 만족할 때만 해당 프레임의 반복을 종료하라.

- 목표 일치율에 도달함
- 세 번 연속 개선 폭이 0.5% 미만이고, 남은 차이가 모두 코드로 수정할 수 없는 항목으로 확인되어 기록됨

각 묶음 이후 집중 테스트와 최소 관련 빌드를 실행하라. 전체 완료 전에는 다음을 실행하라.

```sh
./gradlew :app-android:assembleDebug
./gradlew :app-shared:compileKotlinIosSimulatorArm64
node docs/specs/scc-action.mjs audit
```

정확한 Gradle Task 이름이 다르면 검사를 생략하지 말고 동일한 Task를 찾아 실행하라.

### 7. SCC 상태를 정확하게 유지하기

SCC JSON, JS, Event 파일을 직접 수정하지 마라. 모든 tracking 변경에 `node docs/specs/scc-action.mjs ...`를 사용하고 이후 audit을 실행하라.

UI가 존재한다는 이유만으로 82개 동작 Feature 전체를 구현 완료로 표시하지 마라. 선언된 동작, 테스트, 스크린샷까지 실제로 충족한 Feature만 `done`으로 표시하라. Backend, SDK, 정책 결정이 필요한 동작 Feature는 열린 상태로 유지하고 계획과 PR 설명에 UI 전용 범위를 명시하라.

### 8. 커밋, Push, PR 생성하기

저장소의 한국어 Conventional 형식으로 작은 화면 묶음 커밋을 만들어라.

- `fix: 홈 네비게이션 빌드 오류 수정`
- `feat: 메인 지도 화면 UI 구현`
- `feat: 탐색 및 보관함 화면 UI 구현`
- `test: 화면 스크린샷 검증 추가`

Push 권한을 받았다면 화면 묶음이 검증될 때마다 push하라. 전체 완료 후 `gh pr create`로 `develop` 대상 PR을 만들어라. 범위, 스크린샷과 일치율, 테스트, 네비게이션 검증 범위, 영향받은 Feature UID, UI 전용 제외 범위, 코드로 수정할 수 없는 남은 차이를 포함하라.

## 자율적으로 계속 진행하기

일반적인 시각적 모호성, 토큰 이름, fixture 내용, 상태 이름, 컴포넌트 분리, Route 배치에 대해 사용자에게 질문하지 마라. 자료 우선순위 규칙을 적용하고 계속 진행하라.

일시적인 Figma, Gradle, 네트워크 오류는 더 좁은 요청이나 저장소에 캐시된 근거를 사용해 재시도하라. Figma 노드 하나를 읽을 수 없더라도 기존 스펙과 Asset으로 계속 진행하라.

재시도 후에도 필요한 인증을 사용할 수 없음, 허용 범위 밖의 파괴적 작업이 불가피함, 라이선스 제약, 제품 범위를 바꾸는 상충된 근거처럼 안전하게 해결할 수 없는 하드 블로커에서만 중단하라. 정확한 블로커, 완료한 화면 묶음, 다음 실행 단계를 보고하라.

사용자가 명시적으로 허용하지 않는 한 Sub-agent에게 작업을 위임하지 마라.

## 완료 조건

다음을 모두 만족할 때만 완료하라.

- 인벤토리에 등록된 모든 대표 Figma 프레임을 구현했거나 승인된 범위에서 명시적으로 제외함
- 도달 가능한 네비게이션 경로에 Placeholder 화면이 남아 있지 않음
- 화면의 모든 조작 가능한 요소가 이동, 뒤로가기, 재현 가능한 로컬 UI 상태 변경 중 하나를 수행함
- 독립적인 화면 흐름이 저장소 MVI와 Metro 규칙을 따름
- Android 빌드, iOS 공통 UI 컴파일, 네비게이션 검사, 스크린샷 검증을 통과함
- 코드로 수정 가능한 시각 차이를 모두 해결했고 나머지 차이를 기록함
- 사용자가 지정한 정확한 Figma root/frame node에서 최종 라이브 캡처를 다시 획득함
- 모든 대표 상태에서 추가/누락 요소, 문구, 선택 상태의 의미적 차이가 0임
- Toolbar action, Tab 강조, Section 제목이 영역별 diff 검사를 통과함
- SCC audit을 통과하고 tracking이 기능 완료를 과장하지 않음
- 작업 트리에 의도하지 않은 파일이 없음
- 허용된 커밋, push, PR 생성을 완료함

최종 Route Map, 화면 묶음별 커밋, 테스트, 상태별 일치율, 해결되지 않은 차이, SCC 상태, PR URL을 한국어로 보고하라.
