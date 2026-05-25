import type { Metadata } from "next";
import { Noto_Sans_KR } from "next/font/google";
import { cn } from "@/lib/utils";
import "./globals.css";

const notoSansKR = Noto_Sans_KR({
  variable: "--font-sans",
  subsets: ["latin"],
  weight: ["400", "500", "700", "900"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "뉴스",
  description: "카테고리별 최신 뉴스 열람",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" className={cn("h-full antialiased", notoSansKR.variable)}>
      <body className="min-h-full font-sans bg-neutral-100">
        <a
          href="#main-content"
          className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-2 focus:z-50 focus:rounded focus:bg-brand focus:px-4 focus:py-2 focus:text-white focus:shadow-lg"
        >
          본문 바로가기
        </a>
        <div className="max-w-5xl mx-auto bg-white shadow-sm min-h-screen">
          {children}
        </div>
      </body>
    </html>
  );
}
