import React, {useEffect} from 'react'
import {useAppDispatch, useAppSelector} from '../store'
import {IActivateParamsType, IRegisterResponse} from "../store/types/auth"
import {NavigateFunction, useNavigate, useParams} from 'react-router-dom'
import App from "./App";
import {fetchActivate} from "../store/features/auth/activateSlice";
import {IState} from "../store/types/global";

function Activate(): React.JSX.Element {
    const {token} = useParams<IActivateParamsType>()
    const activateSlice: IState<IRegisterResponse> = useAppSelector(state => state.activate)
    const dispatch = useAppDispatch()
    const navigate: NavigateFunction = useNavigate()

    useEffect((): void => {
        if (token)
            dispatch(fetchActivate(token))
        else
            alert("Token zorunlu")
    }, [dispatch, navigate])

    return <>
        <App/>
        {activateSlice.isLoading && <p>Bekleyin...</p>}
        {activateSlice.error !== null &&
            <>
                <div style={{color: "red"}}>Hata:</div>{JSON.stringify(activateSlice.error.response?.data?.exception)}
            </>
        }
        {activateSlice.response !== null &&
            <>
                <div style={{color: "green"}}>Response:</div>{JSON.stringify(activateSlice.response)}
            </>
        }
    </>
}
export default Activate