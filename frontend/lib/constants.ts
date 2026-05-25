export const CATEGORIES = ["정치", "북한", "경제", "산업", "사회"] as const;
export type CategoryName = (typeof CATEGORIES)[number];
