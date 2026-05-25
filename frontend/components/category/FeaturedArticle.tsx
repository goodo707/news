"use client";

import { useState } from "react";
import { markArticleRead } from "@/lib/actions";
import { Badge } from "@/components/ui/badge";
import type { Article } from "@/lib/types/domain";

interface Props {
  article: Article;
}

export function FeaturedArticle({ article }: Props) {
  const [localRead, setLocalRead] = useState(false);
  const isRead = article.isRead || localRead;

  const handleClick = () => {
    setLocalRead(true);
    void markArticleRead(article.articleId);
  };

  return (
    <article className={`flex flex-col border-b-2 border-foreground pb-8 ${isRead ? "opacity-60" : ""}`}>
      <Badge className="self-start border-transparent bg-accent-red text-white">
        최신
      </Badge>
      <a
        href={article.link}
        target="_blank"
        rel="noopener noreferrer"
        aria-label={`${article.title} (새 탭에서 열림)${isRead ? " — 읽은 기사" : ""}`}
        onClick={handleClick}
        className="mt-2.5 text-2xl font-black leading-tight tracking-tight outline-none hover:text-brand focus-visible:text-brand focus-visible:ring-2 focus-visible:ring-brand"
      >
        {article.title}
      </a>
      <div className="mt-3.5 flex items-center gap-1.5 text-xs text-muted-foreground">
        <span>{article.author}</span>
        <span className="text-neutral-300">·</span>
        <time dateTime={article.pubDate}>{article.pubDate}</time>
        {isRead && (
          <span className="rounded-sm bg-muted px-1.5 py-0.5 text-[0.625rem]">
            <span className="sr-only">상태: </span>읽음
          </span>
        )}
      </div>
    </article>
  );
}
