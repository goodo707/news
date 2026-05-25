import Link from "next/link";
import { CATEGORIES } from "@/lib/constants";

interface HeaderProps {
  activeCategory?: string;
}

export function Header({ activeCategory }: HeaderProps) {
  return (
    <header className="bg-white">
      <div className="flex items-center justify-between px-6 py-2 text-xs text-neutral-600 border-b border-neutral-100">
        <Link href="/" className="font-bold text-brand">← 홈</Link>
        <span>2026.05.25 (월)</span>
      </div>

      <div className="flex items-center justify-center gap-2 py-4">
        <div
          aria-hidden
          className="relative w-8 h-8 rounded-full bg-brand flex items-center justify-center text-white font-black text-sm"
        >
          N
          <span className="absolute top-1 right-1 w-1.5 h-1.5 bg-accent-red rounded-full" />
        </div>
        <span className="text-2xl font-black text-brand tracking-tight">뉴스</span>
      </div>

      <nav aria-label="카테고리" className="border-b-2 border-neutral-900 px-6">
        <ul className="flex">
          {CATEGORIES.map((cat) => {
            const isActive = cat === activeCategory;
            return (
              <li key={cat}>
                <Link
                  href={`/category/${cat}`}
                  aria-current={isActive ? "page" : undefined}
                  className={`block px-4 py-3 text-sm font-bold tracking-tight outline-none focus-visible:ring-2 focus-visible:ring-brand transition-colors ${
                    isActive
                      ? "text-brand border-b-[3px] border-brand -mb-[2px]"
                      : "text-neutral-900 hover:text-brand"
                  }`}
                >
                  {cat}
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>
    </header>
  );
}
