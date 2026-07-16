# AGENTS.md

## 브랜치 네이밍 규칙

형식: `{작업종류}/#{이슈번호}-{작업내용}`

| 작업 종류　 | prefix     |
| -------------| ------------|
| New Feature | `feature`  |
| Improvement | `improve`  |
| Bug Fix　　 | `fix`      |
| Refactoring | `refactor` |
| Chore　　　 | `chore`    |

- 작업 내용은 영문 소문자 snake_case로 간결하게 작성
- `develop` 브랜치에서 분기
- 예시: `feature/#15-home_screen`, `fix/#20-login_crash`

## 브랜치 계층 구조

```
main          ← 최상위 (릴리스)
└── develop   ← 개발 통합 브랜치
    └── feature/*, fix/*, improve/*, refactor/*, chore/*  ← 작업 브랜치
```

- 작업 브랜치 → `develop` 으로 머지
- `develop` → `main` 으로 머지

## 커밋 메시지 규칙

형식: `{타입}: 간결한 설명`

| 타입　　　 | 설명　　　　　　　　　　　　 |
| ------------| ------------------------------|
| `feat`　　 | 새로운 기능　　　　　　　　　|
| `fix`　　　| 버그 수정　　　　　　　　　　|
| `refactor` | 리팩토링　　　　　　　　　　 |
| `chore`　　| 빌드, 설정 등 기타　　　　　 |
| `docs`　　 | 문서 변경　　　　　　　　　　|
| `style`　　| 코드 포맷팅 (동작 변경 없음) |
| `test`　　 | 테스트 추가/수정　　　　　　 |

- 예시: `feat: 홈 화면 UI 구현`, `fix: 로그인 시 크래시 수정`

## 참고 문서

### docs/METRO_INSTRUCTION.md
아래 상황에서 반드시 읽고 참고할 것:
- 새 Activity, ViewModel, iOS ViewController를 생성할 때
- 새 feature 모듈을 추가할 때
- Metro DI 관련 에러(MissingBinding, Couldn't call constructor 등)가 발생했을 때
- IosAppGraph에 수동 등록이 필요할 때

### docs/ARCHITECTURE.md
아래 상황에서 반드시 읽고 참고할 것:
- 프로젝트 전체 구조나 모듈 의존성에 대한 질문을 받았을 때
- 새 모듈을 추가하거나 모듈 간 의존성을 변경할 때
- 아키텍처 설계 판단이 필요할 때

### domain/README.md
아래 상황에서 반드시 읽고 참고할 것:
- 도메인 모델, Repository 인터페이스, UseCase를 추가하거나 수정할 때
- UseCase를 만들지 말지(생성·생략 기준) 판단이 필요할 때
- ViewModel에 비즈니스 로직을 배치할지 고민될 때
- `withContext`, `Dispatchers` 지정이 필요한지 판단할 때 (디스패처 규칙)

### data/README.md
아래 상황에서 반드시 읽고 참고할 것:
- 새 API, DataSource, Repository를 추가하거나 수정할 때
- DTO, Mapper를 작성할 때
- Ktorfit, HttpClient 등 네트워크 설정 관련 작업을 할 때
- data 모듈의 DI 구성(DataGraph, DataScope, `@ContributesBinding` 바인딩)을 수정할 때

### docs/NAVIGATION_STRUCTURE.md
아래 상황에서 반드시 읽고 참고할 것:
- Navigation 관련 코드를 수정하거나 새 Route를 추가할 때
- NavigationState, LinkItNavigator, LinkItNavDisplay 관련 작업을 할 때
- 화면 전환 흐름에 대한 질문을 받았을 때

### docs/DESIGN_TOKEN_MAPPING.md
아래 상황에서 반드시 읽고 참고할 것:
- Figma 디자인을 Compose 코드로 구현할 때
- 새 디자인 토큰을 추가할 때

### docs/COMPOSE_IMPLEMENTATION_GUIDE.md
아래 상황에서 반드시 읽고 참고할 것:
- Figma 디자인을 Compose 코드로 구현할 때
- 새 UI 컴포넌트를 작성할 때
- 스크린샷 테스트를 추가할 때
