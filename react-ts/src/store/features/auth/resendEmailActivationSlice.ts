import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import AuthApiClient from '../../../utils/http-client/AuthApiClient'
import {IRegisterResponse} from "../../types/auth"
import { IState } from '../../types/global'

const authApiClient: AuthApiClient = AuthApiClient.getInstance()

export const fetchResendEmailActivation = createAsyncThunk('auth/fetchResendEmailActivation',
                                        async (email: string, { rejectWithValue }) => {
    try {
        const { data } = await authApiClient.resendEmailActivation(email)
        return data
    } catch (error: any) {
        if (!error.response) {
            throw error
        }
        return rejectWithValue(error)
    }
})

const initialState: IState<IRegisterResponse> = {
    isLoading: false,
    response: null,
    error: null
}

const authResendEmailActivationSlice = createSlice({
    name: 'auth/resendEmailActivation',
    initialState,
    reducers: {
        reset: () => initialState
    },
    extraReducers: builder => {
        builder.addCase(fetchResendEmailActivation.pending, state => {
            state.isLoading = true
            state.response = null
            state.error = null
        })

        builder.addCase(fetchResendEmailActivation.fulfilled, (state, action: PayloadAction<IRegisterResponse>) => {
            state.isLoading = false
            state.response = action.payload
            state.error = null
        })

        builder.addCase(fetchResendEmailActivation.rejected, (state, action) => {
            state.isLoading = false
            state.response = null
            state.error = action.payload
        })
    }
})

export default authResendEmailActivationSlice.reducer
export const {
    reset
} = authResendEmailActivationSlice.actions