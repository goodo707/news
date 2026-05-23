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
- **Database**: SQLite (`app.db`)
- **ORM**: Spring Data JPA + Hibernate Community Dialect
- **RSS**: Rome 2.1.0

## 실행 방법

```bash
cd backend
./gradlew :app:bootRun
```

DB 파일은 실행 디렉토리에 `app.db`로 생성됩니다.

---

## DB 스키마

### 테이블 목록

| 테이블 | 설명 |
|---|---|
| `article` | RSS 수집 기사 (최대 1,000건) |
| `users` | 푸시 알림 대상 사용자 (100명) |
| `user_category` | 사용자별 선호 카테고리 |
| `article_read` | 기사 읽음 상태 |
| `push_log` | 푸시 발송 이력 |

### DDL

```sql
CREATE TABLE IF NOT EXISTS article (
    article_id TEXT NOT NULL PRIMARY KEY,
    title      TEXT NOT NULL,
    link       TEXT NOT NULL,
    author     TEXT,
    category   TEXT NOT NULL,
    pub_date   TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
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
    user_id  INTEGER NOT NULL,
    category TEXT    NOT NULL,
    PRIMARY KEY (user_id, category)
);

CREATE TABLE IF NOT EXISTS article_read (
    article_id TEXT NOT NULL PRIMARY KEY,
    read_at    TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS push_log (
    id         INTEGER PRIMARY KEY,
    device_id  TEXT NOT NULL,
    push_type  TEXT NOT NULL,
    article_id TEXT NOT NULL,
    title      TEXT NOT NULL,
    category   TEXT NOT NULL,
    sent_at    TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    status     TEXT NOT NULL
);
```

### 설계 결정 사항

- **FK 없음**: SQLite는 FK가 기본 비활성화. 앱 레이어에서 관계 무결성 보장
- **TEXT 날짜**: SQLite DATETIME은 TEXT로 저장, ISO 8601 형식(`YYYY-MM-DD HH:MM:SS`) 사용
- **article_id PK**: RSS `link`에서 추출한 자연키(`AKR...`)를 직접 PK로 사용
- **user_category 복합 PK**: `(user_id, category)` 쌍이 자연 식별자
- **push_log 비정규화**: 기사 삭제 후에도 이력 보존을 위해 `title`, `category` 직접 저장

### DB 파일 확인

```bash
sqlite3 backend/app.db

-- 수집된 기사 확인
SELECT category, COUNT(*) FROM article GROUP BY category;

-- 발송 이력 확인
SELECT push_type, status, COUNT(*) FROM push_log GROUP BY push_type, status;

-- 읽은 기사 확인
SELECT a.title, ar.read_at FROM article_read ar JOIN article a ON ar.article_id = a.article_id;
```
