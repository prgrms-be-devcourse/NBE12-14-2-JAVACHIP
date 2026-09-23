import { apiFetch } from "./client";

export type Room = {
  id: number;
  name: string;
  totalBudget: number;
  availableBudget: number;
  currency: string;
  createdAt: string;
};

export type RoomCurrency = "KRW" | "USD" | "JPY";

export type RoomCreateRequest = {
  name: string;
  totalBudget: number;
  currency: RoomCurrency;
};

export type RoomUpdateRequest = {
  name: string;
};

export type Member = {
  userId: number;
  name: string;
  authority: "OWNER" | "OPERATOR" | "MEMBER";
};

type RoomListResponse = {
  rooms: Room[];
};

export async function getRooms() {
  const data = await apiFetch<RoomListResponse>("/rooms");
  return data.rooms;
}

export async function getRoom(roomId: number) {
  return apiFetch<Room>(`/rooms/${roomId}`);
}

export async function createRoom(request: RoomCreateRequest) {
  return apiFetch<Room>("/rooms", {
    method: "POST",
    body: request,
  });
}

export async function updateRoom(
  roomId: number,
  request: RoomUpdateRequest,
) {
  return apiFetch<Room>(`/rooms/${roomId}`, {
    method: "PATCH",
    body: request,
  });
}

export async function deleteRoom(roomId: number) {
  return apiFetch<null>(`/rooms/${roomId}`, {
    method: "DELETE",
  });
}

export async function getMembers(roomId: number) {
  return apiFetch<Member[]>(`/rooms/${roomId}/members`);
}

export async function kickMember(roomId: number, userId: number) {
  return apiFetch<null>(`/rooms/${roomId}/members/${userId}`, {
    method: "DELETE",
  });
}
