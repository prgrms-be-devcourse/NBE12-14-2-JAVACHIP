"use client";

import { useCallback, useEffect, useState } from "react";
import type { FormEvent } from "react";
import { useParams } from "next/navigation";

import {
  createBudgetRequest,
  getBudgetRequests,
  type BudgetRequest,
} from "../../../../lib/api/budgetRequestApi";

import {
  getBudget,
  type Budget,
} from "../../../../lib/api/budgetApi";

export default function BudgetRequestsPage() {
  const { roomId: roomIdParam } = useParams<{ roomId: string }>();
  const roomId = Number(roomIdParam);

  const [budget, setBudget] = useState<Budget | null>(null);
  const [requests, setRequests] = useState<BudgetRequest[]>([]);

  const [amount, setAmount] = useState("");
  const [reason, setReason] = useState("");

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    if (!roomId || Number.isNaN(roomId)) {
      setError("모임 정보를 찾을 수 없습니다.");
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const [budgetData, requestData] = await Promise.all([
        getBudget(roomId),
        getBudgetRequests(roomId),
      ]);

      setBudget(budgetData);
      setRequests(requestData);
    } catch (error) {
      console.error("예산 신청 정보를 불러오지 못했습니다.", error);

      setError(
          error instanceof Error
              ? error.message
              : "예산 신청 정보를 불러오지 못했습니다.",
      );
    } finally {
      setLoading(false);
    }
  }, [roomId]);

  useEffect(() => {
    void Promise.resolve().then(loadData);
  }, [loadData]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!roomId || Number.isNaN(roomId)) {
      alert("모임 정보를 찾을 수 없습니다.");
      return;
    }

    const requestedAmount = Number(amount);

    if (!requestedAmount || requestedAmount <= 0) {
      alert("신청 금액을 입력해주세요.");
      return;
    }

    if (!reason.trim()) {
      alert("신청 사유를 입력해주세요.");
      return;
    }

    if (reason.length > 20) {
      alert("신청 사유는 20자 이하로 입력해주세요.");
      return;
    }

    try {
      setSubmitting(true);

      await createBudgetRequest(roomId, {
        reason: reason.trim(),
        requested_amount: requestedAmount,
      });

      setAmount("");
      setReason("");

      await loadData();

      alert("예산 신청이 완료되었습니다.");
    } catch (error) {
      console.error("예산 신청에 실패했습니다.", error);

      alert(
          error instanceof Error
              ? error.message
              : "예산 신청에 실패했습니다.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <LoadingState />;
  }

  if (error) {
    return (
        <section className="mx-auto max-w-5xl">
          <div className="rounded-2xl bg-white p-8 text-center shadow-sm">
            <p className="text-sm text-red-500">{error}</p>

            <button
                type="button"
                onClick={() => void loadData()}
                className="mt-4 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
            >
              다시 불러오기
            </button>
          </div>
        </section>
    );
  }

  return (
      <section className="mx-auto max-w-5xl space-y-6">
        {/* 제목 */}
        <div>
          <h1 className="text-2xl font-bold text-zinc-900">
            예산 신청
          </h1>

          <p className="mt-2 text-sm text-zinc-500">
            필요한 예산을 신청하고 승인 현황을 확인할 수 있습니다.
          </p>
        </div>

        {/* 현재 예산 */}
        {budget && (
            <div className="grid gap-4 sm:grid-cols-3">
              <BudgetCard
                  label="전체 예산"
                  value={budget.totalBudget}
                  currency={budget.currency}
              />

              <BudgetCard
                  label="사용 가능 예산"
                  value={budget.availableBudget}
                  currency={budget.currency}
              />

              <BudgetCard
                  label="예약 예산"
                  value={budget.reserveBudget}
                  currency={budget.currency}
              />
            </div>
        )}

        {/* 예산 신청 */}
        <div className="rounded-2xl bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold text-zinc-900">
            예산 신청하기
          </h2>

          <form
              onSubmit={handleSubmit}
              className="mt-5 space-y-5"
          >
            <div>
              <label
                  htmlFor="amount"
                  className="mb-2 block text-sm font-medium text-zinc-700"
              >
                신청 금액
              </label>

              <input
                  id="amount"
                  type="number"
                  min="1"
                  value={amount}
                  onChange={(event) => setAmount(event.target.value)}
                  placeholder="신청할 금액을 입력해주세요."
                  className="w-full rounded-xl border border-zinc-200 px-4 py-3 text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              />
            </div>

            <div>
              <div className="mb-2 flex items-center justify-between">
                <label
                    htmlFor="reason"
                    className="text-sm font-medium text-zinc-700"
                >
                  신청 사유
                </label>

                <span className="text-xs text-zinc-400">
                {reason.length}/20
              </span>
              </div>

              <input
                  id="reason"
                  type="text"
                  maxLength={20}
                  value={reason}
                  onChange={(event) => setReason(event.target.value)}
                  placeholder="예산 사용 목적을 입력해주세요."
                  className="w-full rounded-xl border border-zinc-200 px-4 py-3 text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              />
            </div>

            <button
                type="submit"
                disabled={submitting}
                className="w-full rounded-xl bg-indigo-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-zinc-300"
            >
              {submitting ? "신청 중..." : "예산 신청하기"}
            </button>
          </form>
        </div>

        {/* 신청 내역 */}
        <div className="rounded-2xl bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold text-zinc-900">
                예산 신청 내역
              </h2>

              <p className="mt-1 text-sm text-zinc-500">
                지금까지 신청한 예산 내역입니다.
              </p>
            </div>

            <span className="text-sm text-zinc-400">
            총 {requests.length}건
          </span>
          </div>

          {requests.length === 0 ? (
              <div className="mt-8 rounded-xl bg-zinc-50 py-10 text-center">
                <p className="text-sm text-zinc-500">
                  아직 예산 신청 내역이 없습니다.
                </p>
              </div>
          ) : (
              <div className="mt-5 space-y-3">
                {requests.map((request) => (
                    <RequestItem
                        key={request.id}
                        request={request}
                        currency={budget?.currency ?? "KRW"}
                    />
                ))}
              </div>
          )}
        </div>
      </section>
  );
}

function LoadingState() {
  return (
    <section className="mx-auto max-w-5xl">
      <div className="rounded-2xl bg-white p-8 text-center shadow-sm">
        <p className="text-sm text-zinc-500">
          예산 정보를 불러오는 중입니다...
        </p>
      </div>
    </section>
  );
}

function BudgetCard({
                      label,
                      value,
                      currency,
                    }: {
  label: string;
  value: number;
  currency: "KRW" | "USD" | "JPY";
}) {
  return (
      <div className="rounded-2xl bg-white p-5 shadow-sm">
        <p className="text-sm text-zinc-500">{label}</p>

        <p className="mt-2 text-2xl font-bold text-zinc-900">
          {formatMoney(value, currency)}
        </p>
      </div>
  );
}

function RequestItem({
                       request,
                       currency,
                     }: {
  request: BudgetRequest;
  currency: "KRW" | "USD" | "JPY";
}) {
  return (
      <div className="rounded-xl border border-zinc-100 p-4">
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <p className="font-semibold text-zinc-900">
              {request.reason}
            </p>

            <p className="mt-1 text-xs text-zinc-400">
              신청일 {formatDate(request.createdAt)}
            </p>
          </div>

          <p className="shrink-0 font-semibold text-zinc-900">
            {formatMoney(request.requestedAmount, currency)}
          </p>
        </div>

        <div className="mt-3 flex items-center justify-between">
          <StatusBadge status={request.status} />

          {request.rejectReason && (
              <span className="text-xs text-red-500">
            반려 사유: {request.rejectReason}
          </span>
          )}
        </div>
      </div>
  );
}

function StatusBadge({
                       status,
                     }: {
  status: string;
}) {
  const statusMap: Record<
      string,
      {
        label: string;
        className: string;
      }
  > = {
    PENDING: {
      label: "승인 대기",
      className: "bg-amber-50 text-amber-700",
    },
    APPROVED: {
      label: "승인",
      className: "bg-emerald-50 text-emerald-700",
    },
    REJECTED: {
      label: "반려",
      className: "bg-red-50 text-red-700",
    },
    SETTLED: {
      label: "정산 완료",
      className: "bg-zinc-100 text-zinc-600",
    },
  };

  const current = statusMap[status] ?? {
    label: status,
    className: "bg-zinc-100 text-zinc-600",
  };

  return (
      <span
          className={`rounded-full px-2.5 py-1 text-xs font-medium ${current.className}`}
      >
      {current.label}
    </span>
  );
}

function formatMoney(
    value: number,
    currency: "KRW" | "USD" | "JPY",
) {
  const currencyMap = {
    KRW: "₩",
    USD: "$",
    JPY: "¥",
  };

  return `${currencyMap[currency]}${value.toLocaleString("ko-KR")}`;
}

function formatDate(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleDateString("ko-KR");
}
