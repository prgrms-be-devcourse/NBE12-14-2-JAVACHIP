"use client";

interface JoinButtonProps {
  token: string;
}

export default function JoinButton({ token }: JoinButtonProps) {
  const handleJoin = () => {
    alert("버튼 클릭됨!");
  };

  return (
    <button
      type="button"
      onClick={handleJoin}
      className="mt-6 w-full rounded-xl bg-black py-3 font-medium text-white hover:bg-zinc-800"
    >
      모임 참여하기
    </button>
  );
}