"use client";

import { useState } from "react";
import { apiClient } from "@/lib/api/client";
import type { Article } from "@/lib/types/domain";

interface Props {
  article: Article;
}

export function ArticleListItem({ article }: Props) {
  const [localRead, setLocalRead] = useState(false);
  const isRead = article.isRead || localRead;

  const handleClick = () => {
    setLocalRead(true);
    void apiClient.POST("/articles/{articleId}/read", {
      params: { path: { articleId: article.articleId } },
    });
  };

  return (
    <li className="border-b border-border">
      <a
        href={article.link}
        target="_blank"
        rel="noopener noreferrer"
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
