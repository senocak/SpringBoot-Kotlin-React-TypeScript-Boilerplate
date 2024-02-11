import { User } from "./user"

export interface IRegisterParams {
    name: string
    email: string
    password: string
}

export interface ILoginParams {
    email: string
    password: string
}

export interface ILoginResponse {
    token: string
    refreshToken: string
    user: User
}

export interface IRegisterResponse {
    message: string
}

export type IActivateParamsType = {
    token: string
}

export interface IChangePasswordRequest {
    email: string
    password: string
    password_confirmation: string
}