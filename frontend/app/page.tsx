import { Header } from "@/components/layout/Header";
import { getCategories } from "@/lib/api/categories";

export const revalidate = 600;

export default async function Home() {
  const categories = await getCategories();

  return (
    <main>
      <Header />
      <section className="p-6">
        <h1 className="sr-only">카테고리별 최신 뉴스</h1>
        <p className="text-sm text-neutral-500 mb-4">
          {categories.length}개 카테고리 — 카드를 선택하세요
        </p>
        <div className="text-sm text-neutral-400">
          (TODO: 카테고리 카드 그리드)
        </div>
      </section>
    </main>
  );
}
