import { apiFetch } from "./client";

export type BudgetCurrency = "KRW" | "USD" | "JPY";
export type BudgetType = "SETTLEMENT" | "INCREASE" | "DECREASE";

export type Budget = {
  roomId: number;
  roomName: string;
  totalBudget: number;
  availableBudget: number;
  reserveBudget: number;
  currency: BudgetCurrency;
};

export type BudgetHistoryItem = {
  id: number;
  changeBudget: number;
  changedBudget: number;
  type: BudgetType;
  userName: string;
  reason: string;
  processedAt: string;
};

export type BudgetHistoryResponse = {
  history: BudgetHistoryItem[];
};

export type BudgetUpdateRequest = {
  totalBudget: number;
  budgetType: BudgetType;
  reason?: string;
};

export type BudgetUpdateResponse = {
  roomId: number;
  totalBudget: number;
};

export async function getBudget(roomId: number) {
  return apiFetch<Budget>(`/rooms/${roomId}/budget`);
}

export async function getBudgetHistory(roomId: number) {
  return apiFetch<BudgetHistoryResponse>(
    `/rooms/${roomId}/budget/history`,
  );
}

export async function updateBudget(
  roomId: number,
  request: BudgetUpdateRequest,
) {
  return apiFetch<BudgetUpdateResponse>(`/rooms/${roomId}/budget`, {
    method: "PATCH",
    body: request,
  });
}
