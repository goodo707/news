import { notFound } from "next/navigation";
import { Header } from "@/components/layout/Header";
import { CategoryHeader } from "@/components/category/CategoryHeader";
import { FeaturedArticle } from "@/components/category/FeaturedArticle";
import { ArticleListItem } from "@/components/category/ArticleListItem";
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

  if (articles.length === 0) {
    return (
      <main id="main-content">
        <Header activeCategory={name} />
        <CategoryHeader name={name} count={0} />
        <p className="p-10 text-center text-sm text-muted-foreground">
          이 카테고리에 등록된 기사가 없습니다.
        </p>
      </main>
    );
  }

  const [featured, ...rest] = articles;

  return (
    <main id="main-content">
      <Header activeCategory={name} />
      <CategoryHeader name={name} count={articles.length} />
      <div className="grid grid-cols-1 gap-7 p-6 md:grid-cols-[1.2fr_1fr]">
        <FeaturedArticle article={featured} />
        <ul>
          {rest.map((article) => (
            <ArticleListItem key={article.articleId} article={article} />
          ))}
        </ul>
      </div>
    </main>
  );
}
