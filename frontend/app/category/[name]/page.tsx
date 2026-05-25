import { notFound } from "next/navigation";
import { Header } from "@/components/layout/Header";
import { CATEGORIES, type CategoryName } from "@/lib/constants";
import { getArticles } from "@/lib/api/articles";

export const revalidate = 600;

interface PageProps {
  params: Promise<{ name: string }>;
}

export async function generateMetadata({ params }: PageProps) {
  const { name } = await params;
  const decoded = decodeURIComponent(name);
  return {
    title: `${decoded} - 뉴스`,
    description: `${decoded} 카테고리의 최신 기사 목록`,
  };
}

export async function generateStaticParams() {
  return CATEGORIES.map((name) => ({ name }));
}

export default async function CategoryPage({ params }: PageProps) {
  const { name: rawName } = await params;
  const name = decodeURIComponent(rawName);

  if (!CATEGORIES.includes(name as CategoryName)) {
    notFound();
  }

  const articles = await getArticles(name);

  return (
    <main>
      <Header activeCategory={name} />
      <section className="p-6">
        <h1 className="text-3xl font-black tracking-tight">{name}</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          기사 {articles.length}건
        </p>
        <div className="mt-6 text-sm text-muted-foreground">
          (TODO: 피처드 기사 + 리스트 UI)
        </div>
      </section>
    </main>
  );
}
