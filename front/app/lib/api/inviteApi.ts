import { apiFetch } from "./client";

export type Invite = {
    code: string;
    expireAt: string;
};

export type InviteJoinResponse = {
    userId: number;
    roomId: number;
    authority: string;
    joined: boolean;
    createdAt: string;
};

export async function createInvite(roomId: number) {
    return apiFetch<Invite>(
        `/rooms/${roomId}/invites`,
        {
            method: "POST",
        },
    );
}

export async function getInvites(roomId: number) {
    return apiFetch<Invite[]>(
        `/rooms/${roomId}/invites`,
    );
}

export async function joinRoom(code: string) {
    return apiFetch<InviteJoinResponse>(
        `/rooms/join/${code}`,
        {
            method: "POST",
        },
    );
}

export async function verifyInvite(token: string) {
    return apiFetch<Invite>(
        `/invites/${token}`,
    );
}