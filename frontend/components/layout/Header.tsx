"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { CATEGORIES } from "@/lib/constants";

function getActiveCategory(pathname: string): string | undefined {
  const match = pathname.match(/^\/category\/([^/]+)/);
  if (!match) return undefined;
  return decodeURIComponent(match[1]);
}

export function Header() {
  const pathname = usePathname();
  const activeCategory = getActiveCategory(pathname);

  if (activeCategory) {
    return (
      <header className="sticky top-0 z-50 bg-white border-b border-neutral-200">
        <div className="max-w-5xl mx-auto flex items-center gap-8 px-6 py-3">
          <Link
            href="/"
            className="text-xl font-black text-brand tracking-tight outline-none focus-visible:ring-2 focus-visible:ring-brand"
          >
            뉴스
          </Link>
          <nav aria-label="카테고리">
            <ul className="flex items-center gap-1">
              {CATEGORIES.map((cat) => {
                const isActive = cat === activeCategory;
                return (
                  <li key={cat}>
                    <Link
                      href={`/category/${cat}`}
                      aria-current={isActive ? "page" : undefined}
                      className={`block px-3 py-1.5 rounded text-sm font-bold tracking-tight outline-none focus-visible:ring-2 focus-visible:ring-brand transition-colors ${
                        isActive
                          ? "text-brand bg-brand-light"
                          : "text-neutral-900 hover:text-brand hover:bg-neutral-50"
                      }`}
                    >
                      {cat}
                    </Link>
                  </li>
                );
              })}
            </ul>
          </nav>
        </div>
      </header>
    );
  }

  return (
    <header className="bg-white">
      <div className="max-w-5xl mx-auto flex items-center justify-center border-b-2 border-neutral-900 py-6">
        <Link
          href="/"
          className="text-3xl font-black text-brand tracking-tight outline-none focus-visible:ring-2 focus-visible:ring-brand"
        >
          뉴스
        </Link>
      </div>
    </header>
  );
}
