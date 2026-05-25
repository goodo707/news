import Link from "next/link";
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardAction,
  CardContent,
} from "@/components/ui/card";
import type { Article } from "@/lib/types/domain";

interface Props {
  name: string;
  icon: string;
  topArticles: Article[];
}

export function MiniCategoryCard({ name, icon, topArticles }: Props) {
  return (
    <Link
      href={`/category/${name}`}
      aria-label={`${name} 카테고리 — 기사 ${topArticles.length}건`}
      className="group block rounded-xl outline-none focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2"
    >
      <Card className="min-h-[46] bg-card transition-all group-hover:-translate-y-0.5 group-hover:shadow-md group-hover:ring-brand">
        <CardHeader>
          <CardTitle className="text-lg font-black tracking-tight">
            {name}
          </CardTitle>
          <CardDescription>최신 기사 {topArticles.length}건</CardDescription>
          <CardAction aria-hidden className="text-xl">
            {icon}
          </CardAction>
        </CardHeader>

        <CardContent>
          <ul>
            {topArticles.map((article, i) => (
              <li
                key={article.articleId}
                className={`line-clamp-1 py-1.5 text-xs leading-snug text-muted-foreground ${
                  i > 0 ? "border-t border-border" : ""
                }`}
              >
                <span className="font-bold text-brand">·</span> {article.title}
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </Link>
  );
}
