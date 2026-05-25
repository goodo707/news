"use server";

import { revalidateTag } from "next/cache";
import { apiClient } from "@/lib/api/client";

export async function markArticleRead(
  articleId: string,
  categoryName: string,
): Promise<void> {
  await apiClient.POST("/articles/{articleId}/read", {
    params: { path: { articleId } },
  });
  // 해당 카테고리의 articles fetch 캐시만 정확히 invalidate.
  // revalidatePath 는 한국어 path 매칭이 불안정해 tag 방식 사용.
  revalidateTag(`articles:${categoryName}`);
}
