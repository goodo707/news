import { Header } from "@/components/layout/Header";

export default function Loading() {
  return (
    <main>
      <Header />
      <div className="p-6 text-sm text-muted-foreground">
        기사를 불러오는 중...
      </div>
    </main>
  );
}
