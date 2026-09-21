import JoinButton from "./JoinButton";

interface InvitePageProps {
    params: Promise<{
      token: string;
    }>;
  }
  
  export default async function InvitePage({
    params,
  }: InvitePageProps) {
    const { token } = await params;
  
    const response = await fetch(
      `http://localhost:8080/invites/${token}`,
      {
        cache: "no-store",
      }
    );
  
    if (!response.ok) {
      return (
        <main className="min-h-screen flex items-center justify-center bg-zinc-50 px-4">
          <div className="w-full max-w-md rounded-2xl bg-white p-8 text-center shadow-sm">
            <h1 className="text-2xl font-bold text-zinc-900">
              초대 링크를 사용할 수 없습니다
            </h1>
  
            <p className="mt-3 text-zinc-600">
              초대 링크가 존재하지 않거나 만료되었습니다.
            </p>
          </div>
        </main>
      );
    }
  
    const result = await response.json();
  
    return (
      <main className="min-h-screen flex items-center justify-center bg-zinc-50 px-4">
        <div className="w-full max-w-md rounded-2xl bg-white p-8 text-center shadow-sm">
          <h1 className="text-2xl font-bold text-zinc-900">
            모임 초대
          </h1>
  
          <p className="mt-3 text-zinc-600">
            Budzet 모임에 초대되었습니다.
          </p>
  
          <div className="mt-6 rounded-xl bg-zinc-100 p-4 text-left">
            <p className="text-sm text-zinc-500">
              초대 코드
            </p>
  
            <p className="mt-1 font-medium text-zinc-900">
              {result.data.code}
            </p>
  
            <p className="mt-3 text-sm text-zinc-500">
              만료 시간
            </p>
  
            <p className="mt-1 text-sm text-zinc-900">
              {result.data.expireAt}
            </p>
          </div>
  
          <JoinButton token={token} />
        </div>
      </main>
    );
  }