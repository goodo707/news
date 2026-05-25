# 뉴스 열람 웹 + 푸시 알림 시스템

사전 과제 1(뉴스 기사 열람 웹) + 과제 2(푸시 알림 백엔드)를 한 프로젝트로 통합 구현. RSS 수집 + `article` 데이터를 두 과제가 공유.

- **프론트엔드**: Next.js — 카테고리별 기사 열람 + 읽음 처리
- **백엔드**: Spring Boot — 10분 주기 RSS 수집 + 100명 사용자에게 카테고리 / 방해금지(DND) 매칭 기반 푸시(시뮬) 발송

---

## AI 도구 활용

| 항목      | 내용                                                                                                 |
| --------- | ---------------------------------------------------------------------------------------------------- |
| 도구      | Claude Code (Anthropic)                                                                              |
| 활용 스킬 | **`superpowers:brainstorming`** (설계 토의) · `systematic-debugging` (이슈 분석)                     |
| 목적      | 설계 토의 · 코드 리뷰 · 리팩토링 · 트레이드오프 분석                                                 |
| 방식      | 페어 프로그래밍 — 모든 설계/구현 의사결정은 응시자 책임. AI 는 제안 / 검토 / 비교 / 문서화 보조 역할 |

---

## 빠른 시작

### 사전 요구사항: JDK 21+, Node.js 20+, pnpm 9+

```bash
# 1. 백엔드 (터미널 1)
cd backend && ./gradlew :app:bootRun

# 2. 프론트엔드 (터미널 2)
cd frontend
pnpm install
pnpm gen:types      # 백엔드 Swagger → TypeScript 타입 자동 생성
pnpm dev
```

| 항목           | URL / 경로                              |
| -------------- | --------------------------------------- |
| 프론트엔드     | <http://localhost:3000>                 |
| 백엔드 Swagger | <http://localhost:8080/swagger-ui.html> |
| DB 파일        | `backend/data/app.db`                   |

### 테스트

```bash
cd backend  && ./gradlew test    # 백엔드 단위 테스트 (앱 종료 상태에서)
cd frontend && pnpm e2e          # 프론트 E2E (Playwright)
```

---

## 스크린샷

### 홈 — 5개 카테고리 카드

![홈](screenshots/home.png)

### 카테고리 기사 리스트 (읽음 / 미읽음 구분)

![카테고리 페이지](screenshots/category.png)

### 기사 클릭 — 새 탭으로 외부 원문 오픈

![기사 새 탭](screenshots/article.png)

---

## 프로젝트 구조

```
news/
├── backend/
│   ├── core/     도메인 엔티티 + Repository
│   ├── rss/      RSS 수집 (Rome)
│   ├── push/     filter / notification / log / dispatcher
│   └── app/      Spring Boot 엔트리, REST API, 초기화 (Excel 적재)
│
├── frontend/
│   ├── app/          App Router 페이지 + layout / error / loading / not-found
│   ├── components/   ui (shadcn) / layout / home / category
│   ├── lib/          api / types / hooks / actions / constants
│   └── e2e/          Playwright 테스트
│
├── screenshots/  제출용 스크린샷
└── README.md
```

## 기술 스택

| 영역           | 선택                                                                                                     |
| -------------- | -------------------------------------------------------------------------------------------------------- |
| **백엔드**     | Java 21 · Spring Boot 4 · SQLite · Spring Data JPA · Rome (RSS) · Apache POI (Excel) · springdoc-openapi |
| **프론트엔드** | Next.js 16 · TypeScript · React 19 · Tailwind CSS v4 · shadcn/ui · openapi-fetch + openapi-typescript    |
| **테스트**     | JUnit 5 · Mockito · AssertJ · Playwright                                                                 |
| **빌드**       | Gradle 9 (멀티모듈) · pnpm                                                                               |

---

## 과제 1 — 프론트엔드

| 경로               | 설명                                                   |
| ------------------ | ------------------------------------------------------ |
| `/`                | 홈. 5개 카테고리 카드 + 각 카드 최신 기사 3건 미리보기 |
| `/category/{이름}` | 카테고리별 기사 목록. Featured(최신 1건) + 2열 그리드  |

기사 클릭 → **새 탭으로 원문** + 백엔드 읽음 기록(optimistic UI). 새로고침 후에도 읽음 유지.

### 핵심 포인트

1. **ISR + SSG** — 모든 페이지 `revalidate=600` + `generateStaticParams` 로 빌드 시점 정적 prerender. 백엔드 RSS 주기와 정합한 3-layer 캐시 일관성. SEO 친화적.

2. **타입 안전 API** — 백엔드 DTO 의 `@NotNull` → OpenAPI `required` → `pnpm gen:types` 로 TS 타입 자동 생성. `openapi-fetch` 가 컴파일 타임에 URL/파라미터 오류 검출.

3. **WCAG 2.1 / KWCAG 충족** — Skip link, 시멘틱 HTML, ARIA(`aria-label`/`aria-current`), `focus-visible` ring, 색 대비 ≈ 11:1 (AAA), 스크린리더 친화 `sr-only` 텍스트.

4. **읽음 처리** — 클릭 즉시 `useState` optimistic UI + Server Action(`revalidateTag`)로 카테고리별 cache 정밀 무효화. 새로고침 후에도 isRead 유지.

---

## 과제 2 — 푸시 알림 백엔드

### 동작 흐름

```mermaid
flowchart TD
    Start([RssScheduler<br/>fixedDelay 10분 · 단일 진입점]):::trigger --> Collect[RssCollectorService.collectAll]
    Collect --> CatLoop{{카테고리 5개 루프<br/>정치 · 북한 · 경제 · 산업 · 사회<br/>try-catch 부분 실패 격리}}

    CatLoop -->|각 카테고리| Parse["① RSS 파싱<br/>RssParser.parse<br/>connect 5s · read 10s"]:::m1
    Parse --> Dedup["중복 제거<br/>findAllById 배치 lookup"]
    Dedup --> NewLoop{{신규 draft 루프}}

    NewLoop -->|신규만| Save[Article 저장<br/>JPA batch_size 50]
    Save --> Dispatch[PushDispatchService.dispatch<br/>오케스트레이터]
    Dispatch --> Filter["② 사용자 필터링<br/>UserFilterService<br/>카테고리 구독 ∩ DND"]:::m2
    Filter --> TgtLoop{{대상 사용자 루프}}
    TgtLoop -->|대상자마다| Send["③ 푸시 발송 시뮬<br/>sendAPNS / sendFCM"]:::m3
    Send --> Log["④ 이력 저장<br/>PushLogRecorder.record<br/>success/fail 모두 기록"]:::m4

    CatLoop -.cycle 끝.-> Cleanup[cleanupOldArticles<br/>1,000건 초과 시 오래된 순 삭제]

    classDef trigger fill:#fff7ed,stroke:#fb923c
    classDef m1 fill:#eff6ff,stroke:#3b82f6
    classDef m2 fill:#ecfdf5,stroke:#10b981
    classDef m3 fill:#fef3c7,stroke:#f59e0b
    classDef m4 fill:#fce7f3,stroke:#ec4899
```

**단계별 요약** — 발표 시 다이어그램 위에서 아래로 읽기:

| # | 단계 | 위치 / 모듈 | 핵심 동작 |
| --- | --- | --- | --- |
| 0 | 트리거 | `RssScheduler` | 10분 `fixedDelay` 로 cycle 시작 (단일 진입점) |
| 1 | RSS 파싱 | `rss/parser/RssParser` | 5개 피드 HTTP fetch (connect 5s / read 10s) |
| 2 | 중복 제거 | `RssCollectorService` | `findAllById(draftIds)` 1회 조회 + Set 매칭 (N+1 회피) |
| 3 | Article 저장 | `core/ArticleRepository` | JPA `batch_size=50` 로 묶어서 INSERT |
| 4 | 푸시 dispatch | `push/dispatcher/PushDispatchService` | 신규 article 마다 ②③④ 호출 흐름 제어 |
| 5 | 사용자 필터 | `push/filter/UserFilterService` + `DndChecker` | 카테고리 구독자 ∩ DND 미해당 |
| 6 | 푸시 발송 | `push/notification/PushNotificationService` | APNS/FCM 시뮬 — `success`/`fail` 무작위 반환 |
| 7 | 이력 저장 | `push/log/PushLogRecorder` | 결과를 `push_log` 에 비정규화 INSERT |
| 8 | cleanup | `RssCollectorService` | 카테고리 cycle 종료 후 1,000건 초과 분 삭제 |

### 4 핵심 기능 모듈 (요구사항 §4)

| #   | 모듈           | 위치                 | 책임                                        |
| --- | -------------- | -------------------- | ------------------------------------------- |
| ①   | RSS 수집       | Gradle `rss` 모듈    | 10분 주기 수집, 중복 제거, 1000건 cleanup   |
| ②   | 사용자 필터링  | `push/filter/`       | 카테고리 구독 + DND 시간대 필터링           |
| ③   | 푸시 발송      | `push/notification/` | 과제 제공 인터페이스 + 시뮬 구현체 (수정 X) |
| ④   | 발송 이력 저장 | `push/log/`          | 비정규화된 `push_log` 저장                  |
| ─   | 오케스트레이터 | `push/dispatcher/`   | ②③④ 호출 흐름 제어                          |

### DND (방해금지) 시간 룰

| `dnd_time` 값 | 의미                                           |
| ------------- | ---------------------------------------------- |
| `-`           | 미설정 → 항상 발송                             |
| `09:00-18:00` | 같은 날 09:00 ~ 18:00 발송 제외                |
| `23:00-11:00` | **자정 넘김** — 당일 23:00 ~ 다음날 11:00 제외 |

경계값 정책: **양쪽 포함** `[start, end]` — 시작/종료 시각 모두 차단. `start > end` 면 자정 넘김 구간으로 해석.

### 데이터 모델

| 테이블          | 역할                                | PK                                                |
| --------------- | ----------------------------------- | ------------------------------------------------- |
| `category`      | 카테고리 (정치/북한/경제/산업/사회) | `id` (autoincrement)                              |
| `users`         | 사용자 100명 (Excel 적재)           | `id` (Excel No 그대로)                            |
| `user_category` | 사용자-카테고리 다대다 매핑         | `(user_id, category_id)`                          |
| `article`       | RSS 기사 (최대 1,000건)             | `article_id` (자연키 — RSS link 의 `AKR...` 부분) |
| `article_read`  | 기사 읽음 상태 (단일 사용자 가정)   | `article_id`                                      |
| `push_log`      | 푸시 발송 이력 (FK 없이 비정규화)   | `id` (autoincrement)                              |

전체 DDL / 인덱스 → [`backend/app/src/main/resources/schema.sql`](backend/app/src/main/resources/schema.sql)

### Spring Boot 4 부팅 — `schema.sql` 3종 세트

```properties
spring.jpa.hibernate.ddl-auto=none           # Hibernate 자동 DDL 끔
spring.sql.init.mode=always                  # schema.sql 강제 실행
spring.jpa.defer-datasource-initialization=true   # JPA 가 schema.sql 실행을 책임지도록 활성화
```

JPA + schema.sql 조합에서 부팅 순서 보장. 하나라도 빠지면 `no such table` 에러.

---

## 성능 / 캐시 — 의사결정과 트레이드오프

각 항목 모두 **"고려한 옵션 → 트레이드오프 → 최종 선택과 근거"** 흐름. 발표용 핵심 어필 섹션.

### 1. RSS 신규 기사 중복 체크 — N+1 회피

매 cycle 마다 30개 draft 가 article 테이블에 이미 있는지 확인 필요.

| 옵션                                           | 쿼리 수   | 트레이드오프                                                               |
| ---------------------------------------------- | --------- | -------------------------------------------------------------------------- |
| (A) `existsById` 루프 N회                      | N         | 가장 단순. 카테고리당 30회 × 5 = 150 쿼리/cycle                            |
| **(B) `findAllById(draftIds)` 1회 + Set 매칭** | **1**     | **쿼리 1회, in-memory 비교**. IN 절 한계는 30개 규모에선 무의미            |
| (C) DB UPSERT (`INSERT OR IGNORE`)             | 1 (write) | 가장 적은 라운드트립. 단 **신규/기존 구분이 사라져 푸시 트리거 분기 불가** |

→ **B 선택.** 푸시는 "신규 기사" 만 대상이라 INSERT 결과 구분이 필수. C 의 UPSERT 는 이 분기를 잃음. (`8929f6b`)

### 2. article 1,000건 초과 cleanup — 엔티티 로드 회피

| 옵션                                                            | 쿼리                | 트레이드오프                                                                         |
| --------------------------------------------------------------- | ------------------- | ------------------------------------------------------------------------------------ |
| (A) `findAllByOrderByPubDateAsc(Pageable)` + `deleteAllInBatch` | SELECT 1 + DELETE 1 | 삭제 대상 엔티티를 PersistenceContext 에 일단 로드                                   |
| (B) native `DELETE … WHERE article_id IN (SELECT … LIMIT n)`    | DELETE 1            | 엔티티 로드 회피. 단 **DB 종속(`LIMIT` 문법)** + CLAUDE.md JPA 우선순위 위배         |
| **(C) 다시 A 패턴 — derived + Pageable**                        | SELECT 1 + DELETE 1 | A 와 동일. cleanup 빈도(10분 1회) × 규모(< 30 row) 가 native 의 마이크로 이득을 상쇄 |

→ B 로 갔다가 (`b9a957f`) **C 로 회귀** (`c00ca79`). JPA 우선순위 `derived > JPQL > native` 원칙 + 본 과제 규모에서 native 가 과한 추상화 비용이라 판단.

### 3. article 인덱스 전략

| 옵션                                    | 인덱스            | 사용처                                              |
| --------------------------------------- | ----------------- | --------------------------------------------------- |
| (A) `pub_date` 단일                     | cleanup 정렬      | 카테고리 조회는 풀스캔                              |
| (B) `(category_id, pub_date DESC)` 복합 | 카테고리별 최신순 | cleanup 정렬 인덱스 미사용                          |
| **(C) 둘 다**                           | A + B             | 약간의 write 비용 ↑, **두 핫패스 모두 인덱스 스캔** |

→ **C 선택** (`f0bed36`). write 가 10분에 ~30 row 라 추가 인덱스 비용 미미. cleanup 과 카테고리 조회 둘 다 핫패스라 양쪽 인덱스 보유가 합리적.

### 4. 카테고리 페이지 응답에 `isRead` 채우기 — 또 다른 N+1

`GET /articles?category=…` 응답에 기사마다 `isRead` 필드 필요.

| 옵션                                         | 쿼리 수 | 단점                                               |
| -------------------------------------------- | ------- | -------------------------------------------------- |
| (A) 기사마다 `existsById(articleId)`         | N       | 정확히 N+1. 200건 = 200쿼리                        |
| (B) `articleReadRepository.findAll()` → Set  | 1       | **전체 읽음 데이터 로드** — 누적 시 메모리 압박    |
| **(C) `findAllById(articleIds)` → Set 매칭** | **1**   | 해당 카테고리 articleId 만 정확히 IN — 메모리 효율 |

→ **C 선택** (`8929f6b`). 단일 사용자 가정이라 article_read 가 작아도, 패턴 자체가 **스케일 가능한 N+1 회피의 정석**.

### 5. `@Scheduled` — `fixedRate` vs `fixedDelay`

| 옵션                 | 동작                              | 위험                                                                          |
| -------------------- | --------------------------------- | ----------------------------------------------------------------------------- |
| (A) `fixedRate`      | 시작 시각 기준 10분마다 강제 실행 | 한 cycle 이 10분을 넘기면 **누락 catch-up 동시 발생** → 외부 RSS 서버에 burst |
| **(B) `fixedDelay`** | 이전 cycle 종료 후 10분 대기      | catch-up 없음 · 외부 서버에 정중 · pool size 변동에도 단일 실행 보장          |

→ **B 선택** (`4473259`). 외부 HTTP fetch + 푸시 dispatch 가 포함되어 cycle 시간 변동성이 있음.

### 6. Hibernate `batch_size` — 다건 INSERT 라운드트립 축소

푸시 발송 시 `push_log` 는 사용자당 1행 INSERT → 한 기사로 최대 100 INSERT.

| 옵션                                           | 효과                                                |
| ---------------------------------------------- | --------------------------------------------------- |
| (A) 단건 INSERT (Hibernate 기본)               | N round-trip                                        |
| **(B) `batch_size=50` + `order_inserts=true`** | 같은 트랜잭션 INSERT 를 JDBC `executeBatch` 로 묶음 |

→ **B 선택** (`1304fa1`). **추가 코드 0, properties 5줄로 끝**.

**실측** — `PushLog` 100건 INSERT 통합 테스트 (`BatchOnPerfTest`, `BatchOffPerfTest`):

| 시나리오 | 소요 시간 |
|---|---|
| `batch_size=50` (현재 운영) | **79ms** |
| `batch_size=0` (비교용) | 195ms |

→ **약 2.5배 빠름**. 재현:
```bash
./gradlew :app:test --tests "*BatchOnPerfTest" --tests "*BatchOffPerfTest" -i 2>&1 | grep PERF
```

### 7. 읽음 처리 후 Next.js ISR 캐시 무효화 — 폭/정확도

ISR 캐시(`revalidate=600`) 가 살아있는 동안엔 새로고침 후 `isRead` 가 반영 안 됨. 정밀 무효화 필요.

| 옵션                                             | 무효화 범위               | 문제점                                         |
| ------------------------------------------------ | ------------------------- | ---------------------------------------------- |
| (A) `revalidatePath("/category/[name]", "page")` | 카테고리 페이지 5장 전부  | 클릭과 무관한 다른 카테고리 캐시까지 같이 폐기 |
| (B) `revalidatePath('/category/' + name)`        | 클릭한 카테고리 1장       | **한국어 path("/category/정치") 매칭 불안정**  |
| **(C) `revalidateTag('articles:' + name)`**      | 해당 카테고리 fetch 1건만 | `next.tags` 선언 필요. **가장 정밀**           |

→ A → B → **C 로 수렴** (`db5fc42` → `b2094da` → 현 `lib/actions.ts`). path 매칭 실패 케이스를 발견한 뒤 fetch tag 방식으로 우회.

### 8. 프론트엔드 렌더링 전략

| 옵션                           | 첫 로딩              | 콘텐츠 최신성               | SEO  | 운영 비용              |
| ------------------------------ | -------------------- | --------------------------- | ---- | ---------------------- |
| (A) CSR (`useEffect` fetch)    | 느림                 | 항상 최신                   | 낮음 | 0                      |
| (B) SSR (매 요청 fetch)        | 보통                 | 항상 최신                   | 높음 | 요청마다 백엔드 호출   |
| **(C) ISR (`revalidate=600`)** | **빠름 (정적 HTML)** | 10분 stale-while-revalidate | 높음 | 10분당 1회 백엔드 호출 |
| (D) full SSG                   | 가장 빠름            | 빌드 시점 고정              | 높음 | RSS 갱신마다 재빌드    |

→ **C 선택.** 백엔드 RSS 스케줄러가 정확히 10분 주기 → `revalidate=600` 으로 **캐시 만료를 데이터 갱신 주기에 정확히 align**. SEO · 성능 · 운영비용의 최적 균형점.

---

## DB 파일 확인

| 항목                                     | 경로                               |
| ---------------------------------------- | ---------------------------------- |
| DB 파일 (SQLite)                         | `backend/data/app.db`              |
| 테이블별 CSV (열기 편하도록 사전 export) | `backend/data/exports/{table}.csv` |

### SQLite CLI 로 직접 조회

```bash
sqlite3 backend/data/app.db
```

#### `category` — 카테고리 5개

```sql
SELECT * FROM category;
```

#### `users` — 사용자 100명

```sql
-- 일부 확인
SELECT * FROM users LIMIT 5;

-- push_type 분포 (APNS / FCM)
SELECT push_type, COUNT(*) FROM users GROUP BY push_type;

-- DND 설정 여부
SELECT
  SUM(CASE WHEN dnd_start IS NULL THEN 1 ELSE 0 END) AS dnd_off,
  SUM(CASE WHEN dnd_start IS NOT NULL THEN 1 ELSE 0 END) AS dnd_on
FROM users;
```

#### `user_category` — 사용자-카테고리 다대다 매핑

```sql
-- 카테고리별 구독자 수
SELECT c.name, COUNT(*) AS subscribers
  FROM user_category uc
  JOIN category c ON uc.category_id = c.id
  GROUP BY c.name
  ORDER BY subscribers DESC;
```

#### `article` — RSS 수집 기사 (최대 1,000건)

```sql
-- 카테고리별 건수
SELECT c.name, COUNT(*) FROM article a
  JOIN category c ON a.category_id = c.id
  GROUP BY c.name;

-- 최신 5건
SELECT article_id, title, pub_date FROM article
  ORDER BY pub_date DESC LIMIT 5;

-- 가장 오래된 5건 (cleanup 대상)
SELECT article_id, title, pub_date FROM article
  ORDER BY pub_date ASC LIMIT 5;
```

#### `article_read` — 기사 읽음 상태

```sql
-- 읽은 기사 목록 (시간순)
SELECT a.title, ar.read_at FROM article_read ar
  JOIN article a ON ar.article_id = a.article_id
  ORDER BY ar.read_at DESC;

-- 총 읽음 처리 건수
SELECT COUNT(*) FROM article_read;
```

#### `push_log` — 푸시 발송 이력

```sql
-- 발송 타입 × 상태 분포
SELECT push_type, status, COUNT(*) FROM push_log
  GROUP BY push_type, status;

-- 카테고리 × 발송 결과
SELECT category, push_type, status, COUNT(*) FROM push_log
  GROUP BY category, push_type, status
  ORDER BY category, push_type, status;

-- 최근 발송 10건
SELECT device_id, push_type, category, status, sent_at FROM push_log
  ORDER BY sent_at DESC LIMIT 10;

-- 특정 사용자(device_id) 의 수신 이력
SELECT category, status, sent_at FROM push_log
  WHERE device_id = 'DEVICE_0001'
  ORDER BY sent_at DESC;
```
