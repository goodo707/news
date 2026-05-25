import createClient from "openapi-fetch";
import type { paths } from "@/lib/types/api";

const baseUrl = process.env.API_BASE_URL ?? "http://localhost:8080";

export const apiClient = createClient<paths>({ baseUrl });
