import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import AuthApiClient from '../../../utils/http-client/AuthApiClient'
import {IRegisterResponse} from "../../types/auth"
import { IState } from '../../types/global'

const authApiClient: AuthApiClient = AuthApiClient.getInstance()

export const fetchResetPassword = createAsyncThunk('auth/fetchResetPassword',
                                        async (email: string, { rejectWithValue }) => {
    try {
        const { data } = await authApiClient.resetPassword(email)
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

const authResetPasswordSlice = createSlice({
    name: 'auth/resendEmailActivation',
    initialState,
    reducers: {
        reset: () => initialState
    },
    extraReducers: builder => {
        builder.addCase(fetchResetPassword.pending, state => {
            state.isLoading = true
            state.response = null
            state.error = null
        })

        builder.addCase(fetchResetPassword.fulfilled, (state, action: PayloadAction<IRegisterResponse>) => {
            state.isLoading = false
            state.response = action.payload
            state.error = null
        })

        builder.addCase(fetchResetPassword.rejected, (state, action) => {
            state.isLoading = false
            state.response = null
            state.error = action.payload
        })
    }
})

export default authResetPasswordSlice.reducer
export const {
    reset
} = authResetPasswordSlice.actions