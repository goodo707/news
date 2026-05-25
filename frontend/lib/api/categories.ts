import { apiClient } from "./client";
import type { Category } from "@/lib/types/domain";

export async function getCategories(): Promise<Category[]> {
  const { data, error } = await apiClient.GET("/categories", {
    next: { revalidate: 600 },
  });
  if (error || !data) {
    throw new Error("카테고리 목록을 불러오지 못했습니다");
  }
  return data as Category[];
}
