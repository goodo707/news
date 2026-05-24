# 뉴스 열람 웹 애플리케이션 + 푸시 알림 백엔드

## 프로젝트 구조

```
news/
├── backend/                  # Spring Boot 멀티모듈 (Gradle)
│   ├── core/                 # Entity, Repository, 공통 도메인
│   ├── rss/                  # RSS 수집 스케줄러
│   ├── push/                 # 푸시 발송 서비스
│   └── app/                  # 실행 모듈 (Web API, application.properties)
└── README.md
```

## 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 4.0
- **Database**: SQLite (`backend/data/app.db`)
- **ORM**: Spring Data JPA + Hibernate Community Dialect
- **RSS**: Rome 2.1.0

## 실행 방법

```bash
cd backend
./gradlew :app:bootRun
```

DB 파일은 `backend/data/app.db`에 생성됩니다. `bootRun`의 working directory는 프로젝트 루트(`backend/`)로 고정되어 있어 어디서 실행해도 경로가 일관됩니다.

---

## DB 스키마

### 테이블 목록

| 테이블 | 설명 |
|---|---|
| `category` | 뉴스 카테고리 (정치/북한/경제/산업/사회 — 5개 고정) |
| `article` | RSS 수집 기사 (최대 1,000건, `category_id` FK) |
| `users` | 푸시 알림 대상 사용자 (100명) |
| `user_category` | 사용자-카테고리 다대다 매핑 |
| `article_read` | 기사 읽음 상태 |
| `push_log` | 푸시 발송 이력 |

### DDL

```sql
CREATE TABLE IF NOT EXISTS category (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id        INTEGER PRIMARY KEY,
    name      TEXT NOT NULL,
    device_id TEXT NOT NULL UNIQUE,
    push_type TEXT NOT NULL,
    dnd_start TEXT,
    dnd_end   TEXT
);

CREATE TABLE IF NOT EXISTS user_category (
    user_id     INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, category_id)
);

CREATE TABLE IF NOT EXISTS article (
    article_id  TEXT    NOT NULL PRIMARY KEY,
    title       TEXT    NOT NULL,
    link        TEXT    NOT NULL,
    author      TEXT,
    category_id INTEGER NOT NULL,
    pub_date    TEXT    NOT NULL,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS article_read (
    article_id TEXT NOT NULL PRIMARY KEY,
    read_at    TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS push_log (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    device_id  TEXT NOT NULL,
    push_type  TEXT NOT NULL,
    article_id TEXT NOT NULL,
    title      TEXT NOT NULL,
    category   TEXT NOT NULL,
    sent_at    TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    status     TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_article_pub_date ON article(pub_date);
CREATE INDEX IF NOT EXISTS idx_article_category_pub ON article(category_id, pub_date DESC);
```

### 설계 결정 사항

- **카테고리 정규화**: 카테고리는 `category` 테이블로 분리, `article.category_id` / `user_category.category_id`로 FK 참조. 이름 변경/추가 시 한 곳만 수정
- **FK 제약 없음**: SQLite는 FK가 기본 비활성화. 앱 레이어에서 관계 무결성 보장
- **TEXT 날짜**: SQLite는 DATETIME 타입 없음 → ISO 8601 텍스트(`yyyy-MM-dd'T'HH:mm:ss`)로 통일
- **article_id PK**: RSS `link`에서 추출한 자연키(`AKR...`)를 직접 PK로 사용 → 재실행 시 중복 INSERT 자동 차단
- **user_category 복합 PK**: `(user_id, category_id)` 쌍이 자연 식별자
- **push_log 비정규화**: 기사 1,000건 제한으로 article 삭제 후에도 이력 보존을 위해 `title`, `category`를 텍스트로 직접 저장 (JOIN 의존 X)
- **`idx_article_pub_date` 인덱스**: cleanup 시 `findAllByOrderByPubDateAsc` 정렬을 인덱스 스캔으로 처리
- **`idx_article_category_pub` 복합 인덱스**: 카테고리별 기사 조회 시 `WHERE category_id = ? ORDER BY pub_date DESC` 를 필터+정렬 하나의 인덱스 스캔으로 처리 (full table scan 회피)

### DB 파일 확인

```bash
sqlite3 backend/data/app.db

-- 수집된 기사 (카테고리별 건수)
SELECT c.name, COUNT(*) FROM article a JOIN category c ON a.category_id = c.id GROUP BY c.name;

-- 발송 이력
SELECT push_type, status, COUNT(*) FROM push_log GROUP BY push_type, status;

-- 읽은 기사
SELECT a.title, ar.read_at FROM article_read ar JOIN article a ON ar.article_id = a.article_id;
```
