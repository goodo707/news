import Link from "next/link";
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from "@/components/ui/card";
import type { Article } from "@/lib/types/domain";

interface Props {
  name: string;
  icon: string;
  topArticles: Article[];
}

export function FeaturedCategoryCard({ name, icon, topArticles }: Props) {
  return (
    <Link
      href={`/category/${name}`}
      aria-label={`${name} 카테고리 — 기사 ${topArticles.length}건`}
      className="group block rounded-xl outline-none focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2"
    >
      <Card className="relative overflow-hidden bg-linear-to-br from-brand to-brand-dark text-white ring-0 py-7 min-h-[95] gap-5 transition-all group-hover:-translate-y-0.5 group-hover:shadow-lg">
        <div
          aria-hidden
          className="absolute -top-10 -right-10 w-40 h-40 rounded-full bg-white/5"
        />
        <div
          aria-hidden
          className="absolute -bottom-16 -left-16 w-52 h-52 rounded-full bg-white/5"
        />

        <CardHeader className="relative">
          <div className="text-3xl opacity-90" aria-hidden>
            {icon}
          </div>
          <CardTitle className="text-3xl font-black tracking-tight text-white">
            {name}
          </CardTitle>
          <CardDescription className="text-white/70">
            최신 기사 {topArticles.length}건
          </CardDescription>
        </CardHeader>

        <CardContent className="relative">
          <ul>
            {topArticles.map((article, i) => (
              <li
                key={article.articleId}
                className={`flex gap-2.5 py-2.5 text-sm font-semibold leading-snug ${
                  i > 0 ? "border-t border-white/10" : ""
                }`}
              >
                <span className="flex h-5 w-5 shrink-0 items-center justify-center rounded bg-white/15 text-[0.625rem] font-bold">
                  {i + 1}
                </span>
                <span className="line-clamp-2">{article.title}</span>
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </Link>
  );
}
