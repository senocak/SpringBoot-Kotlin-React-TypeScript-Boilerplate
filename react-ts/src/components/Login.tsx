import React, {useEffect, useState} from 'react'
import App from "./App"
import { useAppDispatch, useAppSelector } from '../store'
import {IState} from "../store/types/global"
import {ILoginResponse, IRegisterResponse} from "../store/types/auth"
import { fetchLogin } from '../store/features/auth/loginSlice'
import {NavigateFunction, useNavigate} from 'react-router-dom'
import { fetchMe } from '../store/features/auth/meSlice'
import {fetchResendEmailActivation} from "../store/features/auth/resendEmailActivationSlice"
import {fetchResetPassword} from "../store/features/auth/resetPasswordSlice"

function Login(): React.JSX.Element {
    const dispatch = useAppDispatch()
    const navigate: NavigateFunction = useNavigate()
    const loginSlice: IState<ILoginResponse> = useAppSelector(state => state.login)
    const resendEmailActivationSlice: IState<IRegisterResponse> = useAppSelector(state => state.resendEmailActivation)
    const resetPasswordSlice: IState<IRegisterResponse> = useAppSelector(state => state.resetPassword)
    const [email, setEmail] = useState<string>("anil2@senocak.com")
    const [password, setPassword] = useState<string>("louie.Stehr1")
    const [error, setError] = useState<string>("")

    useEffect((): void => {
        if (!loginSlice.isLoading && loginSlice.response !== null) {
            dispatch(fetchMe())
            navigate('/')
            setError("")
            return
        }
        if (loginSlice.error !== null) {
            setError(loginSlice.error.response?.data?.exception)
        }

    }, [loginSlice, dispatch, navigate])

    useEffect((): void => {
        if (resendEmailActivationSlice.error !== null) {
            setError(resendEmailActivationSlice.error.response?.data?.exception)
        } else {
            setError("")
        }
    }, [resendEmailActivationSlice])

    useEffect((): void => {
        if (resetPasswordSlice.error !== null) {
            setError(resetPasswordSlice.error.response?.data?.exception)
        } else {
            setError("")
        }
    }, [resetPasswordSlice])

    return <>
        <App/>
        <input type="email" placeholder="Email" required autoFocus disabled={loginSlice.isLoading} value={email}
               onChange={(event: React.ChangeEvent<HTMLInputElement>): void => setEmail(event.target.value)}/>
        <input type="password" placeholder="***" required disabled={loginSlice.isLoading} value={password}
               onChange={(event: React.ChangeEvent<HTMLInputElement>): void => setPassword(event.target.value)}/>
        <button disabled={loginSlice.isLoading} onClick={(): void => {
            dispatch(fetchLogin({email: email, password: password}))
        }}>Gönder</button>
        <button onClick={(): void => {
            dispatch(fetchLogin({email: "anil1@senocak.com", password: password}))
        }}>Admin</button>
        {(email !== null && email !== "") &&
            <>
                <button onClick={(): void => {
                    dispatch(fetchResendEmailActivation(email))
                }}>Email Aktivasyon Gönder</button>
                <button onClick={(): void => {
                    dispatch(fetchResetPassword(email))
                }}>Şifre Sıfırlama</button>
            </>
        }
        {(loginSlice.isLoading || resendEmailActivationSlice.isLoading || resetPasswordSlice.isLoading) &&
            <p>Bekleyin...</p>}
        {(error !== null && error !== "") && <div>{JSON.stringify(error)}</div>}
        {(!resendEmailActivationSlice.isLoading && resendEmailActivationSlice.response !== null) &&
            <p>{JSON.stringify(resendEmailActivationSlice.response)}</p>}
        {(!resetPasswordSlice.isLoading && resetPasswordSlice.response !== null) &&
            <p>{JSON.stringify(resetPasswordSlice.response)}</p>}
    </>
}

export default Login