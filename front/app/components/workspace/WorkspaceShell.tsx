"use client";

import Link from "next/link";
import { useParams, usePathname } from "next/navigation";
import type { ReactNode } from "react";
import { useEffect, useState } from "react";

import { getMembers, getRoom } from "../../lib/api/roomsApi";

type IconName =
  | "dashboard"
  | "request"
  | "settlement"
  | "members"
  | "invite"
  | "approval"
  | "menu"
  | "close"
  | "chevron"
  | "back";

const navigation: { label: string; path: string; icon: IconName; badge?: string }[] = [
  { label: "대시보드", path: "dashboard", icon: "dashboard" },
  { label: "예산 신청", path: "budget-requests", icon: "request" },
  { label: "정산하기", path: "settlements", icon: "settlement" },
  { label: "멤버", path: "members", icon: "members" },
  { label: "초대하기", path: "invites", icon: "invite" },
  { label: "승인 관리", path: "approvals", icon: "approval", badge: "2" },
];

function Icon({ name, className = "" }: { name: IconName; className?: string }) {
  const svgProps = {
    className: `h-5 w-5 shrink-0 ${className}`,
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: 1.8,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const,
    "aria-hidden": true,
  };
  const content: Record<IconName, ReactNode> = {
    dashboard: <><rect x="3" y="3" width="7" height="7" rx="1" /><rect x="14" y="3" width="7" height="7" rx="1" /><rect x="3" y="14" width="7" height="7" rx="1" /><rect x="14" y="14" width="7" height="7" rx="1" /></>,
    request: <><rect x="5" y="3" width="14" height="18" rx="2" /><path d="M8 8h8M8 12h8M8 16h4" /></>,
    settlement: <><path d="M5 4h14v16H5z" /><path d="M8 8h8M8 12l2 2 4-4M8 17h5" /></>,
    members: <><circle cx="9" cy="8" r="3" /><path d="M3.5 20a5.5 5.5 0 0 1 11 0M16 11a3 3 0 1 0-1.8-5.4M16 14a5 5 0 0 1 4.5 3" /></>,
    invite: <><path d="M10.5 13.5a4 4 0 0 0 5.7.1l2.1-2.1a4 4 0 0 0-5.7-5.7l-1.2 1.2" /><path d="M13.5 10.5a4 4 0 0 0-5.7-.1l-2.1 2.1a4 4 0 1 0 5.7 5.7l1.2-1.2" /></>,
    approval: <><circle cx="12" cy="12" r="8.5" /><path d="m8.5 12 2.3 2.3 4.8-5" /></>,
    menu: <path d="M4 7h16M4 12h16M4 17h16" />,
    close: <path d="m6 6 12 12M18 6 6 18" />,
    chevron: <path d="m9 18-6-6 6-6" />,
    back: <path d="m15 18-6-6 6-6" />,
  };
  return <svg {...svgProps}>{content[name]}</svg>;
}

function Sidebar({ onNavigate, roomName, ownerName }: { onNavigate?: () => void; roomName: string; ownerName: string }) {
  const pathname = usePathname();
  const roomId = pathname.match(/^\/rooms\/([^/]+)/)?.[1];
  const roomBasePath = roomId ? `/rooms/${roomId}` : null;

  return (
    <aside className="flex h-full w-64 flex-col border-r border-zinc-200 bg-white px-3 py-5">
      <Link href="/rooms" onClick={onNavigate} className="mb-5 flex items-center gap-2 px-2 text-sm font-medium text-zinc-500">
        <Icon name="back" className="h-4 w-4" />내 모임 목록
      </Link>
      <button type="button" className="mb-6 flex w-full items-center gap-3 rounded-xl px-2 py-2 text-left hover:bg-zinc-50">
        <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-600 text-base font-bold text-white">{roomName.charAt(0) || "모"}</span>
        <span className="min-w-0 flex-1 truncate text-[15px] font-semibold text-zinc-800">{roomName}</span>
        <Icon name="chevron" className="h-4 w-4 text-zinc-400" />
      </button>
      <nav aria-label="업무 메뉴" className="space-y-1">
        {navigation.map((item) => {
          const href = roomBasePath ? `${roomBasePath}/${item.path}` : "/rooms";
          const active = pathname === href;
          return (
            <Link key={item.path} href={href} onClick={onNavigate} className={`flex h-11 items-center gap-3 rounded-xl px-3 text-sm font-medium transition-colors ${active ? "bg-indigo-50 text-indigo-600" : "text-zinc-600 hover:bg-zinc-50 hover:text-zinc-900"}`}>
              <Icon name={item.icon} />
              <span className="flex-1">{item.label}</span>
              {item.badge && <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-amber-100 px-1.5 text-xs font-semibold text-amber-700">{item.badge}</span>}
            </Link>
          );
        })}
      </nav>
      <div className="mt-auto border-t border-zinc-100 px-2 pt-4">
        <div className="flex items-center gap-3">
          <span className="flex h-9 w-9 items-center justify-center rounded-full bg-zinc-800 text-sm font-semibold text-white">{ownerName.charAt(0) || "?"}</span>
          <div><p className="text-sm font-semibold text-zinc-800">{ownerName}</p><p className="text-xs text-zinc-500">방장</p></div>
        </div>
      </div>
    </aside>
  );
}

export default function WorkspaceShell({ children }: { children: ReactNode }) {
  const [isOpen, setIsOpen] = useState(false);
  const { roomId: roomIdParam } = useParams<{ roomId?: string }>();
  const roomId = roomIdParam ? Number(roomIdParam) : null;
  const [roomName, setRoomName] = useState("모임");
  const [ownerName, setOwnerName] = useState("방장");

  useEffect(() => {
    if (!roomId || Number.isNaN(roomId)) return;
    const loadWorkspace = async () => {
      try {
        const [room, members] = await Promise.all([getRoom(roomId), getMembers(roomId)]);
        setRoomName(room.name);
        const owner = members.find((member) => member.authority === "OWNER");
        if (owner) setOwnerName(owner.name);
      } catch (error) {
        console.error("모임 정보를 불러오지 못했습니다.", error);
      }
    };
    void loadWorkspace();
  }, [roomId]);

  return (
    <div className="min-h-screen bg-[#f8f8fb] text-zinc-900">
      <div className="fixed inset-y-0 left-0 z-20 hidden md:block"><Sidebar roomName={roomName} ownerName={ownerName} /></div>
      <header className="sticky top-0 z-10 flex h-16 items-center border-b border-zinc-200 bg-white px-4 md:hidden">
        <button type="button" aria-label="메뉴 열기" onClick={() => setIsOpen(true)} className="rounded-lg p-2 text-zinc-700 hover:bg-zinc-100"><Icon name="menu" /></button>
        <span className="ml-3 truncate text-sm font-semibold">{roomName}</span>
      </header>
      {isOpen && (
        <div className="fixed inset-0 z-30 md:hidden">
          <button type="button" aria-label="메뉴 닫기" onClick={() => setIsOpen(false)} className="absolute inset-0 bg-zinc-900/30" />
          <div className="relative h-full w-72 bg-white shadow-xl">
            <button type="button" aria-label="메뉴 닫기" onClick={() => setIsOpen(false)} className="absolute right-3 top-4 rounded-lg p-2 text-zinc-600 hover:bg-zinc-100"><Icon name="close" /></button>
            <Sidebar roomName={roomName} ownerName={ownerName} onNavigate={() => setIsOpen(false)} />
          </div>
        </div>
      )}
      <main className="min-h-[calc(100vh-4rem)] px-5 py-6 md:ml-64 md:min-h-screen md:px-10 md:py-10 lg:px-14">{children}</main>
    </div>
  );
}
