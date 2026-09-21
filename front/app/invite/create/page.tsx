"use client"

import CreateInviteButton from "../CreateInviteButton";

export default function CreateInvitePage() {
  return (
    <main className="min-h-screen bg-zinc-50 flex items-center justify-center px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-sm">
        <h1 className="text-2xl font-bold text-zinc-900">
          초대 링크 생성
        </h1>

        <p className="mt-3 text-zinc-600">
          모임에 참여할 수 있는 초대 링크를 생성합니다.
        </p>

        <div className="mt-6">
          <CreateInviteButton roomId={1} />
        </div>
      </div>
    </main>
  );
}