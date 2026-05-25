# 뉴스 프론트엔드 (과제 1)

Next.js + TypeScript + Tailwind + shadcn/ui 기반 뉴스 열람 웹 앱.

## 실행 방법

### 사전 요구 사항

- Node.js 20+
- pnpm 9+
- 백엔드 (`http://localhost:8080`) 실행 중

### 명령어

```bash
pnpm install              # 의존성 설치
pnpm gen:types            # 백엔드 Swagger → lib/types/api.ts 자동 생성
pnpm dev                  # 개발 서버 (http://localhost:3000)
pnpm build                # 프로덕션 빌드
pnpm start                # 빌드 결과 실행
pnpm lint                 # ESLint
```

> `pnpm gen:types`는 백엔드 `/v3/api-docs` 를 호출하므로 백엔드가 떠 있어야 합니다.

## 기술 스택

| 영역 | 선택 |
|---|---|
| 프레임워크 | Next.js 16 (App Router, Turbopack) |
| 언어 | TypeScript |
| 스타일 | Tailwind CSS v4 + shadcn/ui |
| API 클라이언트 | openapi-fetch + openapi-typescript (Swagger SSOT) |
| 폰트 | Noto Sans KR (`next/font` self-host) |
| 패키지 매니저 | pnpm |

## 디렉터리 구조

```
app/                          App Router 페이지
├── layout.tsx                Root layout (폰트, skip link)
├── page.tsx                  / (홈 — 5개 카테고리 카드)
├── loading.tsx / error.tsx   글로벌 fallback
└── category/[name]/
    ├── page.tsx              /category/{name} (기사 목록)
    ├── loading.tsx
    └── not-found.tsx

components/
├── ui/                       shadcn 자동 생성 (Card, Badge, Button)
├── layout/Header.tsx         공통 헤더 + 카테고리 nav
├── home/                     홈 카드 (Featured / Mini)
└── category/                 카테고리 페이지 컴포넌트

lib/
├── api/
│   ├── client.ts             openapi-fetch 인스턴스
│   ├── categories.ts         getCategories()
│   └── articles.ts           getArticles(category)
├── types/
│   ├── api.ts                Swagger 자동 생성 (gen:types)
│   └── domain.ts             type alias (Category, Article)
├── actions.ts                Server Action — markArticleRead
├── constants.ts              5개 카테고리 상수
└── utils.ts                  cn() (shadcn)
```

## 페이지 구성

| 경로 | 설명 |
|---|---|
| `/` | 홈 — 5개 카테고리 카드 (정치 피처드 + 4개 미니 그리드). 각 카드에 최신 기사 3건 미리보기. |
| `/category/{이름}` | 카테고리별 기사 목록. 좌측 피처드(최신 1건) + 우측 리스트. |

기사 클릭 → **새 탭으로 외부 원문 열림** + 백엔드에 읽음 기록 (optimistic UI).

## 설계 포인트

### 1. ISR (Incremental Static Regeneration)

- 모든 페이지 `export const revalidate = 600` — 빌드 시점에 정적 prerender + 10분마다 백그라운드 재생성
- 백엔드 RSS 수집 주기(10분)와 정합
- `/category/[name]` 은 `generateStaticParams` 로 5개 정적 페이지 생성
- 첫 요청도 정적 HTML 서빙 → TTFB 최소화 + SEO 친화적

### 2. 타입 안전한 API 호출 (Swagger SSOT)

- 백엔드 springdoc-openapi 가 `/v3/api-docs` 노출
- `pnpm gen:types` 한 줄로 `lib/types/api.ts` 자동 생성
- `openapi-fetch` 가 컴파일 타임에 URL 오타, 파라미터 누락, 응답 필드 오류 검출
- 백엔드 API 변경 → `pnpm gen:types` → 프론트 타입 자동 동기화

### 3. 읽음 처리 — Optimistic UI + Server Action

- 클릭 즉시 `useState` 로 회색 처리 (사용자 체감 0ms)
- 백엔드 호출은 Server Action (`markArticleRead`) 으로 위임
- Server Action 내부에서 `revalidatePath` 호출 → 새로고침 후에도 읽음 상태 유지
- 실패해도 UX 영향 없음 — 다음 방문 시 서버 `isRead` 로 동기화

### 4. 웹 접근성 (WCAG 2.1 / KWCAG)

- 시멘틱 HTML (`<nav>`, `<main>`, `<article>`, `<time>`)
- 본문 바로가기 skip link (WCAG 2.4.1 Bypass Blocks)
- 모든 인터랙티브 요소에 `aria-label` + `focus-visible` 링
- 외부 링크는 "(새 탭에서 열림)" 명시
- 헤더 nav `aria-current="page"` 로 활성 카테고리 표시
- 한국어 명시 (`<html lang="ko">`)

### 5. 최소 의존성

- **TanStack Query 미도입** — mutation 1개뿐, Server Action으로 충분
- **axios 미도입** — `fetch()` 기반 `openapi-fetch` 만 사용
- **글로벌 상태 관리 미도입** — 서버 상태가 SSOT

## 데이터 흐름

```
[Browser] ─ GET / ───────────────────┐
                                     │
                          ┌──────────▼──────────┐
                          │ Next.js (RSC + ISR) │
                          │   prerendered HTML  │
                          └──────────┬──────────┘
                                     │ (10분마다 백그라운드 재생성)
                                     │
                  GET /articles?category=... 
                                     │
                          ┌──────────▼──────────┐
                          │ Spring Boot :8080   │
                          │   articles table    │
                          └─────────────────────┘

[기사 클릭]
  ↓ optimistic setState
  ↓ Server Action: markArticleRead(articleId)
        ↓ POST /articles/{id}/read
        ↓ revalidatePath("/category/[name]", "page")
[새로고침] → 갱신된 ISR HTML → isRead=true 반영
```
