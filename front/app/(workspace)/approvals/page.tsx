"use client";

import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "next/navigation";

import { getBudget } from "../../lib/api/budgetApi";
import {
    getBudgetRequests,
    type BudgetRequest,
} from "../../lib/api/budgetRequestApi";

export default function ApprovalsPage() {
    const searchParams = useSearchParams();

    const roomIdParam = searchParams.get("roomId");
    const roomId = roomIdParam ? Number(roomIdParam) : null;

    const [requests, setRequests] = useState<BudgetRequest[]>([]);
    const [availableBudget, setAvailableBudget] = useState<number | null>(null);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!roomId || Number.isNaN(roomId)) {
            return;
        }

        const loadApprovalData = async () => {
            try {
                const [requestData, budgetData] = await Promise.all([
                    getBudgetRequests(roomId),
                    getBudget(roomId),
                ]);

                setRequests(requestData);
                setAvailableBudget(budgetData.availableBudget);
                setError(null);
            } catch (error) {
                console.error(error);
                setError("승인 관리 정보를 불러오지 못했습니다.");
            } finally {
                setLoading(false);
            }
        };

        void loadApprovalData();
    }, [roomId]);

    const pendingRequests = useMemo(
        () => requests.filter((request) => request.status === "PENDING"),
        [requests],
    );

    const processedRequests = useMemo(
        () => requests.filter((request) => request.status !== "PENDING"),
        [requests],
    );

    const formatAmount = (amount: number) =>
        `${amount.toLocaleString("ko-KR")}원`;

    if (!roomId) {
        return (
            <section className="mx-auto max-w-5xl">
                <p className="text-sm text-zinc-500">
                    모임 정보가 없습니다.
                </p>
            </section>
        );
    }

    return (
        <section className="mx-auto max-w-5xl">
            <div className="mb-7">
                <h1 className="text-2xl font-bold text-zinc-900">
                    승인 관리
                </h1>

                <p className="mt-2 text-sm text-zinc-500">
                    멤버들의 예산 신청을 검토하고 승인 또는 반려하세요
                </p>
            </div>

            {/* 현재 사용 가능한 예산 */}
            <div className="mb-7 flex items-center justify-between rounded-2xl border border-indigo-200 bg-indigo-50 px-6 py-5">
                <div>
                    <p className="text-sm font-semibold text-indigo-700">
                        현재 사용 가능한 예산
                    </p>

                    <p className="mt-1 text-xs text-indigo-400">
                        이 금액 내에서 승인할 수 있어요
                    </p>
                </div>

                <p className="text-2xl font-bold text-indigo-600">
                    {availableBudget === null
                        ? "-"
                        : formatAmount(availableBudget)}
                </p>
            </div>

            {/* 로딩 */}
            {loading && (
                <div className="rounded-2xl border border-zinc-200 bg-white p-8 text-center text-sm text-zinc-500">
                    승인 관리 정보를 불러오는 중입니다.
                </div>
            )}

            {/* 에러 */}
            {!loading && error && (
                <div className="rounded-2xl border border-red-200 bg-red-50 p-8 text-center text-sm text-red-600">
                    {error}
                </div>
            )}

            {/* 데이터 */}
            {!loading && !error && (
                <>
                    {/* 대기 중 */}
                    <div>
                        <div className="mb-3 flex items-center gap-1">
                            <h2 className="font-semibold text-zinc-900">
                                대기 중
                            </h2>

                            <span className="text-sm font-semibold text-amber-600">
                ({pendingRequests.length}건)
              </span>
                        </div>

                        {pendingRequests.length === 0 ? (
                            <div className="rounded-2xl border border-zinc-200 bg-white px-6 py-10 text-center text-sm text-zinc-400">
                                대기 중인 예산 신청이 없습니다.
                            </div>
                        ) : (
                            <div className="space-y-3">
                                {pendingRequests.map((request) => (
                                    <ApprovalCard
                                        key={request.id}
                                        request={request}
                                        formatAmount={formatAmount}
                                    />
                                ))}
                            </div>
                        )}
                    </div>

                    {/* 처리된 요청 */}
                    <div className="mt-10">
                        <h2 className="mb-3 font-semibold text-zinc-900">
                            처리된 요청
                        </h2>

                        {processedRequests.length === 0 ? (
                            <div className="rounded-2xl border border-zinc-200 bg-white px-6 py-10 text-center text-sm text-zinc-400">
                                처리된 요청이 없습니다.
                            </div>
                        ) : (
                            <div className="overflow-hidden rounded-2xl border border-zinc-200 bg-white">
                                <div className="grid grid-cols-[1fr_2fr_1fr_100px] bg-zinc-50 px-5 py-3 text-xs font-medium text-zinc-500">
                                    <span>신청자</span>
                                    <span>사유</span>
                                    <span>금액</span>
                                    <span>결과</span>
                                </div>

                                <div className="divide-y divide-zinc-100">
                                    {processedRequests.map((request) => (
                                        <div
                                            key={request.id}
                                            className="grid grid-cols-[1fr_2fr_1fr_100px] items-center px-5 py-4"
                                        >
                      <span className="text-sm font-medium text-zinc-800">
                        User #{request.userId}
                      </span>

                                            <span className="truncate text-sm text-zinc-500">
                        {request.reason}
                      </span>

                                            <span className="text-sm font-semibold text-zinc-900">
                        {formatAmount(request.requestedAmount)}
                      </span>

                                            <StatusBadge status={request.status} />
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                </>
            )}
        </section>
    );
}

function ApprovalCard({
                          request,
                          formatAmount,
                      }: {
    request: BudgetRequest;
    formatAmount: (amount: number) => string;
}) {
    const date = new Date(request.createdAt).toLocaleDateString(
        "ko-KR",
    );

    return (
        <div className="rounded-2xl border border-zinc-200 bg-white p-5">
            <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                    <div className="flex h-10 w-10 items-center justify-center rounded-full bg-indigo-50 text-sm font-semibold text-indigo-600">
                        #{request.userId}
                    </div>

                    <div>
                        <p className="font-semibold text-zinc-900">
                            User #{request.userId}
                        </p>

                        <p className="mt-1 text-xs text-zinc-400">
                            {date}
                        </p>
                    </div>
                </div>

                <div className="text-right">
                    <p className="text-xl font-bold text-zinc-900">
                        {formatAmount(request.requestedAmount)}
                    </p>

                    <span className="mt-1 inline-flex rounded-full border border-amber-200 bg-amber-50 px-2 py-0.5 text-xs font-medium text-amber-600">
            검토 중
          </span>
                </div>
            </div>

            <div className="mt-5 rounded-xl bg-zinc-50 px-4 py-3">
                <p className="text-xs text-zinc-400">
                    신청 사유
                </p>

                <p className="mt-1 text-sm text-zinc-700">
                    {request.reason}
                </p>
            </div>

            <div className="mt-4 grid grid-cols-2 gap-2">
                <button
                    type="button"
                    disabled
                    className="rounded-xl bg-indigo-600 py-2.5 text-sm font-semibold text-white opacity-50"
                >
                    승인
                </button>

                <button
                    type="button"
                    disabled
                    className="rounded-xl border border-red-200 bg-red-50 py-2.5 text-sm font-semibold text-red-500 opacity-50"
                >
                    반려
                </button>
            </div>

            <p className="mt-2 text-center text-xs text-zinc-400">
                승인/반려 API 연결 예정
            </p>
        </div>
    );
}

function StatusBadge({ status }: { status: string }) {
    const isApproved = status === "APPROVED";

    return (
        <span
            className={`inline-flex w-fit rounded-full px-2.5 py-1 text-xs font-medium ${
                isApproved
                    ? "bg-emerald-50 text-emerald-600"
                    : "bg-red-50 text-red-500"
            }`}
        >
      {isApproved ? "승인됨" : "반려됨"}
    </span>
    );
}