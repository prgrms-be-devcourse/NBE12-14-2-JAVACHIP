"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { getRoom, type Room } from "../../lib/api/roomsApis";
import { ApiError } from "../../lib/api/types";

function formatBudget(amount: number, currency: string) {
  const formatted = new Intl.NumberFormat("ko-KR").format(amount);
  return currency === "KRW" ? `${formatted}원` : `${currency} ${formatted}`;
}

function formatCreatedAt(createdAt: string) {
  const date = new Date(createdAt);

  return Number.isNaN(date.getTime())
    ? createdAt
    : new Intl.DateTimeFormat("ko-KR", {
        year: "numeric",
        month: "long",
        day: "numeric",
      }).format(date);
}

function getRoomId(value: string | string[] | undefined) {
  if (typeof value !== "string" || !/^\d+$/.test(value)) {
    return null;
  }

  const roomId = Number(value);
  return Number.isSafeInteger(roomId) && roomId > 0 ? roomId : null;
}

export default function RoomDetailPage() {
  const { roomId: roomIdParam } = useParams<{ roomId?: string | string[] }>();
  const roomId = useMemo(() => getRoomId(roomIdParam), [roomIdParam]);
  const [room, setRoom] = useState<Room | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(roomId !== null);
  const [redirectingToLogin, setRedirectingToLogin] = useState(false);

  const loadRoom = async () => {
    if (roomId === null) return;

    setLoading(true);
    setError(null);
    setRedirectingToLogin(false);

    try {
      setRoom(await getRoom(roomId));
    } catch (caughtError) {
      if (caughtError instanceof ApiError && caughtError.status === 401) {
        setRedirectingToLogin(true);
        return;
      }

      setError(
        caughtError instanceof ApiError
          ? caughtError.message
          : "모임 정보를 불러오지 못했습니다.",
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (roomId === null) {
      return;
    }

    let cancelled = false;

    getRoom(roomId)
      .then((loadedRoom) => {
        if (!cancelled) {
          setRoom(loadedRoom);
        }
      })
      .catch((caughtError: unknown) => {
        if (cancelled) {
          return;
        }

        if (caughtError instanceof ApiError && caughtError.status === 401) {
          setRedirectingToLogin(true);
          return;
        }

        setError(
          caughtError instanceof ApiError
            ? caughtError.message
            : "모임 정보를 불러오지 못했습니다.",
        );
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [roomId]);

  if (roomId === null) {
    return (
      <main className="min-h-screen bg-[#f8f8fb] px-5 py-12 text-zinc-900 sm:px-8 sm:py-20">
        <section className="mx-auto max-w-xl rounded-2xl border border-zinc-200 bg-white px-6 py-14 text-center shadow-sm">
          <h1 className="text-xl font-bold">잘못된 모임 주소입니다.</h1>
          <p className="mt-2 text-sm text-zinc-500">모임 목록에서 다시 선택해 주세요.</p>
          <Link href="/rooms" className="mt-6 inline-flex rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700">내 모임으로 돌아가기</Link>
        </section>
      </main>
    );
  }

  if (loading || redirectingToLogin) {
    return (
      <main className="min-h-screen bg-[#f8f8fb] px-5 py-12 text-zinc-900 sm:px-8 sm:py-20">
        <p className="text-center text-sm text-zinc-500">{redirectingToLogin ? "로그인 페이지로 이동하고 있습니다." : "모임 정보를 불러오는 중입니다."}</p>
      </main>
    );
  }

  if (error || !room) {
    return (
      <main className="min-h-screen bg-[#f8f8fb] px-5 py-12 text-zinc-900 sm:px-8 sm:py-20">
        <section className="mx-auto max-w-xl rounded-2xl border border-red-200 bg-red-50 px-6 py-14 text-center">
          <h1 className="text-lg font-bold text-red-700">모임 정보를 불러오지 못했습니다.</h1>
          <p className="mt-2 text-sm text-red-600">{error ?? "잠시 후 다시 시도해 주세요."}</p>
          <div className="mt-6 flex justify-center gap-2"><button type="button" onClick={() => void loadRoom()} className="rounded-xl border border-red-200 bg-white px-4 py-2.5 text-sm font-semibold text-red-600">다시 시도</button><Link href="/rooms" className="rounded-xl border border-zinc-200 bg-white px-4 py-2.5 text-sm font-semibold text-zinc-700">내 모임으로</Link></div>
        </section>
      </main>
    );
  }

  const usedBudget = Math.max(0, room.totalBudget - room.availableBudget);
  const usedRate = room.totalBudget > 0
    ? Math.max(0, Math.min(100, (usedBudget / room.totalBudget) * 100))
    : 0;
  const availableRate = 100 - usedRate;

  return (
    <main className="min-h-screen bg-[#f8f8fb] px-5 py-12 text-zinc-900 sm:px-8 sm:py-20">
      <div className="mx-auto w-full max-w-3xl">
        <Link href="/rooms" className="inline-flex items-center gap-1 text-sm font-medium text-zinc-500 transition-colors hover:text-zinc-900"><span aria-hidden>‹</span> 내 모임으로</Link>

        <header className="mt-8 flex flex-wrap items-start justify-between gap-5">
          <div className="flex min-w-0 items-center gap-4"><span className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-indigo-50 text-xl font-bold text-indigo-500">{room.name.charAt(0)}</span><div><p className="text-sm font-medium text-zinc-500">{room.currency} · {formatCreatedAt(room.createdAt)} 생성</p><h1 className="mt-1 truncate text-3xl font-bold tracking-tight">{room.name}</h1></div></div>
          <Link href={`/dashboard?roomId=${room.id}`} className="rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-indigo-700">이 모임 열기</Link>
        </header>

        <section className="mt-10 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm sm:p-8" aria-labelledby="budget-title">
          <div className="flex flex-wrap items-center justify-between gap-4"><div><p className="text-sm font-medium text-zinc-500">예산 현황</p><h2 id="budget-title" className="mt-1 text-xl font-bold">{room.name}의 예산</h2></div><span className="rounded-full bg-indigo-50 px-3 py-1.5 text-sm font-semibold text-indigo-600">사용률 {usedRate.toFixed(1)}%</span></div>
          <dl className="mt-8 grid gap-6 sm:grid-cols-3"><div><dt className="text-sm text-zinc-500">총 예산</dt><dd className="mt-2 text-2xl font-extrabold tracking-tight">{formatBudget(room.totalBudget, room.currency)}</dd></div><div><dt className="text-sm text-zinc-500">사용 금액</dt><dd className="mt-2 text-2xl font-extrabold tracking-tight text-indigo-600">{formatBudget(usedBudget, room.currency)}</dd></div><div><dt className="text-sm text-zinc-500">사용 가능</dt><dd className="mt-2 text-2xl font-extrabold tracking-tight text-emerald-600">{formatBudget(room.availableBudget, room.currency)}</dd></div></dl>
          <div className="mt-9"><div className="flex h-3 overflow-hidden rounded-full bg-zinc-100" aria-label={`${room.name} 예산 사용 현황`}><span className="bg-indigo-500" style={{ width: `${usedRate}%` }} /><span className="bg-emerald-100" style={{ width: `${availableRate}%` }} /></div><div className="mt-3 flex flex-wrap gap-x-4 gap-y-2 text-sm text-zinc-500"><span className="flex items-center gap-1.5"><i className="h-2.5 w-2.5 rounded-full bg-indigo-500" />사용 금액</span><span className="flex items-center gap-1.5"><i className="h-2.5 w-2.5 rounded-full bg-emerald-300" />사용 가능</span></div></div>
        </section>
      </div>
    </main>
  );
}
