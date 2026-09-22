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

export type CreateRoomRequest = {
  name: string;
  totalBudget: number;
  currency: RoomCurrency;
};

type RoomListResponse = {
  rooms: Room[];
};

export async function getRooms() {
  const data = await apiFetch<RoomListResponse>("/rooms");
  return data.rooms;
}

export function getRoom(roomId: number) {
  return apiFetch<Room>(`/rooms/${roomId}`);
}

export function createRoom(request: CreateRoomRequest) {
  return apiFetch<Room>("/rooms", {
    method: "POST",
    body: request,
  });
}
