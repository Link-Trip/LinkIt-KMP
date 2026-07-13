# domain 모듈

도메인 모델, Repository 인터페이스, UseCase를 제공하는 KMP 라이브러리 모듈이다.
클린아키텍처의 도메인 계층으로, ViewModel(feature)과 데이터 계층(data) 사이의 경계를 정의한다.
feature 모듈은 이 모듈의 인터페이스만 바라보고, 구현체(RepositoryImpl)는 Metro DI를 통해 주입된다.

이 문서의 예시 코드는 가상의 `Link` 도메인을 기준으로 작성된 가이드 코드이며, 형식만 참고할 것.

> 이 모듈은 **순수 Kotlin(commonMain)** 으로만 구성한다. Android SDK, Compose, Ktor 등 플랫폼·UI·네트워크 타입을 두지 않으며, 덕분에 플랫폼 환경 없이 공통 단위 테스트가 가능하다.

## 레이어 흐름

```
ViewModel (feature)
    ↓ 비즈니스 규칙이 있으면        ↓ 단순 조회면 직접
UseCase (domain/usecase)
    ↓
Repository 인터페이스 (domain/repository)
    ↓ Metro @Binds
RepositoryImpl (data/repository)
```

ViewModel은 UseCase 또는 Repository 인터페이스만 알고, 구현체는 알지 못한다.
RepositoryImpl과 `@Binds` 등록, DataSource, DTO, Mapper 등 데이터 계층 규칙은 [data/README.md](../data/README.md)가 단일 출처다.

## 디렉토리 구조

```
domain/src/commonMain/kotlin/com/linkit/company/domain/
├── model/          # 도메인 모델 (data class, enum, sealed class)
├── repository/     # Repository 인터페이스 (구현체는 data 모듈)
└── usecase/        # UseCase (생성 기준을 만족하는 경우에만)
```

## 도메인 모델 작성 규칙

`model/` 패키지에 비즈니스 개념을 표현하는 순수 Kotlin 타입으로 작성한다.

```kotlin
// model/Link.kt
data class Link(
    val id: Long,
    val title: String,
    val url: String,
    val memo: String,
) {
    val hasMemo: Boolean get() = memo.isNotEmpty()
}
```

- 파일명은 비즈니스 개념 이름 그대로 (`Link.kt`, `LinkStatus.kt`)
- **도메인 관점의 파생 값·판단 로직은 모델의 프로퍼티/함수로 허용한다** (`hasMemo`, 상태 기반 가능 여부 판단 등). 단, UI 관점의 파생 로직(표시용 포맷팅, 라벨 문자열, 색상 결정 등)은 모델에 두지 않는다 — feature 계층의 책임
- `enum`, `sealed class`도 비즈니스 개념이면 함께 둔다. 파일이 늘어나면 `model/enum` 등 하위 패키지로 분리 가능
- **서버 스키마가 아닌 비즈니스 개념 기준으로 설계한다** — 서버 스펙에만 존재하는 필드는 포함하지 않는다
- nullable 정리, 기본값 처리는 data 모듈의 Mapper에서 끝내고 여기서는 깨끗한 non-null 모델을 지향한다
- `@Serializable` 등 직렬화 어노테이션을 붙이지 않는다 — 직렬화가 필요한 건 DTO(data)의 사정이다

## Repository 인터페이스 작성 규칙

`repository/` 패키지에 데이터 접근의 **계약(인터페이스)** 만 작성한다.

```kotlin
// repository/LinkRepository.kt
interface LinkRepository {
    suspend fun getLinks(): List<Link>
    suspend fun createLink(title: String, url: String, memo: String?): Link
    suspend fun deleteLink(id: Long)
}
```

- 이름은 `XxxRepository`
- 반환 타입은 **도메인 모델 또는 원시값**만 — DTO/Entity가 시그니처에 나타나면 안 된다
- 요청 데이터는 개별 파라미터로 받는다 (Request DTO 금지)
- 구현체(`XxxRepositoryImpl`)와 `RepositoryGraph` `@Binds` 등록은 data 모듈에서 한다 — [data/README.md](../data/README.md#repository-작성-규칙) 참고

### suspend vs Flow 선택 기준

| 상황 | 시그니처 |
| --- | --- |
| 일회성 조회, 생성·수정·삭제 커맨드 | `suspend fun getLinks(): List<Link>` |
| 데이터 변화의 지속 구독 (로컬 저장소 관찰 등) | `fun observeLinks(): Flow<List<Link>>` |

- `Flow`를 반환하는 함수에는 `suspend`를 붙이지 않는다 — 구독 시작 자체는 비차단이다
- 관찰 함수는 `observeXxx` 네이밍으로 일회성 조회(`getXxx`)와 구분한다
- 현재는 원격 API만 있으므로 `suspend`가 기본이다. 로컬 저장소(Room KMP 등) 도입 시 화면이 변화를 계속 반영해야 하는 데이터부터 `Flow`를 적용한다

## UseCase 생성·생략 기준

비즈니스 규칙이 존재하면 UseCase로 분리한다. ViewModel이 Repository를 직접 호출하는 것은 아래 조건을 **모두** 만족하는 단순 조회로 한정한다.

| 조건 | 설명 |
| --- | --- |
| 단일 API | 화면에 필요한 데이터를 Repository 호출 하나로 모두 받는다 |
| 단순 표시 | 응답 데이터를 그대로(또는 단순 변환만) 화면에 보여준다 |
| 재사용 없음 | 여러 화면에서 공통으로 재사용할 행위가 아니다 |
| 비즈니스 규칙 없음 | 정렬·필터링·권한 판단·상태 전이 등 어떤 비즈니스 판단도 없다 |

```
위 조건 4가지를 모두 만족하는가?
    YES → ViewModel이 Repository 직접 호출
    NO  → UseCase 작성 (domain/usecase)
```

하나라도 만족하지 않으면 UseCase를 작성하며, 비즈니스 규칙은 종류와 무관하게 ViewModel에 두지 않는다. 아래는 참고용 예시로, 목록에 없는 비즈니스 규칙도 UseCase로 분리한다.

| 예시 유형 | 구체적 예 |
| --- | --- |
| 여러 Repository 조합 | 링크 목록 + 사용자 설정을 합쳐 화면 데이터 구성 |
| 정렬·필터링 | 고정 링크 → 최신순으로 정렬 |
| 권한 판단 | 로그인 상태에 따라 기능 노출 여부 결정 |
| 상태 전이 | 영상 분석이 완료 상태일 때만 일정 생성 가능 판단 |
| 여러 화면에서 재사용하는 행위 | 링크 저장 처리를 탐색·보관함·상세 세 곳에서 공통 사용 |

## UseCase 작성 규칙

`usecase/` 패키지에 클래스로 작성한다.

```kotlin
// usecase/GetSortedLinksUseCase.kt
@Inject
class GetSortedLinksUseCase(
    private val linkRepository: LinkRepository,
) {
    suspend operator fun invoke(): List<Link> {
        return linkRepository.getLinks()
            .sortedByDescending { it.id }
    }
}
```

- 이름은 행위 동사로 시작하는 `XxxUseCase` (`GetSortedLinksUseCase`, `SaveLinkUseCase`)
- public 함수는 `operator fun invoke(...)` **하나만** 둔다
- Repository **인터페이스**만 주입받는다 — 실제 구현체는 data 모듈에 있고 Metro가 런타임에 바인딩한다
- **다른 UseCase 주입을 허용한다** — 여러 화면에서 재사용하는 행위를 조합할 때 로직을 복사하지 않고 주입으로 해결한다. 단, UseCase 간 순환 의존은 금지
- Metro `@Inject`로 생성자 주입을 활성화한다. 인터페이스 없는 구체 클래스이므로 별도 `@Binds` 등록은 필요 없다 — ViewModel 생성자에 그대로 주입하면 된다. Metro 에러 발생 시 [docs/METRO_INSTRUCTION.md](../docs/METRO_INSTRUCTION.md) 참고

## 디스패처 규칙 — 메인 세이프 계약

**모든 suspend 함수는 메인 세이프해야 한다** — 어떤 디스패처에서 호출해도 안전하도록 작성한다.

| 계층 | 규칙 |
| --- | --- |
| ViewModel (feature) | 디스패처 지정 없이 `viewModelScope`에서 그대로 호출한다. `Dispatchers.IO` 일괄 지정 금지 |
| UseCase (domain) | 기본적으로 전환하지 않는다. 대량 정렬·파싱 등 **CPU 집약 연산이 실측으로 확인될 때만** `withContext(Dispatchers.Default)`로 한정 |
| Repository·DataSource (data) | IO 바운드 작업의 스레드 처리는 data 계층의 책임. 단, 아래 이유로 실제 전환 코드는 거의 필요 없다 |

Ktor, Room 등 suspend 기반 라이브러리는 **자체적으로 메인 세이프**하다 — 메인 스레드에서 호출해도 내부에서 IO 처리를 알아서 하므로, 관성적인 `withContext(Dispatchers.IO)` 래핑을 하지 않는다. `Dispatchers.IO` 전환은 블로킹 API(파일 I/O 등)를 suspend 함수로 감쌀 때만 사용하며, 이는 data 계층에서 한다.

## 새 도메인 기능 추가 절차

가상의 `Link` 도메인 기준:

1. **도메인 모델 작성** — `model/Link.kt`
2. **Repository 인터페이스 작성** — `repository/LinkRepository.kt`
3. **데이터 계층 구현** — DTO, Mapper, DataSource, RepositoryImpl과 그래프 등록은 [data/README.md — 새 API 추가 절차](../data/README.md#새-api-추가-절차) 참고
4. **UseCase 작성 여부 판단** — [생성·생략 기준](#usecase-생성생략-기준)에 따라 필요한 경우에만 `usecase/`에 추가
5. **ViewModel에서 사용** — UseCase 또는 Repository 인터페이스를 생성자 주입

## 금지 규칙 (안티패턴)

| 금지 사항 | 이유 |
| --- | --- |
| domain이 data, feature, core:ui, core:designsystem에 의존 | domain은 최하위 계층. 의존 방향은 [docs/ARCHITECTURE.md](../docs/ARCHITECTURE.md#의존성-규칙)가 단일 출처 |
| Android SDK·Compose·Ktor 타입 사용 | 순수 Kotlin 유지. 플랫폼 상세는 data/feature의 책임 |
| Repository 인터페이스 시그니처에 DTO/Entity 노출 | 데이터 계층 구현 상세가 도메인으로 유출됨. 도메인 모델·원시값만 사용 |
| UseCase가 UiState·SideEffect·navigation을 의존 | domain은 UI 레이어를 알아서는 안 됨. MVI 타입은 feature/core:common의 소관 |
| ViewModel 안에 비즈니스 규칙 작성 | 재사용·테스트 불가. UseCase로 분리 |
| UseCase에 public 함수 여러 개 | 하나의 UseCase는 하나의 행위. `invoke` 하나로 제한 |
| 단순 조회에 관성적으로 UseCase 생성 | Repository 위임만 하는 빈 껍데기 UseCase는 계층만 늘림. [생성·생략 기준](#usecase-생성생략-기준) 참고 |
| UI 관점의 파생 로직을 도메인 모델에 배치 | 표시용 포맷팅·라벨·색상 결정은 feature 계층의 책임. 모델에는 도메인 관점의 판단만 허용 |
| UseCase에서 관성적 `withContext(Dispatchers.IO)` 래핑 | Ktor·Room suspend API는 자체 메인 세이프. [디스패처 규칙](#디스패처-규칙--메인-세이프-계약) 참고 |
| Mapper를 domain 모듈에 배치 | domain이 data(DTO)에 역의존하게 됨 — 규칙 상세는 [data/README.md](../data/README.md#mapper-작성-규칙-dto--domain-모델)가 단일 출처 |

## 의존성 추가 가이드

- 현재 의존은 `core:common` 하나뿐이며, 이 상태를 유지하는 것을 지향한다
- 라이브러리 추가는 순수 Kotlin/KMP common 라이브러리(coroutines, kotlinx-datetime 등)로 제한한다
- Android 전용·UI·네트워크 라이브러리는 이 모듈에 추가하지 않는다 — 각각 feature/data 모듈의 소관
