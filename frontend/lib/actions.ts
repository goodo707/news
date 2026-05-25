"use server";

import { updateTag } from "next/cache";
import { apiClient } from "@/lib/api/client";

export async function markArticleRead(
  articleId: string,
  categoryName: string,
): Promise<void> {
  await apiClient.POST("/articles/{articleId}/read", {
    params: { path: { articleId } },
  });
  // 해당 카테고리의 articles fetch 캐시만 정확히 invalidate.
  // Next 16: Server Action 안에서는 updateTag 사용 — 즉시 만료 + read-your-own-writes.
  updateTag(`articles:${categoryName}`);
}
