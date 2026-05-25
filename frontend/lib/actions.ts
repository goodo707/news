"use server";

import { revalidatePath } from "next/cache";
import { apiClient } from "@/lib/api/client";

export async function markArticleRead(articleId: string): Promise<void> {
  await apiClient.POST("/articles/{articleId}/read", {
    params: { path: { articleId } },
  });
  revalidatePath("/category/[name]", "page");
}
