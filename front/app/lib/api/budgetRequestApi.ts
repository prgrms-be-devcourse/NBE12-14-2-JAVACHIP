import { apiFetch } from "./client";

export type BudgetRequest = {
    id: number;
    roomId: number;
    userId: number;
    reason: string;
    requestedAmount: number;
    status: string;
    rejectReason: string | null;
    createdAt: string;
    updatedAt: string;
};

export type BudgetRequestCreateRequest = {
    reason: string;
    requested_amount: number;
};

export async function createBudgetRequest(
    roomId: number,
    request: BudgetRequestCreateRequest,
) {
    return apiFetch<null>(
        `/rooms/${roomId}/budget/request`,
        {
            method: "POST",
            body: request,
        },
    );
}

export async function getBudgetRequests(roomId: number) {
    return apiFetch<BudgetRequest[]>(
        `/rooms/${roomId}/budget/request/list`,
    );
}

export async function getBudgetRequest(
    roomId: number,
    requestId: number,
) {
    return apiFetch<BudgetRequest>(
        `/rooms/${roomId}/budget/request/${requestId}`,
    );
}

export async function deleteBudgetRequest(
    roomId: number,
    requestId: number,
) {
    return apiFetch<null>(
        `/rooms/${roomId}/budget/request/${requestId}`,
        {
            method: "DELETE",
        },
    );
}