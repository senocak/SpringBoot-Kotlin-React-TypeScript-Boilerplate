export interface WsRequestBody {
    from?: string
    to?: string
    type: WsType
    content: string
    date: number
}

export enum WsType {
    Online = 'online',
    Offline = 'offline',
    Login = 'login',
    Logout = 'logout'
}