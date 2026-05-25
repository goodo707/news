"use client";

import { Badge } from "@/components/ui/badge";
import type { Article } from "@/lib/types/domain";

interface Props {
  article: Article;
}

export function FeaturedArticle({ article }: Props) {
  return (
    <article className="flex flex-col border-r border-border pr-7">
      <Badge className="self-start border-transparent bg-accent-red text-white">
        최신
      </Badge>
      <a
        href={article.link}
        target="_blank"
        rel="noopener noreferrer"
        className="mt-2.5 text-2xl font-black leading-tight tracking-tight outline-none hover:text-brand focus-visible:text-brand focus-visible:ring-2 focus-visible:ring-brand"
      >
        {article.title}
      </a>
      <div className="mt-3.5 flex items-center gap-1.5 text-xs text-muted-foreground">
        <span>{article.author}</span>
        <span className="text-neutral-300">·</span>
        <time dateTime={article.pubDate}>{article.pubDate}</time>
      </div>
    </article>
  );
}
