import { Badge } from "@/components/ui/badge";

interface Props {
  name: string;
  count: number;
}

export function CategoryHeader({ name, count }: Props) {
  return (
    <div className="flex items-baseline gap-4 border-b border-neutral-900 px-6 pt-7 pb-3.5">
      <h1 className="text-4xl font-black tracking-tight">{name}</h1>
      <Badge className="border-transparent bg-brand-light font-bold text-brand">
        {count}건
      </Badge>
    </div>
  );
}
