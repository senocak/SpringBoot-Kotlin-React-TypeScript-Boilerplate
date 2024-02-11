import React, {useEffect, useState} from 'react'
import {Link, NavigateFunction, useNavigate} from "react-router-dom"
import {useAppDispatch, useAppSelector} from "../store"
import { IState } from '../store/types/global'
import {Role, User} from '../store/types/user'
import {fetchLogout} from "../store/features/auth/logoutSlice"
import {resetMe} from "../store/features/auth/meSlice"
import {logout} from "../store/features/auth/loginSlice"

function App(): React.JSX.Element {
    const dispatch = useAppDispatch()
    const navigate: NavigateFunction = useNavigate()
    const me: IState<User> = useAppSelector(state => state.me)
    const logoutSlice: IState<string> = useAppSelector(state => state.logout)
    const [isAuthorized, setIsAuthorized] = useState<boolean>(false)

    useEffect((): void => {
        if (me.response) {
            setIsAuthorized(me.response.roles.some((e: Role): boolean => e.name === 'ADMIN'))
        }
    }, [me, dispatch])

    useEffect((): void => {
        if (!logoutSlice.isLoading && logoutSlice.response === "nocontent") {
            console.log("logoutSlice.response",logoutSlice.response)
            dispatch(logout())
            dispatch(resetMe())
        }
    }, [logoutSlice, dispatch, navigate])

    return <>
        <Link to='/'><button>AnaSayfa</button></Link>
        {me.response === null
            ?
            <>
                <Link to='/auth/login'><button>Giriş Yap</button></Link>
                <Link to='/auth/register'><button>Kayıt Ol</button></Link>
            </>
            :
            <>
                {isAuthorized && <Link to={`/admin/users`}><button>Tüm Kullanıcılar</button></Link>}
                <button onClick={(): void => {dispatch(fetchLogout())}}>Çıkış</button>
                <p>{JSON.stringify(me.response)}</p>
            </>
        }
    </>
}

export default App