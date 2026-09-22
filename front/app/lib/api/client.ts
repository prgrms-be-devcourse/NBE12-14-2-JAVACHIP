"use client";

import { ApiError, type ApiResponse } from "./types";

type ApiFetchOptions = Omit<RequestInit, "body" | "credentials"> & {
  body?: unknown;
  redirectOnUnauthorized?: boolean;
};

const API_BASE_URL = (
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"
).replace(/\/$/, "");

let isRedirectingToLogin = false;

function buildUrl(path: string) {
  return `${API_BASE_URL}/${path.replace(/^\//, "")}`;
}

function isJsonBody(body: unknown): body is Record<string, unknown> {
  return body !== null && typeof body === "object" && !(body instanceof FormData);
}

function redirectToLogin() {
  if (typeof window === "undefined" || isRedirectingToLogin || window.location.pathname === "/login") {
    return;
  }

  isRedirectingToLogin = true;
  const next = `${window.location.pathname}${window.location.search}${window.location.hash}`;
  window.location.replace(`/login?next=${encodeURIComponent(next)}`);
}

export async function apiFetch<T>(
  path: string,
  { body, headers, redirectOnUnauthorized = true, ...options }: ApiFetchOptions = {},
): Promise<T> {
  const requestHeaders = new Headers(headers);
  const jsonBody = isJsonBody(body);

  if (jsonBody && !requestHeaders.has("Content-Type")) {
    requestHeaders.set("Content-Type", "application/json");
  }

  let response: Response;

  try {
    response = await fetch(buildUrl(path), {
      ...options,
      body: body === undefined ? undefined : jsonBody ? JSON.stringify(body) : (body as BodyInit),
      credentials: "include",
      headers: requestHeaders,
    });
  } catch {
    throw new ApiError("서버와 연결할 수 없습니다.", 0);
  }

  const responseBody = await response.json().catch(() => null) as ApiResponse<T> | null;

  if (!response.ok) {
    const error = new ApiError(
      responseBody?.message ?? "요청 처리 중 오류가 발생했습니다.",
      response.status,
    );

    if (response.status === 401 && redirectOnUnauthorized) {
      redirectToLogin();
    }

    throw error;
  }

  if (!responseBody) {
    throw new ApiError("서버 응답을 해석할 수 없습니다.", response.status);
  }

  return responseBody.data;
}
