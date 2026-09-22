"use client";

import Link from "next/link";
import { Suspense, useCallback, useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";

import {
  getBudget,
  getBudgetHistory,
  type BudgetHistoryItem,
  type Budget,
} from "../../lib/api/budgetApi";

export default function DashboardPage() {
  return (
    <Suspense fallback={<DashboardLoadingState />}>
      <DashboardContent />
    </Suspense>
  );
}

function DashboardContent() {
  const searchParams = useSearchParams();
  const roomIdParam = searchParams.get("roomId");
  const roomId = roomIdParam ? Number(roomIdParam) : null;

  const [budget, setBudget] = useState<Budget | null>(null);
  const [history, setHistory] = useState<BudgetHistoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadDashboard = useCallback(async () => {
    if (!roomId || Number.isNaN(roomId)) {
      setError("모임 정보를 찾을 수 없습니다.");
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const [budgetData, historyData] = await Promise.all([
        getBudget(roomId),
        getBudgetHistory(roomId),
      ]);

      setBudget(budgetData);
      setHistory(historyData.history);
    } catch (caughtError) {
      console.error("대시보드 조회 실패:", caughtError);
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "대시보드 정보를 불러오지 못했습니다.",
      );
    } finally {
      setLoading(false);
    }
  }, [roomId]);

  useEffect(() => {
    void Promise.resolve().then(loadDashboard);
  }, [loadDashboard]);

  if (loading) {
    return <DashboardLoadingState />;
  }

  if (error || !budget) {
    return (
        <section className="mx-auto w-full max-w-5xl">
          <div className="rounded-2xl border border-red-200 bg-white p-8 text-center shadow-sm">
            <p className="text-sm text-red-500">
              {error ?? "예산 정보를 불러오지 못했습니다."}
            </p>
            <button
                type="button"
                onClick={() => void loadDashboard()}
                className="mt-4 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
            >
              다시 불러오기
            </button>
          </div>
        </section>
    );
  }

  return (
      <section
          aria-labelledby="dashboard-title"
          className="mx-auto w-full max-w-5xl"
      >
        <header className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h1
                id="dashboard-title"
                className="text-2xl font-bold tracking-tight text-zinc-900"
            >
              대시보드
            </h1>

            <p className="mt-1 text-sm text-zinc-500">
              {budget.roomName}의 예산 현황
            </p>
          </div>
        </header>

        <article className="mt-8 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm sm:p-8">
          <div className="grid gap-7 sm:grid-cols-3 sm:gap-6">
            <SummaryItem
                label="총 예산"
                amount={formatMoney(budget.totalBudget, budget.currency)}
                tone="default"
            />

            <SummaryItem
                label="사용 가능"
                amount={formatMoney(budget.availableBudget, budget.currency)}
                description={`${getAvailablePercent(
                    budget.availableBudget,
                    budget.totalBudget,
                )}%`}
                tone="available"
            />

            <SummaryItem
                label="예약 중"
                amount={formatMoney(budget.reserveBudget, budget.currency)}
                description="승인된 미정산 요청"
                tone="reserved"
            />
          </div>

          <p className="mt-6 border-t border-zinc-200 pt-6 text-sm font-medium text-zinc-800">
            총 예산 {formatMoney(budget.totalBudget, budget.currency)}
            <span className="mx-2 text-zinc-400">→</span>

            <span className="text-indigo-500">
            사용 가능 {formatMoney(budget.availableBudget, budget.currency)}
          </span>

            <span className="mx-2 text-zinc-400">+</span>

            <span className="text-indigo-400">
            예약 중 {formatMoney(budget.reserveBudget, budget.currency)}
          </span>
          </p>
        </article>

        <article className="mt-7 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-bold">최근 예산 변동</h2>

            <Link
                href={`/settlements?roomId=${roomId}`}
                className="text-sm font-semibold text-indigo-500 hover:text-indigo-700"
            >
              전체 보기
            </Link>
          </div>

          {history.length === 0 ? (
              <p className="mt-5 rounded-xl bg-zinc-50 py-8 text-center text-sm text-zinc-500">
                아직 예산 변동 내역이 없습니다.
              </p>
          ) : (
              <ul className="mt-5 divide-y divide-zinc-100">
                {history.map((item) => (
                <li
                    key={item.id}
                    className="flex items-center justify-between gap-4 py-4"
                >
                  <div className="min-w-0">
                    <p className="truncate font-semibold text-zinc-800">
                      {item.reason}
                    </p>

                    <p className="mt-1 text-sm text-zinc-500">
                      {item.userName} · {formatDate(item.processedAt)}
                    </p>
                  </div>

                  <div className="shrink-0 text-right">
                    <p className="font-bold text-zinc-900">
                      {formatMoney(item.changedBudget, budget.currency)}
                    </p>

                    <p className="mt-1 text-sm text-zinc-500">
                      {item.type}
                    </p>
                  </div>
                </li>
                ))}
              </ul>
          )}
        </article>
      </section>
  );

  function SummaryItem({
                         label,
                         amount,
                         description,
                         tone,
                       }: {
    label: string;
    amount: string;
    description?: string;
    tone: "default" | "available" | "reserved";
  }) {
    const amountColor =
        tone === "available"
            ? "text-emerald-600"
            : tone === "reserved"
                ? "text-indigo-400"
                : "text-zinc-950";

    return (
        <div>
          <p className="text-sm font-medium text-zinc-500">
            {label}
          </p>

          <p
              className={`mt-1 text-3xl font-extrabold tracking-tight sm:text-4xl ${amountColor}`}
          >
            {amount}
          </p>

          {description && (
              <p className="mt-1 text-sm text-zinc-500">
                {description}
              </p>
          )}
        </div>
    );
  }

  function formatMoney(amount: number, currency: Budget["currency"]) {
    const symbols = {
      KRW: "₩",
      USD: "$",
      JPY: "¥",
    };

    return `${symbols[currency]}${amount.toLocaleString("ko-KR")}`;
  }

  function getAvailablePercent(
      available: number,
      total: number,
  ) {
    if (total === 0) return "0.0";

    return ((available / total) * 100).toFixed(1);
  }

  function formatDate(date: string) {
    return new Date(date).toLocaleDateString("ko-KR");
  }
}

function DashboardLoadingState() {
  return (
    <section className="mx-auto w-full max-w-5xl">
      <div className="rounded-2xl bg-white p-8 text-center shadow-sm">
        <p className="text-sm text-zinc-500">대시보드를 불러오는 중...</p>
      </div>
    </section>
  );
}
