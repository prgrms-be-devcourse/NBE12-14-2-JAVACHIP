const pendingRequests = [
  { name: "김민준", detail: "정기 모임 간식 구입", amount: "120,000원" },
  { name: "이서연", detail: "출사 교통비 (버스 대절)", amount: "85,000원" },
];

const settlements = [
  { title: "인화 및 액자 제작", member: "박지호", date: "2026-09-13", amount: "132,000원", refund: "+16,000원 반환" },
  { title: "전시회 현수막 제작", member: "정다은", date: "2026-09-10", amount: "55,000원" },
  { title: "9월 정기 모임 간식", member: "이서연", date: "2026-09-05", amount: "78,000원", refund: "+12,000원 반환" },
];

const flowSteps = [
  { label: "예산 신청", color: "bg-indigo-50 text-indigo-500" },
  { label: "승인", color: "bg-amber-50 text-amber-600" },
  { label: "예약 차감", color: "bg-indigo-50 text-indigo-400" },
  { label: "실제 지출", color: "bg-emerald-50 text-emerald-600" },
  { label: "정산", color: "bg-sky-50 text-sky-600" },
  { label: "차액 반환", color: "bg-indigo-50 text-indigo-500" },
];

function SummaryItem({ label, amount, description, tone }: { label: string; amount: string; description?: string; tone: "default" | "available" | "reserved" }) {
  const amountColor = tone === "available" ? "text-emerald-600" : tone === "reserved" ? "text-indigo-400" : "text-zinc-950";
  return <div><p className="text-sm font-medium text-zinc-500">{label}</p><p className={`mt-1 text-3xl font-extrabold tracking-tight sm:text-4xl ${amountColor}`}>{amount}</p>{description && <p className="mt-1 text-sm text-zinc-500">{description}</p>}</div>;
}

export default function DashboardPage() {
  return <section aria-labelledby="dashboard-title" className="mx-auto w-full max-w-5xl">
    <header className="flex flex-wrap items-start justify-between gap-4"><div><h1 id="dashboard-title" className="text-2xl font-bold tracking-tight text-zinc-900">대시보드</h1><p className="mt-1 text-sm text-zinc-500">한양대 사진동아리 렌즈의 예산 현황</p></div><Link href="/approvals" className="rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-indigo-700">승인 대기 2건</Link></header>

    <article className="mt-8 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm sm:p-8">
      <div className="grid gap-7 sm:grid-cols-3 sm:gap-6"><SummaryItem label="총 예산" amount="2,000,000원" tone="default" /><SummaryItem label="사용 가능" amount="1,247,000원" description="전체의 62.4%" tone="available" /><SummaryItem label="예약 중" amount="353,000원" description="승인된 미정산 요청" tone="reserved" /></div>
      <div className="mt-8"><div className="flex h-3 overflow-hidden rounded-full bg-zinc-100" aria-label="예산 사용 현황"><span className="w-1/5 bg-indigo-600" /><span className="w-[17.65%] bg-indigo-300" /><span className="flex-1 bg-emerald-100" /></div><div className="mt-3 flex flex-wrap gap-x-4 gap-y-2 text-sm text-zinc-500"><span className="flex items-center gap-1.5"><i className="h-2.5 w-2.5 rounded-full bg-indigo-600" />지출됨</span><span className="flex items-center gap-1.5"><i className="h-2.5 w-2.5 rounded-full bg-indigo-300" />예약 중</span><span className="flex items-center gap-1.5"><i className="h-2.5 w-2.5 rounded-full bg-emerald-300" />사용 가능</span></div></div>
      <p className="mt-6 border-t border-zinc-200 pt-6 text-sm font-medium text-zinc-800">총 예산 2,000,000원 <span className="mx-2 text-zinc-400">→</span><span className="text-indigo-500">지출됨 400,000원</span><span className="mx-2 text-zinc-400">+</span><span className="text-indigo-400">예약 중 353,000원</span><span className="mx-2 text-zinc-400">=</span><span className="text-emerald-600">사용 가능 1,247,000원</span></p>
    </article>

    <div className="mt-7 grid gap-7 md:grid-cols-2">
      <article className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm"><div className="flex items-center justify-between"><h2 className="text-lg font-bold">승인 대기 요청</h2><span className="rounded-full bg-amber-100 px-2.5 py-1 text-sm font-semibold text-amber-700">2건</span></div><ul className="mt-5 divide-y divide-zinc-100">{pendingRequests.map((request) => <li key={request.name} className="flex items-center justify-between gap-4 py-4 first:pt-0"><div><p className="font-semibold text-zinc-800">{request.name}</p><p className="mt-1 text-sm text-zinc-500">{request.detail}</p></div><div className="text-right"><p className="font-bold text-zinc-900">{request.amount}</p><span className="mt-1 inline-block rounded-full border border-amber-200 bg-amber-50 px-2 py-0.5 text-xs font-semibold text-amber-600">검토 중</span></div></li>)}</ul><Link href="/approvals" className="mt-4 flex h-11 items-center justify-center rounded-xl border border-indigo-200 bg-indigo-50 text-sm font-semibold text-indigo-600 transition-colors hover:bg-indigo-100">모두 보기</Link></article>

      <article className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm"><div className="flex items-center justify-between"><h2 className="text-lg font-bold">최근 정산 내역</h2><Link href="/settlements" className="text-sm font-semibold text-indigo-500 hover:text-indigo-700">전체 보기</Link></div><ul className="mt-5 divide-y divide-zinc-100">{settlements.map((settlement) => <li key={settlement.title} className="flex items-center justify-between gap-4 py-4 first:pt-0"><div className="min-w-0"><p className="truncate font-semibold text-zinc-800">{settlement.title}</p><p className="mt-1 text-sm text-zinc-500">{settlement.member} · {settlement.date}</p></div><div className="shrink-0 text-right"><p className="font-bold text-zinc-900">{settlement.amount}</p>{settlement.refund && <p className="mt-1 text-sm font-semibold text-emerald-600">{settlement.refund}</p>}</div></li>)}</ul></article>
    </div>

    <article className="mt-7 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm"><h2 className="text-sm font-bold text-zinc-600">예산 집행 흐름</h2><ol className="mt-4 flex flex-wrap items-center gap-2">{flowSteps.map((step, index) => <li key={step.label} className="flex items-center gap-2"><span className={`rounded-full px-3 py-1.5 text-sm font-semibold ${step.color}`}>{step.label}</span>{index < flowSteps.length - 1 && <span className="text-zinc-400" aria-hidden="true">→</span>}</li>)}</ol></article>
  </section>;
}
import Link from "next/link";
