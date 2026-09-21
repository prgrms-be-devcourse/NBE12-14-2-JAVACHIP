"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

export default function LoginPage() {
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await fetch("http://localhost:8080/users/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          email,
          password,
        }),
      });

      const result = await response.json();

      if (!response.ok) {
        setError(result.message ?? "로그인에 실패했습니다.");
        return;
      }

      alert("로그인되었습니다.");
      router.push("/");
    } catch (error) {
      console.error(error);
      setError("서버와 연결할 수 없습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen bg-zinc-100 flex items-center justify-center px-4">
      <div className="w-full max-w-md">

        {/* 로고 */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-black tracking-tight text-zinc-900">
            Budzet
          </h1>

          <p className="mt-2 text-sm text-zinc-500">
            팀 예산을 쉽고 편하게 관리하세요.
          </p>
        </div>

        {/* 로그인 카드 */}
        <div className="bg-white rounded-2xl shadow-lg border border-zinc-200 p-8">

          <div className="mb-7">
            <h2 className="text-2xl font-bold text-zinc-900">
              로그인
            </h2>

            <p className="mt-2 text-sm text-zinc-500">
              Budzet에 오신 것을 환영합니다.
            </p>
          </div>

          <form onSubmit={handleLogin} className="space-y-5">

            {/* 이메일 */}
            <div>
              <label className="block text-sm font-medium text-zinc-700 mb-2">
                이메일
              </label>

              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="이메일을 입력해주세요."
                autoComplete="email"
                className="w-full h-12 rounded-xl border border-zinc-300 px-4 text-sm outline-none transition focus:border-zinc-900 focus:ring-2 focus:ring-zinc-900/10"
                required
              />
            </div>

            {/* 비밀번호 */}
            <div>
              <label className="block text-sm font-medium text-zinc-700 mb-2">
                비밀번호
              </label>

              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="비밀번호를 입력해주세요."
                className="w-full h-12 rounded-xl border border-zinc-300 px-4 text-sm outline-none transition focus:border-zinc-900 focus:ring-2 focus:ring-zinc-900/10"
                required
              />
            </div>

            {/* 에러 메시지 */}
            {error && (
              <p className="text-sm text-red-500 bg-red-50 rounded-lg px-3 py-2">
                {error}
              </p>
            )}

            {/* 로그인 버튼 */}
            <button
              type="submit"
              disabled={loading}
              className="w-full h-12 rounded-xl bg-zinc-900 text-white font-semibold transition hover:bg-zinc-800 disabled:bg-zinc-400"
            >
              {loading ? "로그인 중..." : "로그인"}
            </button>
          </form>

          {/* 회원가입 이동 */}
          <div className="mt-6 pt-6 border-t border-zinc-100 text-center">
            <span className="text-sm text-zinc-500">
              아직 계정이 없으신가요?
            </span>

            <Link
              href="/signup"
              className="ml-2 text-sm font-semibold text-zinc-900 hover:underline"
            >
              회원가입
            </Link>
          </div>
        </div>
      </div>
    </main>
  );
}