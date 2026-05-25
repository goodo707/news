"use client";

import Link from "next/link";
import { Button } from "@/components/ui/button";

interface ErrorProps {
  error: Error & { digest?: string };
  reset: () => void;
}

export default function GlobalError({ error, reset }: ErrorProps) {
  const message =
    process.env.NODE_ENV === "production"
      ? "잠시 후 다시 시도해주세요."
      : error.message || "잠시 후 다시 시도해주세요.";

  return (
    <main id="main-content" className="p-10 text-center">
      <h1 className="mb-2 text-2xl font-bold">문제가 발생했습니다</h1>
      <p className="mb-6 text-sm text-muted-foreground">{message}</p>
      <div className="flex justify-center gap-3">
        <Button onClick={reset} variant="outline">
          다시 시도
        </Button>
        <Button render={<Link href="/" />}>홈으로</Button>
      </div>
    </main>
  );
}
