"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

import { joinRoom } from '@/app/lib/api/inviteApi';

interface JoinButtonProps {
  token: string;
}

export default function JoinButton({ token }: JoinButtonProps) {
  const router = useRouter();
  const [joining, setJoining] = useState(false);

  const handleJoin = async () => {
    try {
      setJoining(true);
      const joinedRoom = await joinRoom(token);
      router.replace(`/rooms/${joinedRoom.roomId}/dashboard`);
    } catch (error) {
      alert(
        error instanceof Error
          ? error.message
          : "모임 참여에 실패했습니다.",
      );
      router.replace("/");
    } finally {
      setJoining(false);
    }
  };

  return (
    <button
      type="button"
      onClick={handleJoin}
      disabled={joining}
      className="mt-6 w-full rounded-xl bg-black py-3 font-medium text-white hover:bg-zinc-800 disabled:cursor-not-allowed disabled:opacity-50"
    >
      {joining ? "참여 중..." : "모임 참여하기"}
    </button>
  );
}
