# 온보딩/최초진입 스펙

> 기준 Figma: [Pingo v3.0.3](https://www.figma.com/design/Pym5oUSWQjyWVtN86oj6lb/Pingo--v3.0.3?node-id=13008-1945&m=dev)  
> 기준 영역: `인트로 / 최초진입`  
> 기준 section: `15091:142439`  
> 스펙 버전: `v0.1.2`  
> 작성일: 2026-06-14

## 1. 한눈에 보기

온보딩/최초진입 화면군은 사용자가 앱의 핵심 사용법을 처음 경험하도록 만드는 흐름이다. Figma에서는 지도 기반 메인 화면에서 `일정 생성` 버튼을 선택하고, `영상 링크로 만들기`를 통해 여행 일정을 생성한 뒤, 분석 완료 화면과 일정 상세/저장 흐름까지 이어지는 예시 상태를 보여준다.

핵심 UX는 설명만 보여주는 온보딩이 아니라, 사용자가 실제 주요 기능을 따라 눌러보는 튜토리얼에 가깝다.

| 큰 덩어리 | 역할 |
|---|---|
| 인트로 애니메이션 | 최초 진입 시 지도/위치 기반 앱임을 인지시키는 진입 연출 |
| 메인 화면 안내 | 일정 생성 버튼과 생성 방식 선택을 안내 |
| 영상 링크 생성 | 추천 영상 확인, 링크 복사, 클립보드 토스트, 링크 입력, 유효성 검증 |
| 분석 중 | 일정 생성 중 상태와 완료 알림 |
| 분석 완료 | 생성된 일정 저장 진입점과 저장 방식 제공 |
| 여행 상세 미리보기 | 영상 요약, 여행 일정, 장소 카드, 저장/삭제 흐름 확인 |

## 2. 화면 상태별 읽기

### 2.1 인트로 애니메이션

![인트로 애니메이션](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/intro-animation.png)

Figma 메모에는 최초 진입 시 지구가 줌인되며 내 위치로 확장되는 연출이 제안되어 있다. 위치 권한이 허용되지 않으면 서울/한국이 보이는 지도를 기본값으로 두는 방안이 메모되어 있다.

### 2.2 메인 화면 조작 안내

![온보딩 첫 안내](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/onboarding-step-01.png)

사용자에게 `일정 생성 버튼을 선택해보세요`, `영상 링크로 만들기`를 선택해보세요 같은 단계 안내를 보여준다. 이 흐름은 단순 읽기형 온보딩보다 앱 핵심 기능을 직접 눌러보게 하는 방식이다.

온보딩 flow 기준으로 이 단계에서는 `영상 링크로 만들기`만 실제 진행 대상으로 보인다. `보관함에서 가져오기`, `직접 만들기`는 생성 방식으로 노출되지만 온보딩 중 선택 가능 여부가 제한되거나 별도 정책이 필요하다.

![온보딩 메인 안내](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/onboarding-main-guide.png)

메인 화면에 저장 일정이 없는 상태, `1개의 일정 생성중...` 상태, `일정 생성이 완료되었어요` 토스트와 `확인하기` 액션이 함께 설계되어 있다.

### 2.3 영상 링크로 일정 생성

![영상 링크 생성](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/video-link-create.png)

`유튜브 영상 링크를 넣고 여행 일정을 만들어보세요` 문구와 `복사한 링크 붙여넣기`, 링크 입력란, 유효하지 않은 링크 토스트가 확인된다. Figma와 sitemap에는 추천 영상 확인, `링크복사`, 클립보드 토스트, 붙여넣기 순서가 함께 나타난다.

![유효하지 않은 링크 상태](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/onboarding-permission-or-processing.png)

붙여넣기 후 유효하지 않은 링크를 제출하면 `유효한 영상 링크가 아닙니다` 토스트가 표시된다.

![일정 생성 중](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/analysis-loading.png)

유효한 링크로 생성을 시작하면 `일정 생성중...`, `일정을 생성중이에요~`, `완료되면 알려드릴게요~` 문구와 진행 바가 표시된다. 재설치 사용자, 인트로 건너뛰기, 긴 인트로로 인한 이탈 가능성은 질문형 메모로 남아 있어 정책 확인이 필요하다.

### 2.4 분석 완료와 일정 저장

![분석 완료](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/analysis-complete.png)

`축하해요 일정 분석이 완료됐어요!`, `전체일정 저장하기`, `선택항목 저장하기`가 확인된다. sitemap 기준으로 전체 저장은 저장 폴더 설정으로 이어지고, 선택 저장은 항목 선택/전체선택/취소를 포함한 선택항목 저장 흐름으로 이어진다.

### 2.5 여행 상세 미리보기

![여행 상세 미리보기](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/trip-detail-preview.png)

![여행 상세 일정 리스트](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/trip-detail-state-02.png)

![여행 상세 영상 요약](/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/trip-detail-state-03.png)

여행 상세는 `여행 일정`과 `영상 요약` 탭을 제공한다. Figma의 `온보딩_여행 상세보기 01/02/03`은 아래 상태로 나뉜다.

| 상태 | 의미 | 주요 구성 |
|---|---|---|
| `01` | 상세 진입 직후 | 지도 영역, 경로 폴리곤, 영상 썸네일 마커, 바텀시트의 `여행 일정` 탭 시작점 |
| `02` | 여행 일정 리스트 | `3박4일 일정`, 일차 칩, 장소 카드, 이동수단/소요시간, `자세히 보기` |
| `03` | 영상 요약 탭 | 영상 썸네일/재생 버튼, 제목/크리에이터/조회수, AI 요약정보, 관련 태그, 여행 정보, 영상 타임라인 |

## 3. 핵심 UX 규칙

| 규칙 | 내용 |
|---|---|
| 최초 진입 연출 | 지구/지도 줌인 연출이 제안되어 있으나 정확한 애니메이션 정책은 미정이다. |
| 위치 권한 fallback | 권한 미허용 시 서울/한국 기준 지도가 제안되어 있으나 좌표/줌 레벨은 미정이다. |
| 직접 체험형 온보딩 | 일정 생성 버튼, 영상 링크 생성 버튼 등 실제 조작을 안내한다. |
| 온보딩 중 생성 방식 제한 | `영상 링크로 만들기`가 핵심 진행 경로이며, 다른 생성 방식의 선택 가능 여부는 정책 확인이 필요하다. |
| 추천 영상 링크 복사 | 추천 영상에서 링크를 복사한 뒤 클립보드 토스트/붙여넣기로 입력란을 채운다. |
| 링크 입력 검증 | 유효하지 않은 영상 링크는 토스트로 안내한다. |
| 분석 중 상태 | 일정 생성 중 문구와 완료 알림을 제공한다. 최종 노출 순서는 확인이 필요하다. |
| 분석 완료 후 저장 | 전체 일정 저장과 선택 항목 저장을 제공한다. |
| 저장 Flow | 전체 저장은 저장 폴더 설정, 선택 저장은 선택항목 저장 화면으로 이어진다. |
| 여행 상세 탭 | 생성된 일정은 여행 일정/영상 요약 탭으로 확인한다. |

## 4. 사용자 흐름

| 흐름 | 사용자가 하는 일 | 결과 |
|---|---|---|
| 최초 진입 | 앱 최초 실행 | 인트로 애니메이션 또는 메인 안내 진입 |
| 일정 생성 안내 | `일정 생성` 버튼 선택 | 생성 방식 버튼 노출 |
| 영상 링크 생성 | `영상 링크로 만들기` 선택 | 링크 입력 화면으로 이동 |
| 추천 영상 링크 복사 | 추천 영상의 `링크복사` 선택 | 클립보드 토스트 또는 붙여넣기 가능 상태 표시 |
| 링크 붙여넣기 | 클립보드 링크 붙여넣기 | 영상 링크 입력란에 값 입력 |
| 유효하지 않은 링크 제출 | 잘못된 링크로 생성 시도 | `유효한 영상 링크가 아닙니다` 토스트 |
| 일정 분석 중 | 유효한 링크로 일정 생성 | `일정 생성중...` 계열 로딩/대기 상태 표시 |
| 일정 분석 완료 | 분석 완료 | 전체 저장/선택 저장 액션 제공 |
| 전체 일정 저장 | `전체일정 저장하기` 선택 | 저장 폴더 설정 흐름으로 이동 |
| 선택 항목 저장 | `선택항목 저장하기` 선택 | 선택항목 저장 화면으로 이동. 전체선택/취소 제공 |
| 상세 확인 | 생성된 일정 확인 | 여행 일정/영상 요약/장소 정보 확인 |

## 5. 상세 기능 명세

| Feature ID | 기능 | Trigger | 화면 반응 | 확정 수준 | 관련 이미지 |
|---|---|---|---|---|---|
| `INTRO_MAP_ZOOM` | 최초 진입 지도 줌인 | 앱 최초 실행 | 지구가 줌인되며 내 위치로 확장되는 연출 제안 | 메모 기반/정책 필요 | IMG-01 |
| `INTRO_SKIP_POLICY` | 인트로 건너뛰기 | 재설치 또는 사용자 선택 | 건너뛰기 필요 여부가 질문형 메모로 남음 | 정책 필요 | - |
| `GUIDE_CREATE_BUTTON` | 일정 생성 버튼 안내 | 온보딩 단계 진입 | `일정 생성 버튼을 선택해보세요` 안내 | 확정 | IMG-02 |
| `GUIDE_VIDEO_LINK` | 영상 링크 생성 안내 | 생성 방식 노출 | `영상 링크로 만들기` 선택 유도. 다른 생성 방식은 온보딩 진행 대상이 아니며 선택 가능 여부 확인 필요 | 확정/정책 필요 | IMG-02 |
| `RECOMMENDED_VIDEO_SHOW` | 추천 영상 노출 | 영상 링크 생성 화면 진입 | 추천 영상 카드와 조회수/링크복사 정보 표시 | 확정 | IMG-04 |
| `COPY_VIDEO_LINK` | 링크 복사 | 추천 영상의 `링크복사` 선택 | 클립보드에 링크 저장, 토스트/붙여넣기 가능 상태로 전환 | 확정 | IMG-04, IMG-05 |
| `CLIPBOARD_TOAST` | 클립보드 토스트 | 링크 복사 후 또는 클립보드 링크 감지 | 복사한 링크를 입력란에 넣을 수 있는 안내 표시 | 확정 | IMG-05 |
| `VIDEO_LINK_INPUT` | 영상 링크 입력 | 링크 입력/붙여넣기 | 입력란에 링크 값 표시 | 확정 | IMG-04 |
| `VIDEO_LINK_INVALID` | 링크 오류 안내 | 유효하지 않은 링크 제출 | `유효한 영상 링크가 아닙니다` 토스트 노출 | 확정 | IMG-04 |
| `ANALYSIS_LOADING` | 일정 생성 중 | 유효한 링크로 생성 시작 | `일정 생성중...`, `일정을 생성중이에요~`, 완료 알림 대기 문구와 진행 바 표시 | 확정 | IMG-06 |
| `ANALYSIS_COMPLETE` | 분석 완료 | 일정 분석 완료 | 완료 문구와 저장 버튼 노출 | 확정 | IMG-07 |
| `SAVE_ALL` | 전체 일정 저장 | `전체일정 저장하기` 선택 | 저장 폴더 설정 페이지로 이동한 뒤 생성된 전체 일정 저장 | sitemap 기반 | IMG-07 |
| `SAVE_SELECTED` | 선택 항목 저장 | `선택항목 저장하기` 선택 | 선택항목 저장 화면 진입. `전체선택`, `취소`, 선택 개수 표시 제공 | sitemap 기반 | IMG-07 |
| `SAVE_DELETE_CONFIRM` | 저장 취소/삭제 확인 | 생성된 일정 삭제 또는 저장 취소 계열 액션 | `이 일정을 정말 삭제하시겠어요?` 팝업 표시 후보. 취소/삭제 확정 액션과 삭제 후 이동 정책 확인 필요 | sitemap 기반/정책 필요 | - |
| `TRIP_DETAIL_ENTRY` | 상세 진입 직후 | 생성 일정 확인 | 지도, 경로 폴리곤, 영상 마커, 바텀시트 시작점 표시 | 확정 | IMG-08 |
| `TRIP_DETAIL_ITINERARY` | 여행 일정 리스트 | 상세 화면 스크롤 | 일차 칩과 장소 카드, 이동수단/소요시간 표시 | 확정 | IMG-09 |
| `TRIP_DETAIL_SUMMARY` | 영상 요약 탭 | `영상 요약` 탭 선택 | 영상 요약, 관련 태그, 여행 정보, 영상 타임라인 표시 | 확정 | IMG-10 |

## 6. 이미지 참조 매핑

| Image ID | 파일 | 설명 |
|---|---|---|
| IMG-01 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/intro-animation.png` | 인트로 애니메이션 |
| IMG-02 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/onboarding-step-01.png` | 일정 생성 버튼 안내 |
| IMG-03 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/onboarding-main-guide.png` | 메인 화면 안내 |
| IMG-04 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/video-link-create.png` | 영상 링크 생성 |
| IMG-05 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/onboarding-permission-or-processing.png` | 유효하지 않은 링크 토스트 |
| IMG-06 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/analysis-loading.png` | 일정 생성 중 |
| IMG-07 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/analysis-complete.png` | 분석 완료 |
| IMG-08 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/trip-detail-preview.png` | 여행 상세 01: 진입 직후 |
| IMG-09 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/trip-detail-state-02.png` | 여행 상세 02: 일정 리스트 |
| IMG-10 | `/Users/yuhohyeon/Desktop/project/LinkIt-KMP/docs/specs/assets/onboarding/trip-detail-state-03.png` | 여행 상세 03: 영상 요약 |

## 7. 미정/정책 필요

| TBD ID | 항목 | 내용 |
|---|---|---|
| `TBD-01` | 인트로 건너뛰기 | 재설치 사용자 또는 실수로 건너뛰기 선택 시 정책이 필요하다. |
| `TBD-02` | 인트로 길이 | 인트로 과정이 길 경우 이탈 가능성이 메모되어 있어 실제 사용성 검토가 필요하다. |
| `TBD-03` | 위치 권한 fallback | 권한 미허용 시 서울/한국 기준 위치의 좌표와 줌 레벨을 확정해야 한다. |
| `TBD-04` | 분석 처리 순서 | 분석 중 화면과 완료 토스트 자체는 확인되지만 최종 노출 순서와 자동 전환 조건을 확정해야 한다. |
| `TBD-05` | 온보딩 중 생성 방식 제한 | `보관함에서 가져오기`, `직접 만들기`가 온보딩 중 비활성/선택 불가인지 최종 정책 확인 필요 |
| `TBD-06` | 저장 폴더 설정 | 전체 일정 저장 시 저장 폴더 선택/생성 정책과 기본 폴더를 확정해야 한다. |
| `TBD-07` | 삭제 팝업 상세 정책 | 삭제 팝업의 정확한 진입 조건, 취소/삭제 후 이동 위치, 삭제 대상 범위 확인 필요 |

## 8. 변경 이력

| Version | Date | Author | 변경 사항 | 근거 |
|---|---|---|---|---|
| `v0.1.0` | 2026-06-14 | Codex | 온보딩/최초진입 스펙 최초 작성 | Figma metadata / 화면 캡처 / sitemap |
| `v0.1.1` | 2026-06-14 | Codex | 서브에이전트 리뷰 반영. 추천 영상 링크 복사/클립보드, 분석 중 상태, 저장 폴더/선택항목 저장 흐름, 온보딩 중 생성 방식 제한을 보강 | Spec Document Reviewer Agent 1차 리뷰 |
| `v0.1.2` | 2026-06-14 | Codex | 재리뷰 반영. 링크 오류/일정 생성 중 상태를 분리하고 여행 상세 01/02/03 상태별 이미지를 추가 | Spec Document Reviewer Agent 2차 리뷰 |
