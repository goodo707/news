import { Header } from "@/components/layout/Header";
import { FeaturedCategoryCard } from "@/components/home/FeaturedCategoryCard";
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

  const [featured, ...rest] = CATEGORY_META.map((meta, i) => ({
    ...meta,
    topArticles: articlesByCategory[i].slice(0, 3),
  }));

  return (
    <main id="main-content">
      <Header />
      <section className="grid grid-cols-1 gap-6 p-6 md:grid-cols-[1.4fr_1fr]">
        <h1 className="sr-only">카테고리별 최신 뉴스</h1>
        <FeaturedCategoryCard {...featured} />
        <div className="grid grid-cols-2 gap-3">
          {rest.map((cat) => (
            <MiniCategoryCard key={cat.name} {...cat} />
          ))}
        </div>
      </section>
    </main>
  );
}
