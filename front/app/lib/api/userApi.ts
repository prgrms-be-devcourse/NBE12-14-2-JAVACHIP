import { apiFetch } from "./client";

export type User = {
    id: number;
    email: string;
    name: string;
};

export type UserJoinRequest = {
    email: string;
    password: string;
    name: string;
};

export type UserLoginRequest = {
    email: string;
    password: string;
};

export type UserLoginResponse = {
    user: User;
    accessToken: string;
};

export async function signup(request: UserJoinRequest) {
    return apiFetch<User>(
        "/users/join",
        {
            method: "POST",
            body: request,
            redirectOnUnauthorized: false,
        },
    );
}

export async function login(request: UserLoginRequest) {
    return apiFetch<UserLoginResponse>(
        "/users/login",
        {
            method: "POST",
            body: request,
            redirectOnUnauthorized: false,
        },
    );
}