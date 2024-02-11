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

function App(): React.JSX.Element {
    const dispatch = useAppDispatch()
    const navigate: NavigateFunction = useNavigate()
    const me: IState<User> = useAppSelector(state => state.me)
    const logoutSlice: IState<string> = useAppSelector(state => state.logout)
    const resetPasswordSlice: IState<IRegisterResponse> = useAppSelector(state => state.resetPassword)
    const [isAuthorized, setIsAuthorized] = useState<boolean>(false)
    const [error, setError] = useState<string>("")

    useEffect((): void => {
        setError("")
        if (me.response) {
            setIsAuthorized(me.response.roles.some((e: Role): boolean => e.name === 'ADMIN'))
            return
        }
        if (me.error !== null) {
            setError(me.error.response?.data?.exception)
        }
    }, [me, dispatch])

    useEffect((): void => {
        setError("")
        if (!logoutSlice.isLoading && logoutSlice.response === "nocontent") {
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
                <button onClick={(): void => {
                    setError("")
                    dispatch(fetchResetPassword(me.response?.email!))}}>Şifre Sıfırlama</button>
                <hr/>
                <p>{JSON.stringify(me.response)}</p>
                {(!resetPasswordSlice.isLoading && resetPasswordSlice.response !== null) &&
                    <p>{JSON.stringify(resetPasswordSlice.response)}</p>}

                {(error !== null && error !== "") && <p>{JSON.stringify(error)}</p>}
            </>
        }
    </>
}

export default App