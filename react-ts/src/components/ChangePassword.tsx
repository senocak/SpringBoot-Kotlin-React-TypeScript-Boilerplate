import React, {useEffect, useState} from 'react'
import {useAppDispatch, useAppSelector} from '../store'
import {IActivateParamsType, IRegisterResponse} from "../store/types/auth"
import {NavigateFunction, useNavigate, useParams} from 'react-router-dom'
import App from "./App"
import {IState} from "../store/types/global"
import {fetchChangePassword} from "../store/features/auth/changePasswordSlice"
import {logout} from "../store/features/auth/loginSlice"
import {resetMe} from "../store/features/auth/meSlice"
import {resetLogout} from "../store/features/auth/logoutSlice"
import {reset as resetPassword} from "../store/features/auth/resetPasswordSlice"

function ChangePassword(): React.JSX.Element {
    const {token} = useParams<IActivateParamsType>()
    const dispatch = useAppDispatch()
    const navigate: NavigateFunction = useNavigate()
    const changePasswordSlice: IState<IRegisterResponse> = useAppSelector(state => state.changePassword)
    const [email, setEmail] = useState<string>("")
    const [password, setPassword] = useState<string>("louie.Stehr2")
    const [passwordConfirmation, setpasswordConfirmation] = useState<string>("louie.Stehr2")

    useEffect((): void => {
        if (!changePasswordSlice.isLoading && changePasswordSlice.response !== null) {
            console.log("asd")
            dispatch(logout())
            dispatch(resetMe())
            dispatch(resetLogout())
            dispatch(resetPassword())
        }
    }, [changePasswordSlice, dispatch, navigate])

    return <>
        <App/>
        <input type="text" required autoFocus disabled value={token}/>
        <input type="email" placeholder="Email" required autoFocus disabled={changePasswordSlice.isLoading} value={email}
               onChange={(event: React.ChangeEvent<HTMLInputElement>): void => setEmail(event.target.value)}/>
        <input type="password" placeholder="***" required disabled={changePasswordSlice.isLoading} value={password}
               onChange={(event: React.ChangeEvent<HTMLInputElement>): void => setPassword(event.target.value)}/>
        <input type="password" placeholder="***" required disabled={changePasswordSlice.isLoading} value={passwordConfirmation}
               onChange={(event: React.ChangeEvent<HTMLInputElement>): void => setpasswordConfirmation(event.target.value)}/>
        <button disabled={changePasswordSlice.isLoading} onClick={(): void => {
            dispatch(fetchChangePassword({
                body: {
                    email: email,
                    password: password,
                    password_confirmation: passwordConfirmation
                },
                token: token!
            }))
        }}>Gönder</button>

        {changePasswordSlice.isLoading && <p>Bekleyin...</p>}
        {changePasswordSlice.error !== null &&
            <>
                <div style={{color: "red"}}>Hata:</div>
                {JSON.stringify(changePasswordSlice.error.response?.data?.exception)}
            </>
        }
        {changePasswordSlice.response !== null &&
            <>
                <div style={{color: "green"}}>Response:</div>
                {JSON.stringify(changePasswordSlice.response)}
            </>
        }
    </>
}

export default ChangePassword