"use server";

import { revalidatePath } from "next/cache";
import { apiClient } from "@/lib/api/client";

export async function markArticleRead(
  articleId: string,
  categoryName: string,
): Promise<void> {
  await apiClient.POST("/articles/{articleId}/read", {
    params: { path: { articleId } },
  });
  // 호출한 카테고리 페이지 한 곳만 재생성 — 다른 카테고리는 영향 없음.
  revalidatePath(`/category/${categoryName}`, "page");
}
