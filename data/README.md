# data 모듈

Repository 구현체, DataSource, DTO, Mapper, 네트워크 설정을 제공하는 KMP 라이브러리 모듈이다.
클린아키텍처의 데이터 계층으로, 외부(feature 모듈)에서는 `domain` 모듈의 Repository 인터페이스만 바라보고 이 모듈의 구현체는 Metro DI를 통해 주입된다.

이 문서의 예시 코드는 가상의 `Link` 도메인을 기준으로 작성된 가이드 코드이며, 형식만 참고할 것.

## 레이어 흐름

```
ViewModel (feature)
    ↓
Repository 인터페이스 (domain)
    ↓ Metro @ContributesBinding
RepositoryImpl (data/repository)     ← DTO/저장값 → Domain 변환, 출처 조율(캐싱) 지점
    ↓                        ↓
RemoteDataSource 인터페이스   LocalDataSource 인터페이스   (data/datasource)
    ↓ Metro @ContributesBinding   ↓ Metro @ContributesBinding
RemoteDataSourceImpl         LocalDataSourceImpl
    ↓ (Ktorfit API 호출,       ↓ (DataStore Preferences 접근,
       DTO 반환)                 원시값 반환)
Ktorfit API 인터페이스 (data/api)
    ↓
HttpClient (Android: OkHttp / iOS: Darwin)
```

각 계층은 아래 계층의 **인터페이스에만 의존**하고, 구현체 연결은 Impl 클래스의 `@ContributesBinding(DataScope::class)`으로 Metro DI가 담당한다 ([Metro 바인딩 등록](#metro-바인딩-등록) 참고).
DataSource는 출처(원격/로컬)별로 분리하며, 두 출처의 조합은 Repository가 담당한다.

## 디렉토리 구조

```
data/src/
├── commonMain/kotlin/com/linkit/company/data/
│   ├── api/                  # Ktorfit API 인터페이스
│   ├── core/                 # KtorConfig (HttpClient 공통 설정·공통 헤더·에러 변환),
│   │                         # DataStoreFactory (DataStore 생성), DeviceIdProvider (플랫폼별 기기 식별자)
│   ├── dto/                  # 서버 응답/요청 DTO (@Serializable) — 도메인별 하위 패키지(auth/, tripplan/, video/)로 그룹화
│   │                         # 공통 래퍼(ApiResponse, ErrorResponse)는 dto/ 루트에 둔다
│   ├── mapper/               # DTO → Domain 모델 변환 확장 함수
│   ├── datasource/
│   │   └── {도메인}/          # 도메인별 Remote/Local DataSource 인터페이스 + Impl (+ 복합 파라미터 Param 클래스)
│   ├── repository/           # domain Repository 인터페이스의 구현체
│   ├── DataGraph.kt          # baseUrl, Json, Ktorfit 제공
│   └── DataScope.kt          # data 계층 Metro 스코프
└── androidMain/kotlin/com/linkit/company/data/
    └── AndroidDataGraph.kt   # Android용 HttpClient(OkHttp), DataStore, DeviceIdProvider 제공
```

## 네트워크 레이어 구성

### HttpClient 공통 설정 — `core/KtorConfig.kt`

플랫폼별 HttpClient가 공유하는 설정은 `defaultKtorConfig(json, accessTokenProvider)` / `defaultJson()`에 모여 있다.

`defaultKtorConfig`가 설치하는 것:

- `ContentNegotiation` + kotlinx.serialization Json
- **공통 헤더 플러그인**(`LinkTripHeaders`) — Idempotency-Key·Authorization 자동 첨부 ([공통 헤더 자동 첨부](#idempotency-key--공통-헤더-자동-첨부) 참고). `accessTokenProvider` 파라미터로 저장된 accessToken을 읽는다 (구현은 `AuthLocalDataSource.getAccessToken()`)
- **`HttpResponseValidator`** — 실패(4xx/5xx) 응답을 `LinkTripApiException`으로 변환 ([실패 응답 처리](#실패-응답-처리--linktripapiexception) 참고)

`defaultJson()` 설정:

- `ignoreUnknownKeys = true` : 서버에 새 필드가 추가돼도 파싱이 깨지지 않도록 함
- `isLenient = true`, `encodeDefaults = true`
- `explicitNulls = false` : **null 필드는 직렬화에서 생략된다** — "null = 필드 미전송(변경하지 않음)" 패턴에 활용 (예: `UpdateTripPlanRequest.title = null`)

### 플랫폼별 제공 바인딩 (HttpClient·DataStore·DeviceIdProvider)

| 플랫폼 | HttpClient 엔진 | 제공 위치 |
| --- | --- | --- |
| Android | OkHttp | `data/androidMain` — `AndroidDataGraph` |
| iOS | Darwin | `app-shared/iosMain` — `IosAppGraph` (수동 등록) |

플랫폼 그래프는 HttpClient 외에 아래 바인딩도 함께 제공한다.

- `DataStore<Preferences>` — 플랫폼별 파일 경로로 `createLinkItDataStore` 호출 ([DataStore 규칙](#datastore-규칙) 참고)
- `DeviceIdProvider` — Android는 `Settings.Secure.ANDROID_ID`, iOS는 `UIDevice.identifierForVendor` (nil이면 생성 UUID)

data 모듈에는 iosMain 소스셋이 없으므로, iOS 쪽 바인딩은 `IosAppGraph`에서 직접 제공한다. iOS DI 관련 제약은 [docs/METRO_INSTRUCTION.md](../docs/METRO_INSTRUCTION.md) 참고.

### Ktorfit 인스턴스 — `DataGraph`

`DataGraph`가 `baseUrl`, `Json`, `Ktorfit`을 `DataScope`에 제공한다.

```kotlin
@ContributesTo(DataScope::class)
interface DataGraph {

    @Provides
    fun provideBaseUrl(): String = "..."

    @Provides
    fun provideJson(): Json = defaultJson()

    @Provides
    fun provideKtorfit(httpClient: HttpClient, baseUrl: String): Ktorfit {
        return Ktorfit.Builder()
            .httpClient(httpClient)
            .baseUrl(baseUrl)
            .build()
    }
}
```

`DataScope`는 `AndroidAppGraph` / `IosAppGraph`의 `additionalScopes = [DataScope::class]`로 앱 그래프에 병합되므로, `@ContributesTo(DataScope::class)` / `@ContributesBinding(DataScope::class)`으로 기여한 바인딩은 Android에서는 별도 등록 없이 사용 가능하다. 단, **iOS는 멀티모듈 자동 수집이 동작하지 않아** `IosAppGraph`에 동일한 바인딩을 `@Binds`/`@Provides`로 수동 등록해야 한다 — [docs/METRO_INSTRUCTION.md](../docs/METRO_INSTRUCTION.md) 참고.

## Ktorfit API 작성 규칙

`api/` 패키지에 `internal interface`로 작성한다. Ktorfit 어노테이션(`@GET`, `@POST`, `@Path`, `@Query`, `@Body` 등)으로 엔드포인트를 선언하면 컴파일러 플러그인이 구현체를 생성한다.

```kotlin
// api/LinkApi.kt
internal interface LinkApi {

    @GET("links")
    suspend fun getLinks(): ApiResponse<List<LinkResponse>>

    @GET("links/{id}")
    suspend fun getLink(@Path("id") id: Long): ApiResponse<LinkResponse>

    @POST("links")
    suspend fun createLink(@Body request: CreateLinkRequest): ApiResponse<LinkResponse>

    @DELETE("links/{id}")
    suspend fun deleteLink(@Path("id") id: Long): ApiResponse<Unit>
}
```

- 가시성은 `internal` — API 인터페이스는 data 모듈 밖으로 노출하지 않는다
- 엔드포인트는 baseUrl 기준 상대 경로로 작성한다
- 반환 타입은 항상 `ApiResponse<DTO>` — domain 모델을 직접 반환하지 않는다 ([공통 응답 구조](#공통-응답-구조) 참고)
- 응답 데이터가 없는 API(삭제 등)는 `ApiResponse<Unit>`으로 선언한다

API 인스턴스는 DataSourceImpl에서 `ktorfit.create<LinkApi>()`로 생성한다 (컴파일러 플러그인이 처리).

## 공통 응답 구조

서버([LinkTrip API](https://linktrip.cloud/api/swagger-ui/index.html))의 모든 응답은 아래 두 가지 구조 중 하나를 따른다.
**성공(2xx)과 실패(4xx/5xx)의 바디 구조가 서로 다르다**는 점에 주의할 것.

### 성공 응답 (2xx) — 공통 래퍼

모든 성공 응답은 동일한 래퍼로 감싸져 온다.

```json
{
  "status": 200,
  "message": "OK",
  "data": { ... }
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `status` | Int | O | HTTP 상태 코드와 **별개로 바디에 담기는 의미상 코드** (예: 로그인 API는 HTTP 200이어도 신규 회원가입이면 `status=201`) |
| `message` | String | O | 응답 메시지 |
| `data` | T | X | 응답 데이터. **없으면 필드 자체가 생략됨** (예: 삭제 API) |

#### 2xx 코드별 의미

성공은 200만이 아니다. 코드에 따라 클라이언트 처리가 달라진다.

| 코드 | 의미 | 사용 API 예시 |
| --- | --- | --- |
| 200 | 요청 성공 | 전체 |
| 201 | 리소스 생성 | `POST /auth/login` — 신규 회원 가입 시 |
| 202 | 처리 진행 중 → **폴링 필요** | `POST /video/analyze`, `GET /video/schedule/{id}` — 영상 분석 PENDING/PROCESSING 상태 |

201/202도 바디는 200과 동일한 공통 래퍼 구조다.

### 실패 응답 (4xx/5xx)

실패 응답은 공통 래퍼가 **아닌** 별도 구조로 온다.

```json
{
  "code": "NOT_FOUND_TRIP_PLAN",
  "message": "여행 계획을 찾을 수 없습니다."
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `code` | String | 에러 식별 코드 (아래 표 참고) |
| `message` | String | 사용자 노출 가능한 에러 메시지 |

#### 에러 코드 목록

| HTTP | `code` | 의미 | 발생 API |
| --- | --- | --- | --- |
| 400 | `BAD_REQUEST_VALIDATION` | 요청 값 validation 실패 | `POST /auth/login` |
| 400 | `BAD_REQUEST_MISSING_IDEMPOTENCY_KEY` | Idempotency-Key 헤더 누락 | 멱등성 키 필수 API 전체 |
| 400 | `BAD_REQUEST_YOUTUBE_URL` | 유효하지 않은 YouTube URL | `POST /video/analyze` |
| 400 | `BAD_REQUEST_VIDEO` | 자막을 추출할 수 없는 영상 | `POST /video/analyze` |
| 400 | `BAD_REQUEST_DISCOVER_QUERY` | `country`/`region` 동시 사용 불가 | `GET /video/discover/category` |
| 401 | `UNAUTHORIZED_AUTHENTICATION_FAILED` | 인증 실패 | 인증 필요 API 전체 |
| 403 | `FORBIDDEN_TRIP_PLAN` | 본인의 여행 계획이 아님 | `GET/PUT/DELETE /trip-plans/{id}` |
| 404 | `NOT_FOUND_TRIP_PLAN` | 존재하지 않는 여행 계획 | `GET/PUT/DELETE /trip-plans/{id}` |
| 404 | `NOT_FOUND_VIDEO_ANALYSIS_TASK` | 존재하지 않는 영상 분석 결과 | `GET /video/schedule/{id}` |
| 409 | `DUPLICATE_REQUEST` | 동일 멱등성 키의 요청이 이미 처리 중 | 멱등성 키 필수 API 전체 |
| 429 | `TOO_MANY_REQUESTS` | API 요청 횟수 초과 | `POST /video/analyze` |

### Kotlin 표현 가이드

공통 래퍼와 에러 바디는 `dto/`에 아래 형태로 표현한다.

```kotlin
// dto/ApiResponse.kt — 성공 응답 공통 래퍼
@Serializable
internal data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T? = null,   // 서버가 필드를 생략할 수 있으므로 nullable + 기본값 필수
)

// dto/ErrorResponse.kt — 실패 응답 바디
@Serializable
internal data class ErrorResponse(
    val code: String,
    val message: String,
)
```

### 실패 응답 처리 — LinkTripApiException

실패(4xx/5xx) 응답은 `defaultKtorConfig`의 `HttpResponseValidator`가 `ErrorResponse`를 파싱해 **`LinkTripApiException`으로 변환해 던진다**. API·DataSource·Repository에서 에러 바디를 직접 파싱하지 않는다.

```kotlin
// domain 모듈: exception/LinkTripException.kt
class LinkTripApiException(
    val errorCode: LinkTripErrorCode,   // 서버 에러 코드 enum. 매칭 실패 시 UNKNOWN 폴백
    val httpStatus: Int,
    override val message: String,
) : Exception(message)
```

- 예외 타입과 `LinkTripErrorCode` enum은 **domain 모듈**(`domain/exception/`)에 있다 — ViewModel/UseCase가 data 모듈에 의존하지 않고 `errorCode`로 분기해야 하기 때문
- 서버에 새 에러 코드가 추가돼도 깨지지 않도록, 매칭되지 않는 코드는 `LinkTripErrorCode.UNKNOWN`으로 폴백한다 (에러 코드 추가 시 enum과 위 표를 함께 갱신할 것)

### Idempotency-Key · 공통 헤더 자동 첨부

non-GET 요청은 `Idempotency-Key` 헤더가 **필수**다 (UUID v4 권장).

| 대상 API | 누락 시 | 동일 키 중복 요청 시 |
| --- | --- | --- |
| `POST /auth/login`, `POST /video/analyze`, `PUT/DELETE /trip-plans/{id}` | 400 `BAD_REQUEST_MISSING_IDEMPOTENCY_KEY` | 409 `DUPLICATE_REQUEST` |

클라이언트에서는 `defaultKtorConfig`의 공통 헤더 플러그인(`LinkTripHeaders`)이 자동 처리하므로, **API·DataSource에서 헤더를 직접 다루지 않는다**.

- non-GET 요청: `Idempotency-Key`(UUID v4) 자동 첨부
- 저장된 accessToken이 있으면: `Authorization: Bearer` 자동 첨부 (로그인 전에는 미첨부)
- 주의: `HttpRequestRetry` 도입 시 재시도마다 Idempotency-Key가 재생성되어 멱등성이 무력화되므로, 그때는 attributes 기반으로 키를 고정해야 한다

## DTO 작성 규칙

`dto/{도메인}/` 하위 패키지에 `@Serializable` data class로 작성한다. 특정 도메인에 속하지 않는 공통 래퍼(`ApiResponse`, `ErrorResponse`)만 `dto/` 루트에 둔다.

```kotlin
// dto/link/LinkResponse.kt
@Serializable
data class LinkResponse(          // DataSource 인터페이스에 노출되므로 public
    val id: Long,
    val title: String,
    val url: String,
    val memo: String? = null,
)

// dto/link/CreateLinkRequest.kt
@Serializable
internal data class CreateLinkRequest(   // DataSourceImpl 내부에서만 사용하므로 internal
    val title: String,
    val url: String,
    val memo: String? = null,
)
```

- 서버 응답/요청 스키마를 그대로 반영한다 — 서버가 nullable이면 DTO도 nullable
- 서버 필드명과 다르게 쓰려면 `@SerialName("server_name")` 사용
- 요청 바디용 DTO는 `XxxRequest`, 응답용은 `XxxResponse` 네이밍

### DTO 가시성 기준

| 대상 | 가시성 | 이유 |
| --- | --- | --- |
| DataSource 인터페이스 시그니처에 노출되는 Response DTO | `public` | iOS 수동 DI 등록(`IosAppGraph`) 경로에서 타입 해석이 가능해야 함 |
| Request DTO, `ApiResponse`, `ErrorResponse` | `internal` | DataSourceImpl·KtorConfig 내부에서만 사용 — 모듈 밖으로 노출하지 않음 |

### Request DTO 생성 기준

| 상황 | 방식 |
| --- | --- |
| 파라미터 2개 이상 | `XxxRequest` data class 생성 |
| 단일 필드라도 서버가 JSON 객체 바디 요구 | `XxxRequest` data class 생성 (원시값을 `@Body`로 보내면 객체가 아닌 bare value로 직렬화됨) |
| 단일 파라미터 (`@Path`, `@Query`) | DTO 없이 원시값으로 전달 |

- `encodeDefaults = true` 설정이므로 기본값을 가진 필드도 직렬화되어 전송된다. 단 `explicitNulls = false`이므로 **null 필드는 생략된다** — "null = 필드 미전송(변경하지 않음)" 의미로 활용한다 (예: `UpdateTripPlanRequest.title = null`)

## Mapper 작성 규칙 (DTO → Domain 모델)

`mapper/` 패키지에 **DTO의 확장 함수**로 작성한다. domain 모델의 non-null 보장, 기본값 처리는 이 지점에서 끝낸다.

```kotlin
// mapper/LinkMapper.kt
internal fun LinkResponse.toDomain(): Link {
    return Link(
        id = id,
        title = title,
        url = url,
        memo = memo.orEmpty(),
    )
}
```

- 함수명은 `toDomain()`, 파일명은 `{도메인}Mapper.kt`
- Mapper는 반드시 data 모듈에 둔다 — domain 모듈에 두면 domain이 data(DTO)에 역의존하게 된다
- nullable 필드는 여기서 `.orEmpty()`, `?: 기본값` 등으로 정리하여 domain 모델을 깨끗하게 유지한다
- **Request DTO는 매퍼를 거치지 않는다** — DataSourceImpl에서 직접 생성하며, 역방향 매퍼(`toRequest()`)를 만들지 않는다

## DataSource 작성 규칙

`datasource/{도메인}/` 패키지에 인터페이스 + Impl 쌍으로 작성한다.
데이터 출처에 따라 `Remote` / `Local` 접미사를 **항상 명시**한다 — 출처가 하나뿐이어도 생략하지 않는다.

```
datasource/link/
├── LinkRemoteDataSource.kt (+ Impl)   # 원격 API 호출
└── LinkLocalDataSource.kt  (+ Impl)   # 로컬 저장소 접근 (필요한 도메인만)
```

출처 공통 규칙:

- 데이터 출처 호출만 담당하고 비즈니스 로직을 넣지 않는다
- **다른 DataSource를 주입/참조하지 않는다** — Remote/Local 조합은 Repository의 책임
- domain 모델을 반환하거나 파라미터로 받지 않는다 — **파라미터는 원시값, 반환은 DTO/저장값**
- 원시값으로 풀기 어려운 **복합 파라미터는 datasource 패키지에 `XxxParam` 값 객체**를 만들어 받는다 (예: `TripPlanItemOrderParam`). domain 모델 → Param 변환은 RepositoryImpl 책임
- Impl에는 `@Inject`와 `@ContributesBinding(DataScope::class)`을 붙인다 ([Metro 바인딩 등록](#metro-바인딩-등록) 참고)

### RemoteDataSource — 원격 API 호출

Ktorfit API를 호출하고 **DTO만 반환한다**. 파라미터는 원시값으로 받고, **Request DTO 조립은 Impl 내부에서** 한다.

```kotlin
// datasource/link/LinkRemoteDataSource.kt
interface LinkRemoteDataSource {
    suspend fun getLinks(): List<LinkResponse>
    suspend fun deleteLink(id: Long)
    suspend fun createLink(title: String, url: String, memo: String?): LinkResponse
}

// datasource/link/LinkRemoteDataSourceImpl.kt
@Inject
@ContributesBinding(DataScope::class)
class LinkRemoteDataSourceImpl(
    ktorfit: Ktorfit,
) : LinkRemoteDataSource {

    private val api = ktorfit.create<LinkApi>()

    override suspend fun getLinks(): List<LinkResponse> {
        return api.getLinks().data.orEmpty()
    }

    override suspend fun deleteLink(id: Long) {
        api.deleteLink(id)
    }

    override suspend fun createLink(title: String, url: String, memo: String?): LinkResponse {
        val request = CreateLinkRequest(
            title = title,
            url = url,
            memo = memo,
        )
        return checkNotNull(api.createLink(request).data) { "links 생성 응답에 data가 없습니다" }
    }
}
```

- `data`가 반드시 있어야 하는 응답은 `checkNotNull(...) { "설명 메시지" }`로, 리스트 응답은 `orEmpty()`로 언래핑한다. 응답 데이터가 없는 API(`ApiResponse<Unit>`)는 `data`를 무시한다

### LocalDataSource — 로컬 저장소 접근

로컬 저장소에 접근하고 **저장 기술의 모델만 반환한다** — DTO나 domain 모델을 반환하지 않는다.

- 현재 도입된 저장 기술은 **DataStore Preferences** 하나다 — 반환 타입은 원시값(`String?` 등)
- DB(Room KMP 등) 도입 시에는 Entity를 반환하며, Entity ↔ Domain 변환은 `mapper/`에 두고 변환 책임은 Repository에 있다 (도입 시 이 섹션에 작성 규칙 추가할 것)

#### DataStore 규칙

```kotlin
// datasource/link/LinkLocalDataSourceImpl.kt
@Inject
@ContributesBinding(DataScope::class)
class LinkLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
) : LinkLocalDataSource {

    override suspend fun getLastViewedLinkId(): String? {
        return dataStore.data.first()[KEY_LAST_VIEWED_LINK_ID]
    }

    override suspend fun saveLastViewedLinkId(id: String) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_VIEWED_LINK_ID] = id
        }
    }

    companion object {
        private val KEY_LAST_VIEWED_LINK_ID = stringPreferencesKey("last_viewed_link_id")
    }
}
```

- `DataStore<Preferences>`를 생성자 주입받는다 — 인스턴스 생성은 `core/DataStoreFactory.kt`의 `createLinkItDataStore(producePath)`로만 하며, 플랫폼별 파일 경로는 각 플랫폼 그래프(`AndroidDataGraph`/`IosAppGraph`)가 주입한다
- **동일 파일에 DataStore 인스턴스가 2개 이상 생기면 런타임 예외**가 발생하므로, DataStore를 제공하는 `@Provides`에는 반드시 `@SingleIn(DataScope::class)`을 지정한다
- 저장 키(`stringPreferencesKey` 등)는 Impl의 `companion object`에 정의한다

### Metro 바인딩 등록

별도 그래프 파일 없이, Impl 클래스에 `@ContributesBinding(DataScope::class)`을 붙이면 상위 인터페이스로의 바인딩이 `DataScope`에 자동 기여된다.

```kotlin
@Inject
@ContributesBinding(DataScope::class)
class LinkRemoteDataSourceImpl(...) : LinkRemoteDataSource { ... }
```

단, **iOS는 멀티모듈 자동 수집이 동작하지 않으므로** `@ContributesBinding`이 붙은 Impl마다 `IosAppGraph`에 `@Binds`를 수동 등록해야 한다 — [docs/METRO_INSTRUCTION.md](../docs/METRO_INSTRUCTION.md) 참고.

## Repository 작성 규칙

Repository **인터페이스는 domain 모듈**(`domain/repository/`)에, **구현체는 data 모듈**(`repository/`)에 둔다.
인터페이스 작성 규칙(시그니처, suspend vs Flow)은 [domain/README.md](../domain/README.md#repository-인터페이스-작성-규칙)가 단일 출처이며, 이 섹션은 구현체 규칙만 다룬다.

```kotlin
// data 모듈: repository/LinkRepositoryImpl.kt
@Inject
@ContributesBinding(DataScope::class)
class LinkRepositoryImpl(
    private val linkRemoteDataSource: LinkRemoteDataSource,
) : LinkRepository {

    override suspend fun getLinks(): List<Link> {
        return linkRemoteDataSource.getLinks().map { it.toDomain() }
    }

    override suspend fun createLink(title: String, url: String, memo: String?): Link {
        return linkRemoteDataSource.createLink(title, url, memo).toDomain()
    }

    override suspend fun deleteLink(id: Long) {
        linkRemoteDataSource.deleteLink(id)
    }
}
```

- DataSource **인터페이스**만 주입받는다 (Impl 직접 참조 금지)
- 반환 전에 반드시 `toDomain()`으로 변환한다 — DTO/Entity가 domain 계층으로 새어 나가면 안 된다. 응답 바디가 없는 요청은 변환 없이 `Unit`으로 끝낸다
- 요청 데이터는 **개별 파라미터**로 받아 그대로 DataSource에 전달한다 — 인터페이스가 domain 모듈에 있으므로 시그니처에 DTO가 나타나면 안 된다
- **Remote/Local 조합과 캐싱 전략은 Repository만의 책임이다** — 예: 로컬 우선 조회 → 없으면 원격 조회 → 로컬 저장. DataSource에는 이런 조율 로직을 두지 않는다
- **캐싱 전략은 도메인별 자율로 결정한다** — 프로젝트 차원의 표준 전략(local-first 등)을 강제하지 않으며, 각 도메인의 데이터 특성에 맞는 전략을 해당 RepositoryImpl에서 선택한다. 선택한 전략은 RepositoryImpl 구현으로 드러나도록 작성한다 (예: `AuthRepositoryImpl` — 최초 확보한 Device ID를 DataStore에 영구 저장 후 재사용)

바인딩 등록은 위 `@ContributesBinding(DataScope::class)`으로 끝난다 — [Metro 바인딩 등록](#metro-바인딩-등록) 참고 (iOS는 `IosAppGraph` 수동 등록 필요).

## 새 API 추가 절차

가상의 `Link` 도메인 기준 전체 절차:

1. **DTO 작성** — `dto/link/LinkResponse.kt` (`@Serializable`), 파라미터 2개 이상인 요청이면 `dto/link/CreateLinkRequest.kt`도 함께 ([가시성 기준](#dto-가시성-기준) 준수)
2. **domain 모델 작성** — `domain` 모듈에 `Link` data class 추가
3. **Mapper 작성** — `mapper/LinkMapper.kt`에 `LinkResponse.toDomain()` 확장 함수
4. **Ktorfit API 작성** — `api/LinkApi.kt`에 엔드포인트 선언 (기존 API에 함수 추가도 가능)
5. **RemoteDataSource 작성** — `datasource/link/`에 `LinkRemoteDataSource` + `LinkRemoteDataSourceImpl`, Impl에 `@Inject` + `@ContributesBinding(DataScope::class)` 부착
6. **Repository 인터페이스 작성** — `domain` 모듈 `repository/LinkRepository.kt`
7. **RepositoryImpl 작성** — `repository/LinkRepositoryImpl.kt`, `@Inject` + `@ContributesBinding(DataScope::class)` 부착
8. **iOS 수동 등록** — `IosAppGraph`에 새로 만든 DataSource/Repository 바인딩을 `@Binds`로 수동 추가 (iOS는 멀티모듈 자동 수집이 동작하지 않음)

Android는 7단계까지로 주입 가능하지만, **iOS는 8단계까지 마쳐야 한다**. Metro 관련 에러(MissingBinding 등)가 발생하면 [docs/METRO_INSTRUCTION.md](../docs/METRO_INSTRUCTION.md)를 참고한다.

로컬 캐싱이 필요한 경우 아래 단계를 추가한다:

1. **저장 모델 결정** — DataStore Preferences면 원시값으로 충분. DB(Room 등) 도입 시 Entity 정의 + `mapper/`에 Entity ↔ Domain 변환 확장 함수 추가
2. **LocalDataSource 작성** — `datasource/link/`에 `LinkLocalDataSource` + Impl, `@ContributesBinding` 부착 (+ iOS 수동 등록)
3. **Repository에서 조율** — RepositoryImpl에 Remote/Local을 함께 주입받아 캐싱 전략 구현 (전략은 도메인별 자율)

## 금지 규칙 (안티패턴)

| 금지 사항 | 이유 |
| --- | --- |
| feature 모듈이 data 모듈에 직접 의존 | feature는 domain만 의존해야 함 — 의존 방향은 [docs/ARCHITECTURE.md](../docs/ARCHITECTURE.md#의존성-규칙)가 단일 출처 |
| DataSource가 domain 모델을 반환하거나 파라미터로 받음 | 변환 책임은 Repository에 있음. 파라미터는 원시값(복합 값은 datasource 패키지의 `XxxParam`)으로 받을 것 |
| domain Repository 인터페이스 시그니처에 Request DTO 사용 | DTO가 domain 계층으로 유출됨. 개별 파라미터로 받을 것 |
| DataSourceImpl 밖에서 Request DTO 조립 | Request DTO는 DataSourceImpl 내부에서만 생성하여 API 계층에 전달 |
| DataSource 네이밍에 `Remote`/`Local` 접미사 생략 | 출처가 하나뿐이어도 항상 명시. 나중에 출처 추가 시 rename 연쇄 발생 |
| DataSource가 다른 DataSource를 주입/참조 | 출처 조합·캐싱 전략은 Repository의 책임 |
| DataSource 인터페이스 시그니처에 `ApiResponse` 노출 | 래퍼 언래핑은 DataSourceImpl 내부의 책임. 인터페이스는 알맹이 DTO만 반환할 것 |
| Repository가 DTO를 반환/노출 | DTO가 domain·feature 계층으로 유출되어 서버 스키마 변경이 전파됨 |
| Mapper를 domain 모듈에 배치 | domain이 data(DTO)에 역의존하게 됨 |
| `DataGraph`·플랫폼 그래프 밖에서 Ktorfit/HttpClient/DataStore 직접 생성 | 설정이 분산되고 인스턴스가 중복 생성됨 |
| DataStore를 `@SingleIn(DataScope::class)` 없이 제공 | 동일 파일에 인스턴스가 2개 생기면 런타임 예외 발생 |
| API·DataSource에서 `Idempotency-Key`/`Authorization` 헤더 직접 첨부 | 공통 헤더 플러그인(`LinkTripHeaders`)이 자동 처리. 중복 첨부됨 |
| 개별 API/DataSource에서 에러 바디(`ErrorResponse`) 직접 파싱 | `HttpResponseValidator`가 `LinkTripApiException`으로 일괄 변환함 |
| DataSourceImpl에 비즈니스 로직 작성 | DataSource는 데이터 출처 호출만 담당 |
| suspend API(Ktor 등)를 관성적으로 `withContext(Dispatchers.IO)`로 래핑 | Ktor suspend API는 자체 메인 세이프. 블로킹 API를 suspend로 감쌀 때만 전환 — [domain/README.md 디스패처 규칙](../domain/README.md#디스패처-규칙--메인-세이프-계약) 참고 |

## 의존성 추가 가이드

- 네트워크·직렬화·로컬 저장(DataStore 등) 등 데이터 계층 전용 라이브러리는 `data/build.gradle.kts`에만 추가한다
- Android 전용 엔진/라이브러리는 `androidMain.dependencies`에, 공통은 `commonMain.dependencies`에 추가한다
- iOS 전용(Darwin 엔진 등)은 현재 `app-shared`에서 관리한다
