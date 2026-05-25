import { apiClient } from "./client";
import type { Article } from "@/lib/types/domain";

export async function getArticles(category: string): Promise<Article[]> {
  const { data, error } = await apiClient.GET("/articles", {
    params: { query: { category } },
    // tag 부여 — markArticleRead Server Action 의 revalidateTag 로 정확히 invalidate.
    // (한국어 path 의 revalidatePath 가 cache 키 매칭에 불안정해 tag 방식으로 우회)
    next: { revalidate: 600, tags: [`articles:${category}`] },
  });
  if (error || !data) {
    throw new Error(`기사 목록을 불러오지 못했습니다: ${category}`);
  }
  return data as Article[];
}
