import { apiFetch } from "./client";

export type Room = {
  id: number;
  name: string;
  totalBudget: number;
  availableBudget: number;
  currency: string;
  createdAt: string;
};

type RoomListResponse = {
  rooms: Room[];
};

export async function getRooms() {
  const data = await apiFetch<RoomListResponse>("/rooms");
  return data.rooms;
}
