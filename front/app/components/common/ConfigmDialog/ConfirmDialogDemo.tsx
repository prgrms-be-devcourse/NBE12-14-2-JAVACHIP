"use client";

import { useEffect, useRef, useState } from "react";
import ConfirmDialog from "./ConfirmDialog";

type DemoMode = "default" | "danger" | "typed" | null;

const dialogContents = {
  default: {
    title: "예산 신청을 취소할까요?",
    description: "취소한 신청은 다시 복구할 수 없습니다.",
    confirmLabel: "신청 취소",
    variant: "default" as const,
  },
  danger: {
    title: "모임을 삭제할까요?",
    description: "모임의 멤버, 예산 신청, 정산 내역, 초대 링크가 모두 삭제됩니다.",
    confirmLabel: "삭제하기",
    variant: "danger" as const,
  },
  typed: {
    title: "강한 삭제 확인",
    description: "삭제를 진행하려면 아래 입력칸에 안내 문구를 정확히 입력해 주세요.",
    confirmLabel: "삭제 확인",
    variant: "danger" as const,
  },
};

export default function ConfirmDialogDemo() {
  const [mode, setMode] = useState<DemoMode>(null);
  const [confirmationText, setConfirmationText] = useState("");
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState("아직 확인한 동작이 없습니다.");
  const timerRef = useRef<number | null>(null);

  useEffect(() => () => {
    if (timerRef.current !== null) {
      window.clearTimeout(timerRef.current);
    }
  }, []);

  const openDialog = (nextMode: Exclude<DemoMode, null>) => {
    setConfirmationText("");
    setMode(nextMode);
    setResult(`${nextMode === "default" ? "일반" : nextMode === "danger" ? "위험" : "강한 삭제 확인"} 다이얼로그를 열었습니다.`);
  };

  const closeDialog = () => {
    if (loading || mode === null) return;

    setResult("확인 동작을 취소했습니다.");
    setMode(null);
    setConfirmationText("");
  };

  const confirmDialog = () => {
    if (mode === null || loading) return;

    setLoading(true);
    setResult("가짜 처리 상태를 확인하는 중입니다. 이 동안 다이얼로그는 닫을 수 없습니다.");
    timerRef.current = window.setTimeout(() => {
      setLoading(false);
      setMode(null);
      setConfirmationText("");
      setResult("확인 동작이 완료되었습니다.");
      timerRef.current = null;
    }, 1500);
  };

  const dialog = mode === null ? null : dialogContents[mode];
  const confirmDisabled = mode === "typed" && confirmationText.trim() !== "모임 삭제";

  return (
    <main className="min-h-screen bg-[#f8f8fb] px-5 py-12 text-zinc-900 sm:px-8 sm:py-20">
      <section className="mx-auto max-w-3xl">
        <header>
          <p className="text-sm font-semibold text-indigo-600">Component demo</p>
          <h1 className="mt-2 text-3xl font-bold tracking-tight">ConfirmDialog</h1>
          <p className="mt-3 text-zinc-600">실제 API 호출 없이 다이얼로그의 주요 상태와 키보드 동작을 확인할 수 있습니다.</p>
        </header>

        <div className="mt-10 grid gap-5 sm:grid-cols-3">
          <article className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm"><h2 className="font-bold">기본 확인</h2><p className="mt-2 text-sm leading-6 text-zinc-500">일반적인 취소·확인 행동에 사용하는 인디고 variant입니다.</p><button type="button" onClick={() => openDialog("default")} className="mt-5 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700">기본 확인 열기</button></article>
          <article className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm"><h2 className="font-bold">위험 확인</h2><p className="mt-2 text-sm leading-6 text-zinc-500">삭제처럼 되돌리기 어려운 행동에 사용하는 danger variant입니다.</p><button type="button" onClick={() => openDialog("danger")} className="mt-5 rounded-xl bg-red-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-red-700">위험 확인 열기</button></article>
          <article className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm"><h2 className="font-bold">추가 확인</h2><p className="mt-2 text-sm leading-6 text-zinc-500">children과 confirmDisabled로 이름 재입력 확인을 재현합니다.</p><button type="button" onClick={() => openDialog("typed")} className="mt-5 rounded-xl border border-red-200 bg-red-50 px-4 py-2.5 text-sm font-semibold text-red-600 hover:bg-red-100">강한 확인 열기</button></article>
        </div>

        <section className="mt-8 rounded-2xl border border-indigo-200 bg-indigo-50 p-5" aria-live="polite"><h2 className="text-sm font-bold text-indigo-700">최근 결과</h2><p className="mt-2 text-sm text-indigo-600">{result}</p></section>

        <section className="mt-8 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm"><h2 className="font-bold">직접 확인해 보세요</h2><ul className="mt-3 list-disc space-y-1 pl-5 text-sm leading-6 text-zinc-600"><li>다이얼로그를 연 뒤 Escape 키와 배경 클릭으로 닫아 보세요.</li><li>Tab과 Shift+Tab으로 다이얼로그 안에서 포커스가 순환하는지 확인하세요.</li><li>확인 버튼을 누른 뒤 1.5초간 취소·확인·닫기 동작이 잠기는지 확인하세요.</li><li>강한 확인에서는 <span className="font-semibold">모임 삭제</span>를 입력해야 확인 버튼이 활성화됩니다.</li></ul></section>
      </section>

      {dialog && <ConfirmDialog
        open
        title={dialog.title}
        description={dialog.description}
        confirmLabel={dialog.confirmLabel}
        variant={dialog.variant}
        loading={loading}
        confirmDisabled={confirmDisabled}
        onCancel={closeDialog}
        onConfirm={confirmDialog}
      >
        {mode === "typed" && <div><label htmlFor="confirmation-text" className="text-sm font-semibold text-zinc-800">확인을 위해 <span className="text-red-600">모임 삭제</span>를 입력해 주세요.</label><input id="confirmation-text" value={confirmationText} onChange={(event) => setConfirmationText(event.target.value)} disabled={loading} placeholder="모임 삭제" className="mt-2 h-11 w-full rounded-xl border border-zinc-200 px-3 text-sm outline-none focus:border-red-500 focus:ring-2 focus:ring-red-100 disabled:bg-zinc-50" /></div>}
      </ConfirmDialog>}
    </main>
  );
}
