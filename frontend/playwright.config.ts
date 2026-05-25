import { defineConfig, devices } from "@playwright/test";

/**
 * E2E 테스트 설정.
 * - frontend dev 서버는 자동 시작 (이미 떠 있으면 재사용)
 * - 백엔드는 별도로 띄워둬야 함 (localhost:8080)
 */
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  reporter: "list",
  use: {
    baseURL: "http://localhost:3000",
    trace: "on-first-retry",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
  webServer: {
    command: "pnpm dev",
    url: "http://localhost:3000",
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
});
