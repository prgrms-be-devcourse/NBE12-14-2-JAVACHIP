import { apiFetch } from "./client";

export type Member = {
    userId: number;
    name: string;
    authority: "OWNER" | "OPERATOR" | "MEMBER";
};

export async function getMembers(roomId: number) {
    return apiFetch<Member[]>(
        `/rooms/${roomId}/members`,
    );
}

export async function getMemberAuthority(
    roomId: number,
    userId: number,
) {
    return apiFetch<string>(
        `/rooms/${roomId}/members/${userId}/authority`,
    );
}

export async function kickMember(
    roomId: number,
    userId: number,
) {
    return apiFetch<null>(
        `/rooms/${roomId}/members/${userId}`,
        {
            method: "DELETE",
        },
    );
}

export async function leaveRoom(roomId: number) {
    return apiFetch<null>(
        `/rooms/${roomId}/members/me`,
        {
            method: "DELETE",
        },
    );
}

export async function changeMemberAuthority(
    roomId: number,
    userId: number,
    authority: "OWNER" | "OPERATOR" | "MEMBER",
) {
    return apiFetch<null>(
        `/rooms/${roomId}/members/${userId}/authority`,
        {
            method: "PATCH",
            body: {
                authority,
            },
        },
    );
}

export async function delegateOwner(
    roomId: number,
    userId: number,
) {
    return apiFetch<null>(
        `/rooms/${roomId}/members/${userId}/owner`,
        {
            method: "PATCH",
        },
    );
}