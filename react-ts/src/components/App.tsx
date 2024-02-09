import React, {useEffect, useState} from 'react'
import {Link} from "react-router-dom"
import {useAppDispatch, useAppSelector} from "../store"
import { IState } from '../store/types/global'
import {Role, User} from '../store/types/user'

function App(): React.JSX.Element {
    const dispatch = useAppDispatch()
    const me: IState<User> = useAppSelector(state => state.me)
    const [isAuthorized, setIsAuthorized] = useState<boolean>(false)

    useEffect((): void => {
        if (me.response) {
            setIsAuthorized(me.response.roles.some((e: Role): boolean => e.name === 'ADMIN'))
        }
    }, [me, dispatch])

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
                <Link to={`/logout`}><button>Çıkış</button></Link>
                <p>{JSON.stringify(me.response)}</p>
            </>
        }
    </>
}

export default App