# 뉴스 백엔드 (과제 1 + 과제 2)

RSS 주기 수집 → 사용자 필터링 → 푸시 발송 → 이력 저장 + 프론트엔드용 REST API.

## 실행 방법

### 사전 요구 사항

- JDK 21+
- (Gradle은 Wrapper 포함, 별도 설치 불필요)

### 명령어

```bash
cd backend

./gradlew :app:bootRun          # 앱 실행 (RSS 스케줄러 자동 활성화)
./gradlew build                 # 전체 빌드
./gradlew test                  # 테스트 (앱 종료 상태에서 — SQLite 파일 락 회피)
./gradlew clean build           # 클린 빌드

# 특정 모듈만
./gradlew :core:test
./gradlew :push:test
```

### 접속

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- DB 파일: `backend/data/app.db` (SQLite)

> **주의**: `./gradlew test` 는 앱이 떠 있으면 SQLite 파일 락으로 실패합니다. bootRun 종료 후 실행하세요.

---

## 기술 스택

| 영역 | 선택 | 비고 |
|---|---|---|
| Language | Java 21 | record, switch expression 활용 |
| Framework | Spring Boot 4.0 | Web + Data JPA + Scheduling |
| DB | SQLite | 파일 기반, FK 비활성 |
| ORM | Spring Data JPA + Hibernate | `hibernate-community-dialects` |
| RSS Parser | Rome 2.1.0 | dc:creator, pubDate 지원 |
| Excel | Apache POI | 사용자 100명 적재 |
| API Doc | springdoc-openapi | Swagger UI + OpenAPI JSON |
| 의존성 주입 | Constructor Injection + Lombok | `@RequiredArgsConstructor` |
| 빌드 | Gradle 9 (멀티모듈) | core / rss / push / app |

---

## 모듈 구조

Gradle 멀티모듈. 의존 방향은 단방향.

```
core   ← 도메인 엔티티 + Repository. 공통 기반
rss    ← RSS 수집 (Rome). core 의존
push   ← 푸시 발송 + 사용자 필터링. core 의존
app    ← Spring Boot 엔트리. 모든 모듈 합쳐 실행
```

- `app` 만 `org.springframework.boot` 플러그인 적용. 나머지는 라이브러리 jar
- 모듈 간 순환 의존 금지

---

## 동작 흐름

### 1. 부팅 시퀀스

```
[1] schema.sql 실행                                  CREATE TABLE IF NOT EXISTS
[2] JPA EntityManagerFactory 초기화
[3] @Order(1) UserDataInitializer                    Excel → users + user_category + category
[4] @Order(2) RssInitialFetchRunner                  RSS 5개 카테고리 초기 수집
[5] RssScheduler (1분 후 시작, 10분 주기)             news.rss.scheduler.interval-ms=600000
```

### 2. RSS 수집 → 푸시 발송 (한 cycle)

```
RssCollectorService.collectAll()
  │
  ├─ for each RssCategory (정치/북한/경제/산업/사회)
  │     │
  │     ├─ collectOne(cat)                          [부분 실패 격리: try-catch]
  │     │     │
  │     │     ├─ RssParser.parse(feedUrl)          (connect 5s, read 10s)
  │     │     ├─ existing = findAllById(draftIds)   중복 제거
  │     │     │
  │     │     └─ for each new draft
  │     │           ├─ articleRepository.save(...)
  │     │           └─ pushDispatchService.dispatch(article, categoryName)
  │     │                 │
  │     │                 ├─ UserFilterService.findTargetsForArticle(article)
  │     │                 │     ├─ 카테고리 구독자 조회 (user_category)
  │     │                 │     └─ DndChecker.isInDnd(user) 필터링
  │     │                 │
  │     │                 └─ for each target user
  │     │                       ├─ sendAPNS / sendFCM (시뮬, "success" or "fail")
  │     │                       └─ push_log INSERT (성공/실패 모두 기록)
  │
  └─ cleanupOldArticles()                           1000건 초과 시 pub_date 오래된 순 삭제
```

### 3. REST API (과제 1)

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/categories` | 카테고리 목록 (5개) |
| GET | `/articles?category={이름}` | 카테고리별 기사 (최신순). `isRead` 포함 |
| POST | `/articles/{articleId}/read` | 기사 읽음 처리 (`article_read` INSERT) |

---

## 비즈니스 규칙

### RSS 수집
- **소스**: 5개 카테고리 RSS (`https://www.yna.co.kr/rss/{politics,northkorea,economy,industry,society}.xml`)
- **주기**: 10분
- **중복 방지**: `article_id` (RSS link의 `AKR...`) PK 자연키 → 재실행해도 신규만 INSERT
- **부분 실패 격리**: 카테고리 단위 try-catch — 1개 실패가 다른 카테고리에 영향 없음
- **타임아웃**: connect 5초, read 10초

### 기사 보존 정책
- **최대 1,000건** — 초과 시 `pub_date` 오래된 순으로 삭제
- 단일 DELETE 쿼리 (서브쿼리 + LIMIT, native SQL)
- 수집 cycle 끝마다 cleanup 호출

### DND (방해금지) 시간

`dnd_time` 값 해석:

| 값 | 의미 |
|---|---|
| `-` | 미설정 → 시간 무관 항상 발송 (DB는 dnd_start/dnd_end NULL) |
| `09:00-18:00` | 같은 날 09:00 ~ 18:00 발송 제외 |
| `23:00-11:00` | **자정 넘김** — 당일 23:00 ~ 다음날 11:00 발송 제외 |

**경계값 정책: 양쪽 포함** `[start, end]` — 시작/종료 시각 모두 차단.

판정 로직:
```
start <= end : 일반 구간   [start, end]
start >  end : 자정 넘김   [start, 24:00) ∪ [00:00, end]
```

### 푸시 발송 대상
사용자가 푸시를 받는 조건 (AND):
1. **카테고리 구독**: `user_category` 에 해당 article 의 category_id 행 존재
2. **DND 미해당**: 현재 시각이 DND 구간 밖

### 푸시 발송 시뮬레이션
- 과제 요구사항에 따라 **실제 APNS/FCM 호출 안 함**
- `PushNotificationService` 가 `"success"` / `"fail"` 무작위 반환
- 반환값을 `push_log.status` 에 그대로 저장 (성공/실패 모두 이력 남김)
- 푸시 타입 분기:
  - `user.pushType == "APNS"` → `sendAPNS(...)`
  - `user.pushType == "FCM"`  → `sendFCM(...)`
  - 기타 → status=`fail` 로 기록하고 skip

### push_log 비정규화
`title`, `category` 를 텍스트로 직접 저장. 이유: article 1000건 제한으로 삭제돼도 발송 이력은 보존되어야 함. **JOIN 없이 push_log 단독으로 조회 가능**.

---

## 데이터 모델

| 테이블 | 역할 | PK |
|---|---|---|
| `category` | 카테고리 (정치/북한/경제/산업/사회) | `id` (autoincrement) |
| `users` | 사용자 (Excel 100명) | `id` (Excel No 그대로) |
| `user_category` | 사용자-카테고리 다대다 매핑 | `(user_id, category_id)` |
| `article` | RSS 기사 (최대 1,000건) | `article_id` (자연키, RSS link 추출) |
| `article_read` | 기사 읽음 상태 | `article_id` |
| `push_log` | 푸시 발송 이력 | `id` (autoincrement) |

### 관계

- **User ↔ Category 다대다** — `user_category` 매핑 테이블 (복합 PK)
- **Article → Category** 다대일 (`article.category_id` FK 역할)
- **ArticleRead → Article** 일대일 (단일 사용자 가정으로 user_id 컬럼 없음)
- **PushLog → User + Article** 발송 1건당 1행. FK 없이 `device_id` / `article_id` 텍스트만 저장 (article 삭제돼도 이력 보존)

전체 DDL은 루트 [`README.md`](../README.md#ddl) 참고.

---

## 설정값 (application.properties)

```properties
# RSS 스케줄러
news.rss.scheduler.enabled=true              # 빈 등록 여부
news.rss.scheduler.interval-ms=600000         # 수집 주기 (10분)
news.rss.scheduler.initial-delay-ms=60000     # 부팅 후 첫 실행 지연 (1분)

# CORS
news.cors.allowed-origins=http://localhost:3000

# JPA / SQLite — 부팅 순서 보장 3종 세트
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
```

---

## 핵심 설계 결정

### Spring Boot 4 부팅 순서
`schema.sql` → JPA 초기화 → ApplicationRunner 순서를 보장하려면 위 3개 properties 필수. 하나라도 빠지면 `no such table` 에러.

### `@Order` 로 Runner 순서 보장
- `UserDataInitializer` (@Order 1): Excel → users + category 적재
- `RssInitialFetchRunner` (@Order 2): RSS 수집 시 `categoryRepository.findByName("정치")` 가 카테고리 적재를 선행해야 함

### 스케줄러 활성화는 Property 로 제어
Spring Boot 4 부터 `@Scheduled` 는 `@EnableScheduling` 없어도 자동 활성화. 테스트/운영 분리를 위해 `@ConditionalOnProperty` 로 명시 제어.

### bootRun 의 working directory 고정
`backend/app/build.gradle` 에 `bootRun.workingDir = rootProject.projectDir` 설정. 어디서 실행하든 `backend/` 가 cwd → DB 경로 (`./data/app.db`) 일관.

### 부분 실패 격리 패턴
외부 의존 작업 (RSS 수집) 은 카테고리 단위 try-catch — 1개 실패가 다른 카테고리에 영향 없음. 푸시 발송도 동일하게 article 단위로 격리.

### SQLite 특수 사항
- 모든 날짜는 TEXT 로 저장 (ISO 8601, `yyyy-MM-dd'T'HH:mm:ss`)
- FK 제약 기본 비활성 — 앱 레이어에서 무결성 보장
- `hibernate-community-dialects` 필수
- 동일 DB 파일 동시 접근 불가 → bootRun 실행 중 테스트 X

---

## 디렉터리

```
backend/
├── core/               엔티티, Repository
│   └── src/main/java/com/example/news/core/
│       ├── domain/                 @Entity (Article, User, Category, ArticleRead, PushHistory ...)
│       └── repository/             JpaRepository
│
├── rss/                RSS 수집
│   └── src/main/java/com/example/news/rss/
│       ├── domain/                 RssCategory enum (5개 피드 URL)
│       ├── parser/                 RssParser (Rome), ArticleDraft record
│       ├── service/                RssCollectorService
│       ├── scheduler/              RssScheduler (@ConditionalOnProperty)
│       └── init/                   RssInitialFetchRunner
│
├── push/               푸시 발송 + 필터링
│   └── src/main/java/com/example/news/push/
│       ├── filter/                 UserFilterService, DndChecker
│       └── service/                PushDispatchService, PushNotificationServiceImpl
│
└── app/                Spring Boot 엔트리
    └── src/main/
        ├── java/com/example/news/
        │   ├── NewsApplication.java
        │   ├── config/             WebConfig (CORS), OpenApiConfig, ClockConfig
        │   ├── controller/         ArticleController, CategoryController
        │   ├── dto/                ArticleResponse, CategoryResponse
        │   └── init/               UserDataInitializer (Excel → DB)
        └── resources/
            ├── schema.sql
            ├── application.properties
            └── 데이터.xlsx          사용자 100명 초기 데이터
```
