"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";

import {
    changeMemberAuthority,
    delegateOwner,
    getMembers,
    kickMember,
    leaveRoom,
    type Member,
} from "@/app/lib/api/memberApi";

type ActionType =
    | "PROMOTE"
    | "DEMOTE"
    | "KICK"
    | "DELEGATE_OWNER"
    | "LEAVE";

type ConfirmState = {
    type: ActionType;
    member?: Member;
} | null;

export default function MembersPage() {
    const { roomId } = useParams<{ roomId: string }>();

    const [members, setMembers] = useState<Member[] | null>(null);
    const [error, setError] = useState<string | null>(null);

    const [openMenuUserId, setOpenMenuUserId] = useState<number | null>(null);
    const [confirmState, setConfirmState] = useState<ConfirmState>(null);
    const [actionLoading, setActionLoading] = useState(false);

    const roomIdNumber = Number(roomId);

    useEffect(() => {
        if (!roomId || Number.isNaN(roomIdNumber)) {
            return;
        }

        const loadMembers = async () => {
            try {
                const data = await getMembers(roomIdNumber);

                setMembers(data);
                setError(null);
            } catch (error) {
                console.error(error);
                setError("멤버 정보를 불러오지 못했습니다.");
            }
        };

        void loadMembers();
    }, [roomId, roomIdNumber]);

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

    const ownerCount =
        members?.filter(
            (member) => member.authority === "OWNER",
        ).length ?? 0;

    const operatorCount =
        members?.filter(
            (member) => member.authority === "OPERATOR",
        ).length ?? 0;

    const normalMemberCount =
        members?.filter(
            (member) => member.authority === "MEMBER",
        ).length ?? 0;

    const openConfirm = (
        type: ActionType,
        member?: Member,
    ) => {
        setOpenMenuUserId(null);

        setConfirmState({
            type,
            member,
        });
    };

    const closeConfirm = () => {
        if (actionLoading) {
            return;
        }

        setConfirmState(null);
    };

    const handleAction = async () => {
        if (!confirmState) {
            return;
        }

        if (!roomId || Number.isNaN(roomIdNumber)) {
            return;
        }

        const { type, member } = confirmState;

        try {
            setActionLoading(true);

            switch (type) {
                case "PROMOTE":
                    if (!member) {
                        return;
                    }

                    await changeMemberAuthority(
                        roomIdNumber,
                        member.userId,
                        "OPERATOR",
                    );
                    break;

                case "DEMOTE":
                    if (!member) {
                        return;
                    }

                    await changeMemberAuthority(
                        roomIdNumber,
                        member.userId,
                        "MEMBER",
                    );
                    break;

                case "KICK":
                    if (!member) {
                        return;
                    }

                    await kickMember(
                        roomIdNumber,
                        member.userId,
                    );
                    break;

                case "DELEGATE_OWNER":
                    if (!member) {
                        return;
                    }

                    await delegateOwner(
                        roomIdNumber,
                        member.userId,
                    );
                    break;

                case "LEAVE":
                    await leaveRoom(roomIdNumber);

                    window.location.replace("/rooms");
                    return;
            }

            const updatedMembers = await getMembers(roomIdNumber);

            setMembers(updatedMembers);
            setConfirmState(null);
            setOpenMenuUserId(null);
            setError(null);
        } catch (error) {
            console.error(error);

            alert(
                error instanceof Error
                    ? error.message
                    : "요청 처리에 실패했습니다.",
            );
        } finally {
            setActionLoading(false);
        }
    };

    if (!roomId || Number.isNaN(roomIdNumber)) {
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
            {/* 헤더 */}
            <div className="mb-7 flex items-start justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-zinc-900">
                        멤버
                    </h1>

                    <p className="mt-2 text-sm text-zinc-500">
                        총 {members?.length ?? 0}명이 함께하고 있어요
                    </p>
                </div>

                <button
                    type="button"
                    onClick={() => {
                        alert("멤버 초대 기능은 준비 중입니다.");
                    }}
                    className="text-sm font-semibold text-indigo-600 transition hover:text-indigo-700"
                >
                    + 멤버 초대
                </button>
            </div>

            {/* 로딩 */}
            {members === null && !error && (
                <div className="rounded-2xl border border-zinc-200 bg-white p-8 text-center text-sm text-zinc-500">
                    멤버 정보를 불러오는 중입니다.
                </div>
            )}

            {/* 에러 */}
            {members === null && error && (
                <div className="rounded-2xl border border-red-200 bg-red-50 p-8 text-center text-sm text-red-600">
                    {error}
                </div>
            )}

            {/* 멤버 목록 */}
            {members !== null && !error && (
                <>
                    <div className="space-y-3">
                        {members.map((member) => (
                            <div
                                key={member.userId}
                                className="relative flex items-center rounded-2xl border border-zinc-200 bg-white px-6 py-5"
                            >
                                {/* 프로필 */}
                                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-indigo-600 text-sm font-bold text-white">
                                    {member.name.charAt(0)}
                                </div>

                                {/* 이름 / 권한 */}
                                <div className="ml-4 min-w-0 flex-1">
                                    <div className="flex items-center gap-2">
                                        <p className="font-semibold text-zinc-900">
                                            {member.name}
                                        </p>

                                        <span
                                            className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                                                member.authority ===
                                                "OWNER"
                                                    ? "bg-indigo-50 text-indigo-600"
                                                    : member.authority ===
                                                    "OPERATOR"
                                                        ? "bg-blue-50 text-blue-600"
                                                        : "bg-zinc-100 text-zinc-500"
                                            }`}
                                        >
                                            {getAuthorityLabel(
                                                member.authority,
                                            )}
                                        </span>
                                    </div>

                                    <p className="mt-1 text-sm text-zinc-400">
                                        User ID #{member.userId}
                                    </p>
                                </div>

                                {/* 더보기 */}
                                <div className="relative">
                                    <button
                                        type="button"
                                        aria-label={`${member.name} 멤버 메뉴`}
                                        onClick={() =>
                                            setOpenMenuUserId(
                                                (current) =>
                                                    current ===
                                                    member.userId
                                                        ? null
                                                        : member.userId,
                                            )
                                        }
                                        className="flex h-9 w-9 items-center justify-center rounded-full text-xl text-zinc-400 transition hover:bg-zinc-100 hover:text-zinc-700"
                                    >
                                        ⋮
                                    </button>

                                    {openMenuUserId ===
                                        member.userId && (
                                            <MemberActionMenu
                                                member={member}
                                                onPromote={() =>
                                                    openConfirm(
                                                        "PROMOTE",
                                                        member,
                                                    )
                                                }
                                                onDemote={() =>
                                                    openConfirm(
                                                        "DEMOTE",
                                                        member,
                                                    )
                                                }
                                                onKick={() =>
                                                    openConfirm(
                                                        "KICK",
                                                        member,
                                                    )
                                                }
                                                onDelegateOwner={() =>
                                                    openConfirm(
                                                        "DELEGATE_OWNER",
                                                        member,
                                                    )
                                                }
                                            />
                                        )}
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* 통계 */}
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

                    {/* 모임 나가기 */}
                    <div className="mt-8 border-t border-zinc-200 pt-6">
                        <button
                            type="button"
                            onClick={() =>
                                openConfirm("LEAVE")
                            }
                            className="w-full rounded-xl border border-red-200 bg-white py-3.5 text-sm font-semibold text-red-500 transition hover:bg-red-50"
                        >
                            모임 나가기
                        </button>
                    </div>
                </>
            )}

            {/* 확인 모달 */}
            {confirmState && (
                <ConfirmModal
                    state={confirmState}
                    loading={actionLoading}
                    onCancel={closeConfirm}
                    onConfirm={handleAction}
                />
            )}
        </section>
    );
}

function MemberActionMenu({
                              member,
                              onPromote,
                              onDemote,
                              onKick,
                              onDelegateOwner,
                          }: {
    member: Member;
    onPromote: () => void;
    onDemote: () => void;
    onKick: () => void;
    onDelegateOwner: () => void;
}) {
    return (
        <div className="absolute right-0 top-11 z-20 w-44 overflow-hidden rounded-xl border border-zinc-200 bg-white py-1 shadow-lg">
            {member.authority === "MEMBER" && (
                <button
                    type="button"
                    onClick={onPromote}
                    className="block w-full px-4 py-3 text-left text-sm text-zinc-700 transition hover:bg-zinc-50"
                >
                    관리자 지정
                </button>
            )}

            {member.authority === "OPERATOR" && (
                <button
                    type="button"
                    onClick={onDemote}
                    className="block w-full px-4 py-3 text-left text-sm text-zinc-700 transition hover:bg-zinc-50"
                >
                    관리자 해제
                </button>
            )}

            {member.authority !== "OWNER" && (
                <button
                    type="button"
                    onClick={onKick}
                    className="block w-full px-4 py-3 text-left text-sm text-red-500 transition hover:bg-red-50"
                >
                    강퇴하기
                </button>
            )}

            {member.authority !== "OWNER" && (
                <button
                    type="button"
                    onClick={onDelegateOwner}
                    className="block w-full px-4 py-3 text-left text-sm text-zinc-700 transition hover:bg-zinc-50"
                >
                    방장 위임
                </button>
            )}

            {member.authority === "OWNER" && (
                <div className="px-4 py-3 text-sm text-zinc-400">
                    현재 방장
                </div>
            )}
        </div>
    );
}

function ConfirmModal({
                          state,
                          loading,
                          onCancel,
                          onConfirm,
                      }: {
    state: ConfirmState;
    loading: boolean;
    onCancel: () => void;
    onConfirm: () => void;
}) {
    const memberName = state?.member?.name;

    let title = "";
    let description = "";
    let confirmText = "";
    let danger = false;

    switch (state?.type) {
        case "PROMOTE":
            title = "관리자로 지정할까요?";
            description = `${memberName}님을 관리자로 지정합니다.`;
            confirmText = "관리자 지정";
            break;

        case "DEMOTE":
            title = "관리자 권한을 해제할까요?";
            description = `${memberName}님의 관리자 권한을 해제합니다.`;
            confirmText = "권한 해제";
            break;

        case "KICK":
            title = "멤버를 강퇴할까요?";
            description = `${memberName}님을 모임에서 내보냅니다.`;
            confirmText = "강퇴하기";
            danger = true;
            break;

        case "DELEGATE_OWNER":
            title = "방장을 위임할까요?";
            description = `${memberName}님에게 방장 권한을 넘깁니다.`;
            confirmText = "방장 위임";
            danger = true;
            break;

        case "LEAVE":
            title = "모임을 나갈까요?";
            description =
                "모임을 나가면 다시 초대를 받아야 참여할 수 있습니다.";
            confirmText = "모임 나가기";
            danger = true;
            break;
    }

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
            onMouseDown={(event) => {
                if (event.target === event.currentTarget) {
                    onCancel();
                }
            }}
        >
            <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl">
                <h2 className="text-lg font-bold text-zinc-900">
                    {title}
                </h2>

                <p className="mt-2 text-sm leading-6 text-zinc-500">
                    {description}
                </p>

                <div className="mt-6 flex gap-2">
                    <button
                        type="button"
                        disabled={loading}
                        onClick={onCancel}
                        className="flex-1 rounded-xl bg-zinc-100 py-3 text-sm font-semibold text-zinc-700 transition hover:bg-zinc-200 disabled:opacity-50"
                    >
                        취소
                    </button>

                    <button
                        type="button"
                        disabled={loading}
                        onClick={onConfirm}
                        className={`flex-1 rounded-xl py-3 text-sm font-semibold text-white transition disabled:opacity-50 ${
                            danger
                                ? "bg-red-500 hover:bg-red-600"
                                : "bg-indigo-600 hover:bg-indigo-700"
                        }`}
                    >
                        {loading ? "처리 중..." : confirmText}
                    </button>
                </div>
            </div>
        </div>
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
                    highlight
                        ? "text-indigo-600"
                        : "text-zinc-900"
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