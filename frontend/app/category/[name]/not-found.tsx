import Link from "next/link";

export default function NotFound() {
  return (
    <main id="main-content">
      <div className="p-10 text-center">
        <h1 className="mb-2 text-2xl font-bold">
          카테고리를 찾을 수 없습니다
        </h1>
        <p className="mb-4 text-sm text-muted-foreground">
          존재하지 않거나 잘못된 카테고리입니다.
        </p>
        <Link href="/" className="text-brand underline">
          홈으로 돌아가기
        </Link>
      </div>
    </main>
  );
}
