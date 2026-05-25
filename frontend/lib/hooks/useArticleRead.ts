"use client";

import { useState } from "react";
import { markArticleRead } from "@/lib/actions";
import type { Article } from "@/lib/types/domain";

/**
 * 클릭 시 optimistic UI + Server Action 으로 읽음 기록.
 * categoryName 은 revalidateTag 의 카테고리별 cache invalidate 에 사용.
 */
export function useArticleRead(
  article: Pick<Article, "articleId" | "isRead">,
  categoryName: string,
) {
  const [localRead, setLocalRead] = useState(false);
  const isRead = article.isRead || localRead;

  const handleClick = () => {
    setLocalRead(true);
    void markArticleRead(article.articleId, categoryName);
  };

  return { isRead, handleClick };
}
