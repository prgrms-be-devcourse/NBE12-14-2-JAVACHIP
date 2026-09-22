"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";

import {
    getBudget,
    type Budget,
} from "../../lib/api/budgetApi";
import {
    getBudgetRequests,
    type BudgetRequest,
} from "../../lib/api/budgetRequestApi";
import {
    createBudgetChange,
    getSettlementChanges,
    type BudgetChange,
} from "../../lib/api/budgetChangeApi";
import { ApiError } from "../../lib/api/types";

const settlementSteps = [
    "승인된 예산 신청 건을 선택하세요",
    "실제 지출한 금액을 입력하세요",
    "차액이 있으면 예산으로 자동 반환돼요",
    "정산 내역은 모든 멤버가 볼 수 있어요",
];

function formatBudget(amount: number, currency: string) {
    const formatted = new Intl.NumberFormat("ko-KR").format(amount);

    return currency === "KRW"
        ? `${formatted}원`
        : `${currency} ${formatted}`;
}

function getErrorMessage(error: unknown) {
    if (error instanceof ApiError && error.status === 401) {
        return null;
    }

    return error instanceof ApiError
        ? error.message
        : "정산 정보를 불러오지 못했습니다.";
}

function formatDate(date: string) {
    return date.slice(0, 10);
}

export default function SettlementPage() {
    /*
     * URL:
     * /rooms/1/settlements
     *
     * 폴더가 [id]라면 params.id
     * 폴더가 [roomId]라면 params.roomId
     *
     * 둘 다 대응하도록 작성
     */
    const params = useParams();

    const roomId = Number(
        params.roomId ?? params.id,
    );

    const [budget, setBudget] = useState<Budget | null>(null);
    const [requests, setRequests] = useState<BudgetRequest[]>([]);
    const [changes, setChanges] = useState<BudgetChange[]>([]);

    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const [selectedRequestId, setSelectedRequestId] = useState(0);
    const [spentAmount, setSpentAmount] = useState("");
    const [description, setDescription] = useState("");

    /*
     * 승인된 예산 신청만 사용
     */
    const approvedRequests = requests.filter(
        (request) =>
            request.status.toUpperCase() === "APPROVED",
    );

    /*
     * 현재 선택된 승인 요청
     */
    const selectedRequest =
        approvedRequests.find(
            (request) => request.id === selectedRequestId,
        ) ?? approvedRequests[0];

    /*
     * 방 예산에서 가져온 통화
     */
    const currency = budget?.currency ?? "KRW";

    /*
     * 실제 지출 금액
     */
    const spent =
        Number(spentAmount.replace(/,/g, "")) || 0;

    /*
     * 선택한 예산 신청의 승인 금액
     */
    const approvedAmount =
        selectedRequest?.requestedAmount ?? 0;

    /*
     * 반환 금액
     *
     * 승인 금액 - 실제 지출 금액
     */
    const refundAmount = Math.max(
        approvedAmount - spent,
        0,
    );

    /*
     * 정산 데이터 조회
     */
    const loadSettlementData = async () => {
        if (!Number.isFinite(roomId) || roomId <= 0) {
            setError("올바른 모임 정보가 없습니다.");
            setLoading(false);
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const [
                budgetData,
                requestData,
                changeData,
            ] = await Promise.all([
                /*
                 * 현재 방 예산
                 *
                 * GET /rooms/{roomId}/budget
                 */
                getBudget(roomId),

                /*
                 * 예산 신청 목록
                 */
                getBudgetRequests(roomId),

                /*
                 * 정산 내역
                 */
                getSettlementChanges(roomId),
            ]);

            setBudget(budgetData);
            setRequests(requestData);

            /*
             * BudgetChangeListResponse
             * {
             *   changes: BudgetChange[]
             * }
             */
            setChanges(changeData.changes);

            /*
             * 승인된 신청 중 첫 번째를 기본 선택
             */
            const approved = requestData.filter(
                (request) =>
                    request.status.toUpperCase() === "APPROVED",
            );

            setSelectedRequestId(
                approved[0]?.id ?? 0,
            );
        } catch (caughtError) {
            setError(getErrorMessage(caughtError));
        } finally {
            setLoading(false);
        }
    };

    /*
     * 최초 페이지 진입 시 데이터 조회
     */
    useEffect(() => {
        let cancelled = false;

        const load = async () => {
            if (!Number.isFinite(roomId) || roomId <= 0) {
                setError("올바른 모임 정보가 없습니다.");
                setLoading(false);
                return;
            }

            setLoading(true);
            setError(null);

            try {
                const [
                    budgetData,
                    requestData,
                    changeData,
                ] = await Promise.all([
                    getBudget(roomId),
                    getBudgetRequests(roomId),
                    getSettlementChanges(roomId),
                ]);

                if (cancelled) {
                    return;
                }

                setBudget(budgetData);
                setRequests(requestData);
                setChanges(changeData.changes);

                const approved = requestData.filter(
                    (request) =>
                        request.status.toUpperCase() === "APPROVED",
                );

                setSelectedRequestId(
                    approved[0]?.id ?? 0,
                );
            } catch (caughtError) {
                if (!cancelled) {
                    setError(getErrorMessage(caughtError));
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        };

        load();

        return () => {
            cancelled = true;
        };
    }, [roomId]);

    /*
     * 정산 완료
     */
    const handleSubmit = async () => {
        if (!selectedRequest) {
            return;
        }

        if (spent <= 0) {
            setError("실제 지출 금액을 입력해주세요.");
            return;
        }

        if (spent > selectedRequest.requestedAmount) {
            setError(
                "실제 지출 금액은 승인 금액을 초과할 수 없습니다.",
            );
            return;
        }

        setSubmitting(true);
        setError(null);

        try {
            await createBudgetChange(
                roomId,
                selectedRequest.id,
                {
                    /*
                     * 실제 정산 금액
                     */
                    changedBudget: spent,

                    /*
                     * BudgetChangeCreateRequest에서
                     * reason은 string 타입이므로
                     * undefined를 넣지 않음
                     */
                    reason: description,
                },
            );

            setSpentAmount("");
            setDescription("");

            /*
             * 정산 완료 후
             * 예산 / 신청 / 정산 내역 다시 조회
             */
            await loadSettlementData();
        } catch (caughtError) {
            setError(getErrorMessage(caughtError));
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <section
            aria-labelledby="settlement-title"
            className="mx-auto w-full max-w-5xl"
        >
            {/* 제목 */}
            <header>
                <h1
                    id="settlement-title"
                    className="text-2xl font-bold tracking-tight text-zinc-900"
                >
                    정산하기
                </h1>

                <p className="mt-1 text-sm text-zinc-500">
                    승인된 예산을 실제 지출 내역과 함께 정산하세요
                </p>
            </header>

            {/* 로딩 */}
            {loading && (
                <div className="mt-4 rounded-xl border border-zinc-200 bg-white px-4 py-3 text-sm text-zinc-500">
                    정산 정보를 불러오는 중...
                </div>
            )}

            {/* 에러 */}
            {error && (
                <div className="mt-4 flex items-center justify-between gap-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
                    <span>{error}</span>

                    <button
                        type="button"
                        onClick={loadSettlementData}
                        className="shrink-0 rounded-lg border border-red-200 bg-white px-3 py-2 text-sm font-semibold text-red-600"
                    >
                        다시 시도
                    </button>
                </div>
            )}

            {/* roomId가 잘못된 경우 */}
            {!loading && !error && !budget && (
                <div className="mt-4 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700">
                    해당하는 모임을 찾을 수 없습니다.
                </div>
            )}

            <div className="mt-6 grid max-w-4xl gap-6 lg:grid-cols-[minmax(0,1.55fr)_minmax(280px,1fr)]">
                {/* ========================= */}
                {/* 왼쪽 - 새 정산 */}
                {/* ========================= */}

                <form
                    onSubmit={(event) => {
                        event.preventDefault();
                        handleSubmit();
                    }}
                    className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-6"
                >
                    <h2 className="text-lg font-bold text-zinc-800">
                        새 정산
                    </h2>

                    {/* 승인된 예산 신청 */}
                    <div className="mt-4">
                        <label
                            htmlFor="request"
                            className="text-sm font-semibold text-zinc-800"
                        >
                            승인된 예산 신청
                        </label>

                        {approvedRequests.length > 0 ? (
                            <select
                                id="request"
                                value={selectedRequestId}
                                onChange={(event) =>
                                    setSelectedRequestId(
                                        Number(event.target.value),
                                    )
                                }
                                className="mt-2 h-12 w-full rounded-xl border border-zinc-200 bg-white px-4 text-sm text-zinc-900 outline-none transition focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100"
                            >
                                {approvedRequests.map(
                                    (request) => (
                                        <option
                                            key={request.id}
                                            value={request.id}
                                        >
                                            {request.reason} ·{" "}
                                            {formatBudget(
                                                request.requestedAmount,
                                                currency,
                                            )}
                                        </option>
                                    ),
                                )}
                            </select>
                        ) : (
                            <div className="mt-2 rounded-xl border border-dashed border-zinc-300 bg-zinc-50 px-4 py-4 text-sm text-zinc-500">
                                정산할 수 있는 승인된 예산 신청이 없습니다.
                            </div>
                        )}
                    </div>

                    {/* 승인 금액 */}
                    <div className="mt-4 rounded-xl bg-indigo-50 p-4">
                        <div className="flex items-center justify-between">
                            <span className="text-sm font-medium text-indigo-600">
                                승인 금액
                            </span>

                            <span className="text-lg font-extrabold text-indigo-700">
                                {selectedRequest
                                    ? formatBudget(
                                        selectedRequest.requestedAmount,
                                        currency,
                                    )
                                    : "-"}
                            </span>
                        </div>
                    </div>

                    {/* 실제 지출 금액 */}
                    <div className="mt-4">
                        <label
                            htmlFor="spentAmount"
                            className="text-sm font-semibold text-zinc-800"
                        >
                            실제 지출 금액
                        </label>

                        <div className="mt-2 flex h-12 items-center rounded-xl border border-zinc-200 px-4 transition focus-within:border-indigo-400 focus-within:ring-2 focus-within:ring-indigo-100">
                            <input
                                id="spentAmount"
                                inputMode="numeric"
                                value={spentAmount}
                                onChange={(event) => {
                                    const value =
                                        event.target.value.replace(
                                            /[^0-9]/g,
                                            "",
                                        );

                                    setSpentAmount(value);
                                }}
                                placeholder="0"
                                className="min-w-0 flex-1 bg-transparent text-base text-zinc-900 outline-none placeholder:text-zinc-400"
                            />

                            <span className="text-sm font-medium text-zinc-500">
                                {currency === "KRW"
                                    ? "원"
                                    : currency}
                            </span>
                        </div>
                    </div>

                    {/* 지출 내용 */}
                    <div className="mt-4">
                        <label
                            htmlFor="description"
                            className="text-sm font-semibold text-zinc-800"
                        >
                            지출 내용
                        </label>

                        <textarea
                            id="description"
                            rows={3}
                            value={description}
                            onChange={(event) =>
                                setDescription(
                                    event.target.value,
                                )
                            }
                            placeholder="예: 온사인관에서 사진 30장 인화 및 액자 제작"
                            className="mt-2 w-full resize-none rounded-xl border border-zinc-200 px-4 py-3 text-sm text-zinc-900 outline-none placeholder:text-zinc-400 focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100"
                        />
                    </div>

                    {/* 반환 금액 */}
                    <div className="mt-4 flex items-center justify-between rounded-xl border border-emerald-100 bg-emerald-50 px-4 py-3">
                        <span className="text-sm font-medium text-emerald-700">
                            예산 반환 금액
                        </span>

                        <span className="text-base font-extrabold text-emerald-700">
                            {formatBudget(
                                refundAmount,
                                currency,
                            )}
                        </span>
                    </div>

                    {/* 정산 버튼 */}
                    <button
                        type="submit"
                        disabled={
                            submitting ||
                            !selectedRequest ||
                            approvedRequests.length === 0
                        }
                        className="mt-4 h-12 w-full rounded-xl bg-indigo-600 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-indigo-500/55"
                    >
                        {submitting
                            ? "정산 처리 중..."
                            : "정산 완료하기"}
                    </button>
                </form>

                {/* ========================= */}
                {/* 오른쪽 - 예산 / 절차 */}
                {/* ========================= */}

                <div className="space-y-4">
                    {/* 현재 방 예산 */}
                    <article className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
                        <div className="flex items-center justify-between">
                            <h2 className="text-sm font-bold text-zinc-500">
                                현재 방 예산
                            </h2>

                            {budget && (
                                <span className="text-xs font-medium text-zinc-400">
                                    {budget.currency}
                                </span>
                            )}
                        </div>

                        {budget ? (
                            <div className="mt-4 space-y-3">
                                {/* 총 예산 */}
                                <div className="rounded-xl bg-zinc-50 px-4 py-3">
                                    <div className="flex items-center justify-between">
                                        <span className="text-sm font-medium text-zinc-500">
                                            총 예산
                                        </span>

                                        <span className="text-lg font-extrabold text-zinc-900">
                                            {formatBudget(
                                                budget.totalBudget,
                                                budget.currency,
                                            )}
                                        </span>
                                    </div>
                                </div>

                                {/* 가용 예산 */}
                                <div className="rounded-xl bg-emerald-50 px-4 py-3">
                                    <div className="flex items-center justify-between">
                                        <span className="text-sm font-medium text-emerald-700">
                                            가용 예산
                                        </span>

                                        <span className="text-lg font-extrabold text-emerald-600">
                                            {formatBudget(
                                                budget.availableBudget,
                                                budget.currency,
                                            )}
                                        </span>
                                    </div>
                                </div>

                                {/* 예비 예산 */}
                                <div className="rounded-xl bg-indigo-50 px-4 py-3">
                                    <div className="flex items-center justify-between">
                                        <span className="text-sm font-medium text-indigo-700">
                                            예약 예산
                                        </span>

                                        <span className="text-lg font-extrabold text-indigo-600">
                                            {formatBudget(
                                                budget.reserveBudget,
                                                budget.currency,
                                            )}
                                        </span>
                                    </div>
                                </div>

                                {/* 모임 이름 */}
                                <div className="border-t border-zinc-200 pt-3">
                                    <div className="flex items-center justify-between">
                                        <span className="text-sm font-semibold text-zinc-800">
                                            모임
                                        </span>

                                        <span className="text-sm font-semibold text-zinc-700">
                                            {budget.roomName}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        ) : (
                            <div className="mt-4 rounded-xl bg-zinc-50 px-4 py-6 text-center text-sm text-zinc-400">
                                예산 정보를 불러오는 중입니다.
                            </div>
                        )}
                    </article>

                    {/* 정산 절차 */}
                    <article className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
                        <h2 className="text-sm font-bold text-indigo-600">
                            정산 절차 안내
                        </h2>

                        <ol className="mt-3 space-y-2.5">
                            {settlementSteps.map(
                                (step, index) => (
                                    <li
                                        key={step}
                                        className="flex items-center gap-2 text-sm font-medium text-indigo-500"
                                    >
                                        <span className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-indigo-600 text-xs font-bold text-white">
                                            {index + 1}
                                        </span>

                                        {step}
                                    </li>
                                ),
                            )}
                        </ol>
                    </article>
                </div>
            </div>

            {/* ========================= */}
            {/* 정산 내역 */}
            {/* ========================= */}

            <section
                aria-labelledby="settlements-title"
                className="mt-7 max-w-4xl"
            >
                <h2
                    id="settlements-title"
                    className="text-lg font-bold text-zinc-800"
                >
                    정산 내역
                </h2>

                <div className="mt-3 overflow-x-auto rounded-2xl border border-zinc-200 bg-white shadow-sm">
                    <table className="min-w-[760px] w-full text-left text-sm">
                        <thead className="border-b border-zinc-200 bg-zinc-50 text-zinc-500">
                        <tr>
                            <th className="px-5 py-3 font-medium">
                                정산일
                            </th>

                            <th className="px-5 py-3 font-medium">
                                내용
                            </th>

                            <th className="px-5 py-3 font-medium">
                                정산자
                            </th>

                            <th className="px-5 py-3 text-right font-medium">
                                승인 금액
                            </th>

                            <th className="px-5 py-3 text-right font-medium">
                                지출 금액
                            </th>

                            <th className="px-5 py-3 text-right font-medium">
                                반환 금액
                            </th>
                        </tr>
                        </thead>

                        <tbody>
                        {changes.length > 0 ? (
                            changes.map((change) => {
                                /*
                                 * changeBudget
                                 * = 승인 금액
                                 */
                                const approvedAmount =
                                    change.changeBudget;

                                /*
                                 * changedBudget
                                 * = 실제 정산 금액
                                 */
                                const spentAmount =
                                    change.changedBudget;

                                /*
                                 * 반환 금액
                                 * = 승인 금액 - 실제 정산 금액
                                 */
                                const refundAmount =
                                    Math.max(
                                        approvedAmount -
                                        spentAmount,
                                        0,
                                    );

                                return (
                                    <tr
                                        key={change.id}
                                        className="border-b border-zinc-100 text-zinc-800 last:border-b-0"
                                    >
                                        <td className="px-5 py-4 text-zinc-500">
                                            {change.processedAt
                                                ? formatDate(
                                                    change.processedAt,
                                                )
                                                : "-"}
                                        </td>

                                        <td className="px-5 py-4 font-semibold">
                                            {change.reason || "-"}
                                        </td>

                                        <td className="px-5 py-4 text-zinc-600">
                                            {change.userName || "-"}
                                        </td>

                                        <td className="px-5 py-4 text-right font-semibold">
                                            {formatBudget(
                                                approvedAmount,
                                                currency,
                                            )}
                                        </td>

                                        <td className="px-5 py-4 text-right font-semibold">
                                            {formatBudget(
                                                spentAmount,
                                                currency,
                                            )}
                                        </td>

                                        <td className="px-5 py-4 text-right">
                                            {refundAmount > 0 ? (
                                                <span className="font-semibold text-emerald-600">
                                                        +
                                                    {formatBudget(
                                                        refundAmount,
                                                        currency,
                                                    )}
                                                    </span>
                                            ) : (
                                                <span className="text-zinc-400">
                                                        −
                                                    </span>
                                            )}
                                        </td>
                                    </tr>
                                );
                            })
                        ) : (
                            <tr>
                                <td
                                    colSpan={6}
                                    className="px-5 py-12 text-center text-sm text-zinc-500"
                                >
                                    아직 정산 내역이 없습니다.
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </section>
        </section>
    );
}