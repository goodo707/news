"use client";

import { useState } from "react";
import { markArticleRead } from "@/lib/actions";
import type { Article } from "@/lib/types/domain";

/**
 * 기사 클릭 시 읽음 처리 훅.
 *
 * - 클릭 즉시 setState 로 UI 갱신 (optimistic)
 * - Server Action 으로 백엔드 저장 (fire-and-forget; revalidatePath 가 새로고침 후에도 유지)
 * - 서버의 isRead 가 true 이면 처음부터 읽음 상태로 표시
 * - categoryName 은 revalidatePath 정밀화 용도 — 해당 카테고리 페이지 한 곳만 재생성
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
