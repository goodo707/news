import { test, expect } from "@playwright/test";

test("홈 → 카테고리 페이지 → 기사 link (새 탭 + 외부 URL)", async ({ page }) => {
  // 1. 홈 접속 — 5개 카테고리 카드 표시
  await page.goto("/");

  const categoryCards = page.getByRole("link", { name: /카테고리 — 기사/ });
  await expect(categoryCards).toHaveCount(5);

  // 2. "정치" 카테고리 카드 클릭 → 카테고리 페이지로 이동
  await page.getByRole("link", { name: /^정치 카테고리/ }).click();

  await expect(page).toHaveURL(/\/category\/(%EC%A0%95%EC%B9%98|정치)/);
  await expect(
    page.getByRole("heading", { level: 1, name: "정치" }),
  ).toBeVisible();

  // 3. 헤더 nav 의 "정치" 가 active 표시
  const activeNavItem = page.locator('a[aria-current="page"]');
  await expect(activeNavItem).toHaveText("정치");

  // 4. 첫 기사 link — 외부 URL + target="_blank" + rel 보안 속성
  const articleLink = page.locator('a[href*="/view/AKR"]').first();
  await expect(articleLink).toHaveAttribute("target", "_blank");
  await expect(articleLink).toHaveAttribute(
    "rel",
    /noopener.*noreferrer|noreferrer.*noopener/,
  );

  const href = await articleLink.getAttribute("href");
  expect(href).toMatch(/^https:\/\/.+\/view\/AKR/);
});
