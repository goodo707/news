"use client";

import { useArticleRead } from "@/lib/hooks/useArticleRead";
import type { Article } from "@/lib/types/domain";

interface Props {
  article: Article;
}

export function ArticleListItem({ article }: Props) {
  const { isRead, handleClick } = useArticleRead(article);

  return (
    <li className="border-b border-border md:odd:border-r md:odd:pr-7 md:even:pl-7">
      <a
        href={article.link}
        target="_blank"
        rel="noopener noreferrer"
        aria-label={`${article.title} (새 탭에서 열림)${isRead ? " — 읽은 기사" : ""}`}
        onClick={handleClick}
        className={`block py-3.5 outline-none hover:text-brand focus-visible:text-brand focus-visible:ring-2 focus-visible:ring-brand ${
          isRead ? "opacity-55" : ""
        }`}
      >
        <h3
          className={`mb-1.5 text-[0.9375rem] leading-snug tracking-tight ${
            isRead ? "font-medium text-muted-foreground" : "font-extrabold"
          }`}
        >
          {article.title}
        </h3>
        <div className="flex items-center gap-2 text-[0.6875rem] text-muted-foreground">
          <span>{article.author}</span>
          <span className="text-neutral-300">·</span>
          <time dateTime={article.pubDate}>{article.pubDate}</time>
          {isRead && (
            <span className="rounded-sm bg-muted px-1.5 py-0.5 text-[0.625rem]">
              <span className="sr-only">상태: </span>읽음
            </span>
          )}
        </div>
      </a>
    </li>
  );
}
