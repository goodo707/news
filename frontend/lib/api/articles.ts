import { apiClient } from "./client";
import type { Article } from "@/lib/types/domain";

export async function getArticles(category: string): Promise<Article[]> {
  const { data, error } = await apiClient.GET("/articles", {
    params: { query: { category } },
    next: { revalidate: 600 },
  });
  if (error || !data) {
    throw new Error(`기사 목록을 불러오지 못했습니다: ${category}`);
  }
  return data as Article[];
}
