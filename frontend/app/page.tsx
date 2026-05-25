import { MiniCategoryCard } from "@/components/home/MiniCategoryCard";
import { getArticles } from "@/lib/api/articles";

export const revalidate = 600;

const CATEGORY_META = [
  { name: "정치", icon: "🏛️" },
  { name: "북한", icon: "🌏" },
  { name: "경제", icon: "📈" },
  { name: "산업", icon: "🏭" },
  { name: "사회", icon: "📰" },
] as const;

export default async function Home() {
  const articlesByCategory = await Promise.all(
    CATEGORY_META.map((c) => getArticles(c.name).catch(() => [])),
  );

  const categoryCards = CATEGORY_META.map((meta, i) => ({
    ...meta,
    topArticles: articlesByCategory[i].slice(0, 3),
  }));

  return (
    <main id="main-content">
      <section className="grid grid-cols-1 gap-3 p-6 sm:grid-cols-2 lg:grid-cols-5">
        <h1 className="sr-only">카테고리별 최신 뉴스</h1>
        {categoryCards.map((cat) => (
          <MiniCategoryCard key={cat.name} {...cat} />
        ))}
      </section>
    </main>
  );
}
