import type { components } from "./api";

/**
 * 백엔드 응답 스키마는 모든 필드가 optional로 생성되지만
 * 실제 응답은 Java record 기반이라 항상 채워져서 옴.
 * 소비처 편의를 위해 Required로 좁힘.
 */
export type Category = Required<components["schemas"]["CategoryResponse"]>;
export type Article = Required<components["schemas"]["ArticleResponse"]>;
