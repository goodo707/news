import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

// "2026-05-20T07:52:00" → "2026-05-20 07:52"
// Date 객체를 거치지 않아 SSR/CSR 타임존 차이로 인한 hydration mismatch가 없다.
export function formatPubDate(iso: string): string {
  return iso.replace("T", " ").slice(0, 16);
}
