"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";

import JoinButton from "./JoinButton";
import { verifyInvite, type Invite } from "../../lib/api/inviteApi";

export default function InvitePage() {
  const params = useParams<{ token: string }>();
  const token = params.token;
  const [invite, setInvite] = useState<Invite | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    void Promise.resolve()
      .then(async () => {
        if (!token) {
          setError("초대 링크를 찾을 수 없습니다.");
          return;
        }

        const verifiedInvite = await verifyInvite(token);
        setInvite(verifiedInvite);
      })
      .catch((caughtError: unknown) => {
          setError(
            caughtError instanceof Error
              ? caughtError.message
              : "초대 링크를 확인할 수 없습니다.",
          );
        })
      .finally(() => setLoading(false));
  }, [token]);

  if (loading) {
    return <InviteState message="초대 링크를 확인하는 중입니다." />;
  }

  if (error || !invite) {
    return <InviteState message={error ?? "초대 링크를 사용할 수 없습니다."} isError />;
  }

  return (
    <main className="min-h-screen flex items-center justify-center bg-zinc-50 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 text-center shadow-sm">
        <h1 className="text-2xl font-bold text-zinc-900">모임 초대</h1>
        <p className="mt-3 text-zinc-600">Budzet 모임에 초대되었습니다.</p>

        <div className="mt-6 rounded-xl bg-zinc-100 p-4 text-left">
          <p className="text-sm text-zinc-500">초대 코드</p>
          <p className="mt-1 font-medium text-zinc-900">{invite.code}</p>
          <p className="mt-3 text-sm text-zinc-500">만료 시간</p>
          <p className="mt-1 text-sm text-zinc-900">{invite.expireAt}</p>
        </div>

        <JoinButton token={token} />
      </div>
    </main>
  );
}

function InviteState({
  message,
  isError = false,
}: {
  message: string;
  isError?: boolean;
}) {
  return (
    <main className="min-h-screen flex items-center justify-center bg-zinc-50 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 text-center shadow-sm">
        <h1 className="text-2xl font-bold text-zinc-900">
          {isError ? "초대 링크를 사용할 수 없습니다" : "모임 초대"}
        </h1>
        <p className={`mt-3 ${isError ? "text-red-600" : "text-zinc-600"}`}>
          {message}
        </p>
      </div>
    </main>
  );
}
