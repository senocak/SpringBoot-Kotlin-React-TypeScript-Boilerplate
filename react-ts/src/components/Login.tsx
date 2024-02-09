import React, {useEffect, useState} from 'react'
import App from "./App"
import { useAppDispatch, useAppSelector } from '../store'
import {IState} from "../store/types/global"
import {ILoginResponse} from "../store/types/auth"
import { fetchLogin } from '../store/features/auth/loginSlice'
import {NavigateFunction, useNavigate } from 'react-router-dom'
import { fetchMe } from '../store/features/auth/meSlice'

function Login(): React.JSX.Element {
    const dispatch = useAppDispatch()
    const navigate: NavigateFunction = useNavigate()
    const loginSlice: IState<ILoginResponse> = useAppSelector(state => state.login)
    const [email, setEmail] = useState<string>("anil2@senocak.com")
    const [password, setPassword] = useState<string>("asenocak")

    useEffect((): void => {
        if (!loginSlice.isLoading && loginSlice.response !== null) {
            dispatch(fetchMe())
            navigate('/')
        }
    }, [loginSlice, dispatch, navigate])
    return <>
        <App/>
        <br/>
        <input type="text" placeholder="Username" required autoFocus disabled={loginSlice.isLoading} value={email}
               onChange={(event: React.ChangeEvent<HTMLInputElement>): void => setEmail(event.target.value)}/>
        <input type="password" placeholder="***" required disabled={loginSlice.isLoading} value={password}
               onChange={(event: React.ChangeEvent<HTMLInputElement>): void => setPassword(event.target.value)}/>
        <button disabled={loginSlice.isLoading} onClick={(): void => {dispatch(fetchLogin({email: email, password: password}))}}>Gönder</button>
        <button onClick={(): void => {dispatch(fetchLogin({email: "anil1@senocak.com", password: password}))}}>Admin</button>
        {loginSlice.isLoading && <p>Bekleyin...</p>}
        {loginSlice.error !== null &&
            <div>
                {JSON.stringify(loginSlice.error.response?.data?.exception)}
            </div>
        }
    </>
}

export default Login