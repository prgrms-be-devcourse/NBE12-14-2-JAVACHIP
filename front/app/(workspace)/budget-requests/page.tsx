const steps = [
  "금액과 사유를 입력하고 신청",
  "방장/관리자가 승인 또는 반려",
  "승인 시 해당 금액 예약 처리",
  "실제 지출 후 정산하기",
];

export default function BudgetRequestsPage() {
  return <section aria-labelledby="budget-request-title" className="mx-auto w-full max-w-5xl">
    <header>
      <h1 id="budget-request-title" className="text-2xl font-bold tracking-tight text-zinc-900">예산 신청</h1>
      <p className="mt-1 text-sm text-zinc-500">지출 전 예산을 미리 신청하고 승인받으세요</p>
    </header>

    <div className="mt-8 grid max-w-4xl gap-7 lg:grid-cols-[minmax(0,1.55fr)_minmax(280px,1fr)]">
      <form className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm sm:p-8">
        <h2 className="text-lg font-bold text-zinc-800">새 예산 신청</h2>
        <div className="mt-6">
          <label htmlFor="amount" className="text-base font-semibold text-zinc-800">신청 금액</label>
          <div className="mt-2 flex h-14 items-center rounded-xl border border-zinc-200 px-4 transition focus-within:border-indigo-400 focus-within:ring-2 focus-within:ring-indigo-100">
            <input id="amount" inputMode="numeric" placeholder="0" className="min-w-0 flex-1 bg-transparent text-base text-zinc-900 outline-none placeholder:text-zinc-400" />
            <span className="text-sm font-medium text-zinc-500">원</span>
          </div>
        </div>
        <div className="mt-6">
          <label htmlFor="reason" className="text-base font-semibold text-zinc-800">신청 사유</label>
          <textarea id="reason" rows={4} placeholder="예: 10월 정기 모임 간식 구입 — 편의점 5만원, 음료 3만원" className="mt-2 w-full resize-none rounded-xl border border-zinc-200 px-4 py-3 text-base text-zinc-900 outline-none placeholder:text-zinc-400 focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100" />
        </div>
        <button type="button" disabled className="mt-5 h-14 w-full rounded-xl bg-indigo-500/55 text-base font-semibold text-white disabled:cursor-not-allowed">예산 신청하기</button>
      </form>

      <div className="space-y-5">
        <article className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
          <h2 className="text-sm font-bold text-zinc-500">현재 예산 현황</h2>
          <dl className="mt-4 space-y-3 text-sm"><div className="flex justify-between"><dt className="text-zinc-500">총 예산</dt><dd className="text-lg font-extrabold text-zinc-900">2,000,000원</dd></div><div className="flex justify-between"><dt className="text-zinc-500">예약 중</dt><dd className="text-lg font-extrabold text-indigo-400">353,000원</dd></div><div className="border-t border-zinc-200 pt-4"><div className="flex justify-between"><dt className="font-semibold text-zinc-800">사용 가능</dt><dd className="text-xl font-extrabold text-emerald-600">1,247,000원</dd></div></div></dl>
        </article>
        <article className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
          <h2 className="text-sm font-bold text-indigo-600">신청 절차 안내</h2>
          <ol className="mt-4 space-y-3">{steps.map((step, index) => <li key={step} className="flex items-center gap-2 text-sm font-medium text-indigo-500"><span className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-indigo-600 text-xs font-bold text-white">{index + 1}</span>{step}</li>)}</ol>
        </article>
      </div>
    </div>

    <section aria-labelledby="my-requests-title" className="mt-10 max-w-4xl">
      <h2 id="my-requests-title" className="text-lg font-bold text-zinc-800">내 신청 내역</h2>
      <div className="mt-5 overflow-x-auto rounded-2xl border border-zinc-200 bg-white shadow-sm">
        <table className="min-w-[620px] w-full text-left text-sm"><thead className="border-b border-zinc-200 bg-zinc-50 text-zinc-500"><tr><th className="px-6 py-4 font-medium">신청일</th><th className="px-6 py-4 font-medium">사유</th><th className="px-6 py-4 text-right font-medium">금액</th><th className="px-6 py-4 text-right font-medium">상태</th></tr></thead><tbody><tr className="text-zinc-800"><td className="px-6 py-5 text-zinc-500">2026-09-14</td><td className="px-6 py-5 font-semibold">정기 모임 간식 구입</td><td className="px-6 py-5 text-right text-lg font-extrabold">120,000원</td><td className="px-6 py-5 text-right"><span className="rounded-full border border-amber-200 bg-amber-50 px-2.5 py-1 text-sm font-semibold text-amber-600">검토 중</span></td></tr></tbody></table>
      </div>
    </section>
  </section>;
}
