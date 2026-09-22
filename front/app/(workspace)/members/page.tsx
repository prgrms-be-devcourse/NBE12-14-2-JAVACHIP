"use client";

import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "next/navigation";

import { getMembers, type Member } from "../../lib/api/roomsApi";

export default function MembersPage() {
    const searchParams = useSearchParams();
    const roomIdParam = searchParams.get("roomId");
    const roomId = roomIdParam ? Number(roomIdParam) : null;

    const [members, setMembers] = useState<Member[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!roomId || Number.isNaN(roomId)) {
            return;
        }

        const loadMembers = async () => {
            try {
                const data = await getMembers(roomId);

                setMembers(data);
                setError(null);
            } catch (error) {
                console.error(error);
                setError("멤버 정보를 불러오지 못했습니다.");
            } finally {
                setLoading(false);
            }
        };

        void loadMembers();
    }, [roomId]);

    const ownerCount = useMemo(
        () => members.filter((member) => member.authority === "OWNER").length,
        [members],
    );

    const operatorCount = useMemo(
        () =>
            members.filter((member) => member.authority === "OPERATOR").length,
        [members],
    );

    const normalMemberCount = useMemo(
        () =>
            members.filter((member) => member.authority === "MEMBER").length,
        [members],
    );

    const getAuthorityLabel = (authority: Member["authority"]) => {
        switch (authority) {
            case "OWNER":
                return "방장";
            case "OPERATOR":
                return "관리자";
            case "MEMBER":
                return "멤버";
        }
    };

    if (!roomId) {
        return (
            <section className="mx-auto max-w-5xl">
                <p className="text-sm text-zinc-500">
                    모임 정보가 없습니다.
                </p>
            </section>
        );
    }

    return (
        <section className="mx-auto max-w-5xl">
            <div className="mb-7 flex items-start justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-zinc-900">
                        멤버
                    </h1>

                    <p className="mt-2 text-sm text-zinc-500">
                        총 {members.length}명이 함께하고 있어요
                    </p>
                </div>

                <button
                    type="button"
                    className="text-sm font-semibold text-indigo-600 hover:text-indigo-700"
                >
                    + 멤버 초대
                </button>
            </div>

            {loading && (
                <div className="rounded-2xl border border-zinc-200 bg-white p-8 text-center text-sm text-zinc-500">
                    멤버 정보를 불러오는 중입니다.
                </div>
            )}

            {!loading && error && (
                <div className="rounded-2xl border border-red-200 bg-red-50 p-8 text-center text-sm text-red-600">
                    {error}
                </div>
            )}

            {!loading && !error && (
                <>
                    <div className="space-y-3">
                        {members.map((member) => (
                            <div
                                key={member.userId}
                                className="flex items-center rounded-2xl border border-zinc-200 bg-white px-6 py-5"
                            >
                                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-indigo-600 text-sm font-bold text-white">
                                    {member.name.charAt(0)}
                                </div>

                                <div className="ml-4 min-w-0 flex-1">
                                    <div className="flex items-center gap-2">
                                        <p className="font-semibold text-zinc-900">
                                            {member.name}
                                        </p>

                                        <span
                                            className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                                                member.authority === "OWNER"
                                                    ? "bg-indigo-50 text-indigo-600"
                                                    : member.authority === "OPERATOR"
                                                        ? "bg-blue-50 text-blue-600"
                                                        : "bg-zinc-100 text-zinc-500"
                                            }`}
                                        >
                      {getAuthorityLabel(member.authority)}
                    </span>
                                    </div>

                                    <p className="mt-1 text-sm text-zinc-400">
                                        User ID #{member.userId}
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="mt-6 grid grid-cols-3 gap-4">
                        <StatCard
                            value={members.length}
                            label="전체 멤버"
                        />

                        <StatCard
                            value={ownerCount + operatorCount}
                            label="관리자"
                            highlight
                        />

                        <StatCard
                            value={normalMemberCount}
                            label="일반 멤버"
                        />
                    </div>
                </>
            )}
        </section>
    );
}

function StatCard({
                      value,
                      label,
                      highlight = false,
                  }: {
    value: number;
    label: string;
    highlight?: boolean;
}) {
    return (
        <div className="rounded-2xl border border-zinc-200 bg-white px-6 py-5 text-center">
            <p
                className={`text-2xl font-bold ${
                    highlight ? "text-indigo-600" : "text-zinc-900"
                }`}
            >
                {value}
            </p>

            <p className="mt-1 text-sm text-zinc-500">
                {label}
            </p>
        </div>
    );
}