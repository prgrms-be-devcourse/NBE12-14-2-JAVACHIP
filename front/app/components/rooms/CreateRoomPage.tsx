"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { ApiError } from "../../lib/api/types";
import {
  createRoom,
  type RoomCurrency,
} from "../../lib/api/roomsApis";

const currencies: { value: RoomCurrency; label: string; symbol: string }[] = [
  { value: "KRW", label: "원", symbol: "₩" },
  { value: "USD", label: "달러", symbol: "$" },
  { value: "JPY", label: "엔", symbol: "¥" },
];

function validate(name: string, budget: string) {
  const trimmedName = name.trim();
  const amount = Number(budget);

  if (!trimmedName) return "모임 이름을 입력해 주세요.";
  if (trimmedName.length > 20) return "모임 이름은 20자 이하여야 합니다.";
  if (!budget.trim()) return "총 예산을 입력해 주세요.";
  if (!Number.isSafeInteger(amount) || amount < 0) {
    return "총 예산은 0 이상의 정수로 입력해 주세요.";
  }

  return null;
}

export default function CreateRoomPage() {
  const router = useRouter();
  const [name, setName] = useState("");
  const [currency, setCurrency] = useState<RoomCurrency>("KRW");
  const [budget, setBudget] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const formError = validate(name, budget);
  const selectedCurrency = currencies.find(({ value }) => value === currency)!;

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (formError) {
      setError(formError);
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      const room = await createRoom({
        name: name.trim(),
        totalBudget: Number(budget),
        currency,
      });
      router.replace(`/dashboard?roomId=${room.id}`);
    } catch (caughtError) {
      if (!(caughtError instanceof ApiError && caughtError.status === 401)) {
        setError(
          caughtError instanceof ApiError
            ? caughtError.message
            : "모임을 만들지 못했습니다. 잠시 후 다시 시도해 주세요.",
        );
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="min-h-screen bg-[#f8f8fb] px-5 py-12 text-zinc-900 sm:px-8 sm:py-20">
      <div className="mx-auto w-full max-w-[532px]">
        <Link
          href="/rooms"
          className="inline-flex items-center gap-1 text-sm font-medium text-zinc-500 transition-colors hover:text-zinc-900"
        >
          <span aria-hidden>‹</span> 내 모임으로
        </Link>

        <header className="mt-8">
          <h1 className="text-3xl font-bold tracking-tight">새 모임 만들기</h1>
          <p className="mt-2 text-zinc-500">모임 이름과 예산을 설정하면 바로 시작할 수 있어요</p>
        </header>

        <form
          className="mt-10 rounded-2xl border border-zinc-200 bg-white p-7 shadow-sm sm:p-8"
          onSubmit={(event) => void handleSubmit(event)}
          noValidate
        >
          <div>
            <label htmlFor="room-name" className="text-sm font-bold">모임 이름</label>
            <input
              id="room-name"
              value={name}
              onChange={(event) => {
                setName(event.target.value);
                if (error) setError(null);
              }}
              disabled={submitting}
              maxLength={21}
              placeholder="예: 한양대 사진동아리 렌즈"
              className="mt-2 h-12 w-full rounded-xl border border-zinc-200 px-4 text-sm outline-none transition-colors placeholder:text-zinc-400 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100 disabled:bg-zinc-50"
            />
          </div>

          <fieldset className="mt-7">
            <legend className="text-sm font-bold">통화</legend>
            <div className="mt-2 grid grid-cols-3 gap-2">
              {currencies.map(({ value, label, symbol }) => (
                <button
                  key={value}
                  type="button"
                  onClick={() => {
                    setCurrency(value);
                    if (error) setError(null);
                  }}
                  disabled={submitting}
                  aria-pressed={currency === value}
                  className={`h-11 rounded-xl border text-sm font-semibold transition-colors disabled:cursor-not-allowed ${currency === value ? "border-indigo-600 bg-indigo-600 text-white" : "border-zinc-200 bg-white text-zinc-500 hover:border-indigo-200"}`}
                >
                  {symbol} {label}
                </button>
              ))}
            </div>
          </fieldset>

          <div className="mt-7">
            <label htmlFor="room-budget" className="text-sm font-bold">총 예산</label>
            <div className="relative mt-2">
              <input
                id="room-budget"
                value={budget}
                onChange={(event) => {
                  setBudget(event.target.value);
                  if (error) setError(null);
                }}
                disabled={submitting}
                inputMode="numeric"
                placeholder="0"
                className="h-12 w-full rounded-xl border border-zinc-200 px-4 pr-12 text-sm outline-none transition-colors placeholder:text-zinc-400 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100 disabled:bg-zinc-50"
              />
              <span className="pointer-events-none absolute inset-y-0 right-4 flex items-center text-sm font-medium text-zinc-500">{selectedCurrency.symbol}</span>
            </div>
          </div>

          {error && <p role="alert" className="mt-5 text-sm font-medium text-red-600">{error}</p>}

          <button
            type="submit"
            disabled={submitting || Boolean(formError)}
            className="mt-7 h-13 w-full rounded-xl bg-indigo-600 px-4 py-3 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-indigo-300"
          >
            {submitting ? "모임을 만드는 중..." : "모임 만들기"}
          </button>
        </form>

        <p className="mt-5 text-center text-sm text-zinc-400">모임을 만들면 자동으로 방장이 됩니다. 초대 링크로 멤버를 추가할 수 있어요.</p>
      </div>
    </main>
  );
}
