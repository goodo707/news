# Frontend

Next.js 기반 뉴스 열람 웹 (과제 1)

---

## 프로젝트 구조

```
frontend/
├── app/                          App Router 페이지
│   ├── layout.tsx                Root layout (폰트, skip link, Header)
│   ├── page.tsx                  / (홈 — 5개 카테고리 카드)
│   ├── loading.tsx               글로벌 fallback
│   ├── error.tsx                 글로벌 에러
│   ├── icon.png                  파비콘 (Next.js file convention)
│   └── category/[name]/
│       ├── page.tsx              /category/{name} (기사 목록)
│       ├── loading.tsx
│       └── not-found.tsx
│
├── components/
│   ├── ui/                       shadcn 자동 생성 (Card, Badge, Button)
│   ├── layout/Header.tsx         공통 헤더 + 카테고리 nav (홈/카테고리 두 가지 디자인)
│   ├── home/                     홈 카드 (MiniCategoryCard)
│   └── category/                 카테고리 페이지 (FeaturedArticle, ArticleListItem)
│
├── lib/
│   ├── api/
│   │   ├── client.ts             openapi-fetch 인스턴스 (서버 전용 API_BASE_URL)
│   │   ├── categories.ts         getCategories()
│   │   └── articles.ts           getArticles(category)
│   ├── types/
│   │   ├── api.ts                Swagger 자동 생성 (pnpm gen:types)
│   │   └── domain.ts             type alias (Category, Article)
│   ├── hooks/
│   │   └── useArticleRead.ts     기사 클릭 시 읽음 처리 훅
│   ├── actions.ts                Server Action — markArticleRead
│   ├── constants.ts              5개 카테고리 상수
│   └── utils.ts                  cn() (shadcn)
│
└── e2e/                          Playwright E2E
    └── happy-path.spec.ts        홈 → 카테고리 → 외부 링크 검증
```

---

## 기술 스택

| 영역           | 선택                                              |
| -------------- | ------------------------------------------------- |
| 프레임워크     | Next.js 16 (App Router, Turbopack)                |
| 언어           | TypeScript                                        |
| UI             | React 19                                          |
| 스타일         | Tailwind CSS v4 + shadcn/ui                       |
| API 클라이언트 | openapi-fetch + openapi-typescript (Swagger SSOT) |
| 폰트           | Noto Sans KR (`next/font` self-host)              |
| E2E            | Playwright                                        |
| 패키지 매니저  | pnpm                                              |

---

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
pnpm e2e                  # Playwright E2E
```

> `pnpm gen:types` 는 백엔드 `/v3/api-docs` 를 호출하므로 백엔드가 떠 있어야 합니다.
