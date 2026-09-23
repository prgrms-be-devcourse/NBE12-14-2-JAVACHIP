import { apiFetch } from "./client";

export type BudgetRequest = {
    id: number;
    userId: number;
    userName?: string;
    reason: string;
    requestedAmount: number;
    status: string;
    rejectReason?: string;
    createdAt: string;
    updatedAt?: string;
};

export type CreateBudgetRequestPayload = {
    reason: string;
    requested_amount: number;
};

export async function createBudgetRequest(
    roomId: number,
    payload: CreateBudgetRequestPayload,
) {
    return apiFetch<null>(
        `/rooms/${roomId}/budget/request`,
        {
            method: "POST",
            body: payload,
        },
    );
}

export async function getBudgetRequests(roomId: number) {
    return apiFetch<BudgetRequest[]>(
        `/rooms/${roomId}/budget/request/list`,
    );
}

export async function approveBudgetRequest(
    roomId: number,
    requestId: number,
) {
    return apiFetch<null>(
        `/rooms/${roomId}/budget/request/${requestId}/approve`,
        {
            method: "PATCH",
        },
    );
}

export async function rejectBudgetRequest(
    roomId: number,
    requestId: number,
    rejectReason: string,
) {
    return apiFetch<null>(
        `/rooms/${roomId}/budget/request/${requestId}/reject`,
        {
            method: "PATCH",
            body: {
                rejectReason,
            },
        },
    );
}