"use client";

import {Suspense, useCallback, useEffect, useState} from "react";
import {useParams} from "next/navigation";

import {
  getBudgetChange,
  getSettlementChanges,
  type BudgetChange,
  type BudgetChangeDetailResponse,
} from "@/app/lib/api/budgetChangeApi";

export default function SettlementsPage() {
  // return (
  //     <Suspense fallback={<LoadingState />}>
  //       <SettlementsContent />
  //     </Suspense>
  // );

    return (
        <SettlementsContent />
    );
}

function SettlementsContent() {

  // const roomIdParam = searchParams.get("roomId");
  // const roomId = roomIdParam ? Number(roomIdParam) : null;
    const { roomId } = useParams<{ roomId: string }>();

  const [changes, setChanges] = useState<BudgetChange[]>([]);
  const [selectedChange, setSelectedChange] =
      useState<BudgetChangeDetailResponse | null>(null);

  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadChanges = useCallback(async () => {
    if (!roomId || Number.isNaN(roomId)) {
      setError("모임 정보를 찾을 수 없습니다.");
      setLoading(false);
      return;
    }

    try {
      const response = await getSettlementChanges(Number(roomId));

      setChanges(response.changes);
      setError(null);
    } catch (caughtError) {
      console.error(caughtError);

      setError(
          caughtError instanceof Error
              ? caughtError.message
              : "정산 내역을 불러오지 못했습니다.",
      );
    } finally {
      setLoading(false);
    }
  }, [roomId]);

  useEffect(() => {
      // eslint-disable-next-line
      loadChanges();
  }, []);

  const selectChange = async (changeId: number) => {
    if (!roomId) {
      return;
    }

    try {
      setDetailLoading(true);
      setError(null);

      const detail = await getBudgetChange(Number(roomId), changeId);

      setSelectedChange(detail);
    } catch (caughtError) {
      console.error(caughtError);

      setError(
          caughtError instanceof Error
              ? caughtError.message
              : "정산 상세 정보를 불러오지 못했습니다.",
      );
    } finally {
      setDetailLoading(false);
    }
  };

  if (!roomId || Number.isNaN(roomId)) {
    return (
        <section className="mx-auto w-full max-w-5xl">
          <div className="rounded-2xl border border-zinc-200 bg-white p-8 text-center shadow-sm">
            <p className="text-sm text-zinc-500">
              모임 정보를 찾을 수 없습니다.
            </p>
          </div>
        </section>
    );
  }

  if (loading) {
    return <LoadingState />;
  }

  if (error && !selectedChange) {
    return (
        <ErrorState
            message={error}
            onRetry={() => void loadChanges()}
        />
    );
  }

  return (
      <section className="mx-auto w-full max-w-5xl">
        {/* 페이지 헤더 */}
        <header>
          <h1 className="text-2xl font-bold tracking-tight text-zinc-900">
            정산 내역
          </h1>

          <p className="mt-1 text-sm text-zinc-500">
            완료된 정산 내역과 실제 지출 금액을 확인합니다.
          </p>
        </header>

        {/* 정산 목록 + 상세 */}
        <div className="mt-8 grid gap-7 lg:grid-cols-[minmax(0,1.3fr)_minmax(280px,0.7fr)]">
          {/* 정산 목록 */}
          <section className="rounded-2xl border border-zinc-200 bg-white shadow-sm">
            <div className="flex items-center justify-between border-b border-zinc-100 px-6 py-5">
              <h2 className="font-bold text-zinc-900">
                정산 목록
              </h2>

              <span className="text-sm text-zinc-500">
              총 {changes.length}건
            </span>
            </div>

            {changes.length === 0 ? (
                <p className="px-6 py-14 text-center text-sm text-zinc-500">
                  아직 완료된 정산 내역이 없습니다.
                </p>
            ) : (
                <ul className="divide-y divide-zinc-100">
                  {changes.map((change) => (
                      <li key={change.id}>
                        <button
                            type="button"
                            onClick={() => void selectChange(change.id)}
                            className="flex w-full items-center justify-between gap-4 px-6 py-5 text-left transition hover:bg-zinc-50"
                        >
                          <div className="min-w-0">
                            <p className="truncate font-semibold text-zinc-900">
                              {change.reason || "정산 사유 없음"}
                            </p>

                            <p className="mt-1 text-sm text-zinc-500">
                              {change.userName} ·{" "}
                              {formatDate(change.processedAt)}
                            </p>
                          </div>

                          <div className="shrink-0 text-right">
                            <p className="font-bold text-zinc-900">
                              {formatAmount(change.changedBudget)}
                            </p>

                            <p className="mt-1 text-xs text-zinc-500">
                              {change.type}
                            </p>
                          </div>
                        </button>
                      </li>
                  ))}
                </ul>
            )}
          </section>

          {/* 정산 상세 */}
          <aside className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
            <h2 className="font-bold text-zinc-900">
              정산 상세
            </h2>

            {detailLoading ? (
                <p className="mt-6 text-sm text-zinc-500">
                  상세 정보를 불러오는 중입니다.
                </p>
            ) : selectedChange ? (
                <div className="mt-6">
                  {/* 금액 요약 */}
                  <div className="rounded-xl bg-zinc-50 p-4">
                    <div className="flex items-center justify-between">
                  <span className="text-sm text-zinc-500">
                    신청 금액
                  </span>

                      <span className="font-semibold text-zinc-900">
                    {formatAmount(selectedChange.changeBudget)}
                  </span>
                    </div>

                    <div className="mt-3 flex items-center justify-between">
                  <span className="text-sm text-zinc-500">
                    실제 지출
                  </span>

                      <span className="font-semibold text-zinc-900">
                    {formatAmount(selectedChange.changedBudget)}
                  </span>
                    </div>

                    <div className="my-4 border-t border-zinc-200" />

                    <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-zinc-600">
                    반환 금액
                  </span>

                      <span
                          className={`font-bold ${
                              getRefundAmount(selectedChange) > 0
                                  ? "text-indigo-600"
                                  : "text-zinc-900"
                          }`}
                      >
                    {formatAmount(
                        getRefundAmount(selectedChange),
                    )}
                  </span>
                    </div>
                  </div>

                  {/* 상세 정보 */}
                  <dl className="mt-6 space-y-4 text-sm">
                    <DetailItem
                        label="정산자"
                        value={selectedChange.userName}
                    />

                    <DetailItem
                        label="이메일"
                        value={selectedChange.userEmail}
                    />

                    <DetailItem
                        label="정산 사유"
                        value={selectedChange.reason || "-"}
                    />

                    <DetailItem
                        label="처리 일시"
                        value={formatDate(selectedChange.processedAt)}
                    />
                  </dl>
                </div>
            ) : (
                <p className="mt-6 text-sm text-zinc-500">
                  목록에서 정산 내역을 선택하면 상세 정보를 볼 수 있습니다.
                </p>
            )}

            {error && selectedChange && (
                <p className="mt-5 text-sm text-red-500">
                  {error}
                </p>
            )}
          </aside>
        </div>
      </section>
  );
}

function DetailItem({
                      label,
                      value,
                    }: {
  label: string;
  value: string;
}) {
  return (
      <div className="flex items-start justify-between gap-4">
        <dt className="shrink-0 text-zinc-500">
          {label}
        </dt>

        <dd className="text-right font-medium text-zinc-900">
          {value}
        </dd>
      </div>
  );
}

function LoadingState() {
  return (
      <section className="mx-auto w-full max-w-5xl">
        <div className="rounded-2xl border border-zinc-200 bg-white p-8 text-center shadow-sm">
          <p className="text-sm text-zinc-500">
            정산 내역을 불러오는 중입니다.
          </p>
        </div>
      </section>
  );
}

function ErrorState({
                      message,
                      onRetry,
                    }: {
  message: string;
  onRetry: () => void;
}) {
  return (
      <section className="mx-auto w-full max-w-5xl">
        <div className="rounded-2xl border border-zinc-200 bg-white p-8 text-center shadow-sm">
          <p className="text-sm text-red-500">
            {message}
          </p>

          <button
              type="button"
              onClick={onRetry}
              className="mt-4 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
          >
            다시 불러오기
          </button>
        </div>
      </section>
  );
}

function formatAmount(value: number) {
  return `${value.toLocaleString("ko-KR")}원`;
}

function formatDate(value: string) {
  const date = new Date(value);

  return Number.isNaN(date.getTime())
      ? value
      : date.toLocaleDateString("ko-KR");
}

function getRefundAmount(change: BudgetChangeDetailResponse) {
  return Math.max(
      change.changeBudget - change.changedBudget,
      0,
  );
}