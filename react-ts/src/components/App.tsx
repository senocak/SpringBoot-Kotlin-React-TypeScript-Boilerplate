import React, {useEffect, useState} from 'react'
import {Link, NavigateFunction, useNavigate} from "react-router-dom"
import {useAppDispatch, useAppSelector} from "../store"
import { IState } from '../store/types/global'
import {Role, User} from '../store/types/user'
import {fetchLogout, resetLogout} from "../store/features/auth/logoutSlice"
import {reset as resetPassword} from "../store/features/auth/resetPasswordSlice"
import {resetMe} from "../store/features/auth/meSlice"
import {logout} from "../store/features/auth/loginSlice"
import {fetchResetPassword} from "../store/features/auth/resetPasswordSlice"
import {IRegisterResponse} from "../store/types/auth"
import app from "../config/app"
import AppStorage from "../utils/storage"
import {IMessageEvent, w3cwebsocket as WebSocket} from 'websocket'
import {WsRequestBody, WsType} from "../store/types/ws"

function App(): React.JSX.Element {
    const dispatch = useAppDispatch()
    const navigate: NavigateFunction = useNavigate()
    const me: IState<User> = useAppSelector(state => state.me)
    const logoutSlice: IState<string> = useAppSelector(state => state.logout)
    const resetPasswordSlice: IState<IRegisterResponse> = useAppSelector(state => state.resetPassword)
    const [isAuthorized, setIsAuthorized] = useState<boolean>(false)
    const [error, setError] = useState<string>("")

    const [online, setOnline] = useState<string[]>([])
    const [wsConnection, setWsConnection] = useState<WebSocket |null>(null)
    const [notification, setNotification] = useState({show: false, color: "green", msg: ""})

    useEffect((): void => {
        setError("")
        if (me.response) {
            setIsAuthorized(me.response.roles.some((e: Role): boolean => e.name === 'ADMIN'))
            const ws: WebSocket = new WebSocket(`${app.WS_BASE}/ws?access_token=${AppStorage.getAccessToken()}`)
            ws.onopen = (): void => {
                setWsConnection(ws)
                setNotification({show: true, color: 'green', msg: `Websocket bağlandı`})
                setTimeout((): void => {
                    setNotification({show: false, color: '', msg: ''})
                }, 1_000)
            }
            ws.onmessage = (event: IMessageEvent): void => {
                const parse: WsRequestBody = JSON.parse(event.data.toString())
                console.log("parse:"+parse)
                if (parse.type === WsType.Online) {
                    setOnline(parse.content!.split(","))
                }else if (parse.type === WsType.Login) {
                    if (!online.includes(parse.content!)) {
                        setOnline((prevArray: string[]) => [...prevArray, parse.content!]);
                    }
                }else if (parse.type === WsType.Logout) {
                    if (!online.includes(parse.content!)) {
                        setOnline((prevArray: string[]) => prevArray.filter((item: string) => item !== parse.content!));
                    }
                }
            }
            return
        }
        if (me.error !== null) {
            setError(me.error.response?.data?.exception)
        }
    }, [me, dispatch])

    useEffect((): void => {
        setError("")
        if (!logoutSlice.isLoading && logoutSlice.response === "nocontent") {
            wsConnection?.close(3333, "")
            setWsConnection(null)
            dispatch(logout())
            dispatch(resetMe())
            dispatch(resetLogout())
            dispatch(resetPassword())
            return
        }
        if (logoutSlice.error !== null) {
            setError(logoutSlice.error.response?.data?.exception)
        }
    }, [logoutSlice, dispatch, navigate])

    useEffect((): void => {
        setError("")
        if (resetPasswordSlice.error !== null) {
            setError(resetPasswordSlice.error.response?.data?.exception)
        }
    }, [resetPasswordSlice, dispatch])

    return <>
        <Link to='/'><button>AnaSayfa</button></Link>
        {me.response === null
            ?
            <>
                <Link to='/auth/login'><button>Giriş Yap</button></Link>
                <Link to='/auth/register'><button>Kayıt Ol</button></Link>
                <hr/>
            </>
            :
            <>
                {isAuthorized && <Link to={`/admin/users`}><button>Tüm Kullanıcılar</button></Link>}
                <button onClick={(): void => {dispatch(fetchLogout())}}>Çıkış</button>
                <button onClick={(): void => {setError(""); dispatch(fetchResetPassword(me.response?.email!))}}>Şifre Sıfırlama</button>
                <hr/>
                <p>{JSON.stringify(me.response)}</p>
                {(!resetPasswordSlice.isLoading && resetPasswordSlice.response !== null) &&
                    <p>{JSON.stringify(resetPasswordSlice.response)}</p>}

                {(error !== null && error !== "") && <p>{JSON.stringify(error)}</p>}
                <p>Online: {JSON.stringify(online)}</p>
            </>
        }

        {notification.show &&
            <div
                style={{
                    zIndex: 999999,
                    position: 'fixed',
                    top: '1rem',
                    right: '2rem',
                    width: '250px',
                    height: '35px',
                    borderRadius: '5px',
                    borderLeft: `5px solid ${notification.color}`,
                    padding: '1rem 1rem',
                    boxShadow: 'var(--soft-shadow)',
                    transition: 'transform 200ms ease-in-out',
                    animation: 'slideForNotification 3000ms 2',
                    color: 'white',
                    backgroundColor: 'gray'
                }}
            >
                <p dangerouslySetInnerHTML={{__html: notification.msg}}></p>
            </div>
        }
    </>
}

export default App