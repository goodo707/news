# Backend

Spring Boot 멀티모듈 — REST API(과제 1) + 푸시 알림 시스템(과제 2).

---

## 프로젝트 구조

```
backend/
├── core/                도메인 엔티티 + Repository
│   ├── domain/          @Entity (Article, User, Category, ArticleRead, PushLog ...)
│   ├── repository/      JpaRepository
│   └── util/            TimeFormats 등 공통 유틸
│
├── rss/                 RSS 수집 (Rome 라이브러리). core 의존
│   ├── domain/          RssCategory enum (5개 피드 URL)
│   ├── parser/          RssParser, ArticleDraft
│   ├── service/         RssCollectorService
│   └── scheduler/       RssScheduler — 단일 진입점 (5초 후 첫 수집, 10분 주기)
│
├── push/                사용자 필터링 + 푸시 발송 + 이력 저장. core 의존
│   ├── filter/          ② 사용자 필터링 — UserFilterService, DndChecker
│   ├── notification/    ③ 푸시 발송 — PushNotificationService(Impl)
│   ├── log/             ④ 발송 이력 저장 — PushLogRecorder
│   └── dispatcher/      오케스트레이터 — PushDispatchService
│
└── app/                 Spring Boot 엔트리. 모든 모듈을 합쳐 실행
    └── src/main/
        ├── java/com/example/news/
        │   ├── NewsApplication.java
        │   ├── config/       WebConfig (CORS), OpenApiConfig, ClockConfig
        │   ├── controller/   ArticleController, CategoryController
        │   ├── dto/          ArticleResponse, CategoryResponse
        │   └── init/         UserDataInitializer (Excel → DB)
        └── resources/
            ├── schema.sql
            ├── application.properties
            └── 데이터.xlsx    사용자 100명 초기 데이터
```

- 의존 방향은 단방향: `core ← rss / push ← app`
- `app` 만 `org.springframework.boot` 플러그인 적용

---

## 기술 스택

| 영역       | 선택                                               |
| ---------- | -------------------------------------------------- |
| Language   | Java 21                                            |
| Framework  | Spring Boot 4.0 — Web + Data JPA + Scheduling      |
| DB         | SQLite (`hibernate-community-dialects` 사용)       |
| ORM        | Spring Data JPA + Hibernate                        |
| RSS Parser | Rome 2.1.0                                         |
| Excel      | Apache POI                                         |
| API Doc    | springdoc-openapi (Swagger UI + OpenAPI JSON)      |
| Validation | jakarta.validation (`@NotNull` → OpenAPI required) |
| Lombok     | `@Slf4j`, `@RequiredArgsConstructor`, `@Getter`    |
| 빌드       | Gradle 9 멀티모듈 (core / rss / push / app)        |
| 테스트     | JUnit 5 + Mockito + AssertJ                        |

---

## 실행 방법

### 사전 요구 사항

- JDK 21+
- (Gradle 은 Wrapper 포함, 별도 설치 불필요)

### 명령어

```bash
cd backend

./gradlew :app:bootRun          # 앱 실행 (RSS 스케줄러 자동 활성)
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

> ⚠️ `./gradlew test` 는 앱이 떠 있으면 SQLite 파일 락으로 실패합니다. bootRun 종료 후 실행하세요.
