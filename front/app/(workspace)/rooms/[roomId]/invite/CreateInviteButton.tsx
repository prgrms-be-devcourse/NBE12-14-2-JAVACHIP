"use client";

import { useState } from "react";
import { createInvite } from '@/app/lib/api/inviteApi';

interface CreateInviteButtonProps {
  roomId: number;
}

export default function CreateInviteButton({
  roomId,
}: CreateInviteButtonProps) {
  const [inviteUrl, setInviteUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleCreateInvite = async () => {
    try {
      setLoading(true);

      const invite = await createInvite(roomId);
      const url = `${window.location.origin}/invite/${invite.code}`;

      setInviteUrl(url);
    } catch (error) {
      console.error("초대 링크 생성 실패:", error);
      alert(
        error instanceof Error
          ? error.message
          : "초대 링크 생성에 실패했습니다.",
      );
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = async () => {
    if (!inviteUrl) {
      return;
    }

    await navigator.clipboard.writeText(inviteUrl);

    alert("초대 링크가 복사되었습니다!");
  };

  return (
    <div className="w-full">
      <button
        type="button"
        onClick={handleCreateInvite}
        disabled={loading}
        className="w-full rounded-xl bg-black py-3 font-medium text-white hover:bg-zinc-800 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {loading ? "초대 링크 생성 중..." : "초대 링크 생성"}
      </button>

      {inviteUrl && (
        <div className="mt-4 rounded-xl bg-zinc-100 p-4">
          <p className="text-sm text-zinc-500">
            초대 링크
          </p>

          <p className="mt-2 break-all text-sm text-zinc-900">
            {inviteUrl}
          </p>

          <button
            type="button"
            onClick={handleCopy}
            className="mt-3 w-full rounded-xl bg-white py-2 font-medium text-zinc-900 shadow-sm hover:bg-zinc-50"
          >
            링크 복사
          </button>
        </div>
      )}
    </div>
  );
}
